package iitm.speechlab.gettonicfromfile.activity;

import android.util.Log;

import java.io.*;

import iitm.speechlab.gettonicfromfile.WavFile;
import iitm.speechlab.gettonicfromfile.WavFileException;

import static java.lang.Math.PI;
import static java.lang.Math.log;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class AudioUtils {
    /*
    Fourier transform of samples.
    Inputs -
    Data:
    real part of i-th data = data[2*i+1]
    complex part of i-th data = data[2*i+2]
    nn- a power of two and greater than number of elements
    isign - Fourier if it is +1, Inverse if it is -1
    */
    private static void four1(double data[], int nn, int isign)
    {
        int n, mmax, m, j, istep, i;
        double wtemp, wr, wpr, wpi, wi, theta;
        double tempr, tempi;

        n = nn << 1;
        j = 1;
        for (i = 1; i < n; i += 2) {
            if (j > i) {
                tempr = data[j];     data[j] = data[i];     data[i] = tempr;
                tempr = data[j+1]; data[j+1] = data[i+1]; data[i+1] = tempr;
            }
            m = n >> 1;
            while (m >= 2 && j > m) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }
        mmax = 2;
        while (n > mmax) {
            istep = 2*mmax;
            theta = PI/(isign*mmax);
            wtemp = sin(0.5*theta);
            wpr = -2.0*wtemp*wtemp;
            wpi = sin(theta);
            wr = 1.0;
            wi = 0.0;
            for (m = 1; m < mmax; m += 2) {
                for (i = m; i <= n; i += istep) {
                    j =i + mmax;
                    tempr = wr*data[j]   - wi*data[j+1];
                    tempi = wr*data[j+1] + wi*data[j];
                    data[j]   = data[i]   - tempr;
                    data[j+1] = data[i+1] - tempi;
                    data[i] += tempr;
                    data[i+1] += tempi;
                }
                wr = (wtemp = wr)*wpr - wi*wpi + wr;
                wi = wi*wpr + wtemp*wpi + wi;
            }
            mmax = istep;
        }
    }

    /* Round up to next higher power of 2 (return x if it's already a power of 2).*/
    private static int Pow2Roundup(int x)
    {
        if (x < 0)
            return 0;
        --x;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x+1;
    }

    /*return pitch for an array of fourier samplings*/
    public static float CepPitch(double[] EX,int winSize2, int rate,int maxim,int minim)
    {
        int j;
        int ms2 = (rate/maxim);
        int ms20 = (rate/minim);
        int maxIndex=1;
        double maxValue = 0.0;
        for(j=1;j<winSize2 ;j+=2)
        {
            EX[j] = log(EX[j]*EX[j] + EX[j+1]*EX[j+1]+Double.MIN_VALUE) ;
            EX[j+1] = 0.0;
        }
        four1(EX,(winSize2-1)/2,-1);
        for(j=1; j<winSize2; j++)
        {
            EX[j] /= (winSize2-1)/2;
        }

        maxValue = 0.0;
        for(j=ms2;j<=ms20;j++)
        {
            if((EX[2*j+1]*EX[2*j+1] + EX[2*j+2]*EX[2*j+2])> maxValue)
            {
                maxValue = (EX[2*j+1]*EX[2*j+1] + EX[2*j+2]*EX[2*j+2]);
                maxIndex = j;
            }
        }
        return (rate/(float)maxIndex);
    }

    /*Get minimum maximum pitch given metadata*/
    private static int getMin(int metadata)
    {
        switch(metadata)
        {
            case 1:
        		return  100;
            case 2:
                return 140;
            case 3:
                return 120;
            case 4:
		        return 100;
        }
        return 100;
    }
    private static int getMax(int metadata)
    {
        switch(metadata)
        {
            case 1:
                return  180;
            case 2:
                return 250;
            case 3:
                return 215;
            case 4:
                return 250;
        }
        return 100;
    }

    /*Return pitch given Low Energy frames using ENMF*/

    private static float PitchENMF(double[][] EXLowEng, int height, int winSize, int rate, int minim, int maxim)
    {
        double[] W = new double[winSize];

        int i,j;
        /*Mean of magnitudes*/
        for(j=1;j<winSize;j+=2)
        {
            for(i=0;i<height;i++)
            {

                W[j] += sqrt(EXLowEng[i][j]*EXLowEng[i][j] + EXLowEng[i][j+1]*EXLowEng[i][j+1]);
            }
            W[j+1] = 0.0;
        }
        /*normalise*/
        for(j=1;j<winSize;j++)
        {
            W[j] /= 2*0.5*(winSize-1);
        }
        return CepPitch(W,winSize, rate, maxim, minim);
    }

    /*Return pitch given Low Energy frames - Low Energy + Histogram method*/

    private static int LowEnergyHist(double[][] EXLowEng, int winSize, int height, int rate, int maxim, int minim)
    {
        int[] histogram = new int[600];

        float pitch;
        int i;
        for(i=0;i<height;i++)
        {
            pitch = CepPitch(EXLowEng[i], winSize, rate, maxim, minim);
            histogram[(int)pitch] += 1;
        }
        int maxValue = 0;
        int maxIndex = 0;
        for(i=0;i<600;i++)
        {
            if(histogram[i]>maxValue)
            {
                maxValue = histogram[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }



    /*
    GetTonicDrone
    Inputs:
    filename - Name of wav file
    metadata - 1 male; 2 female; 3 Instrumental; 4 - not known; Default = 4
    sec - No.of seconds of sampling to be taken
    per - Percentage of low energy frames to be taken
    algo - 1 for enmf, 2 for ceppitch
    */
    public static int getTonicDrone (String filename, int metadata,int sec, int per, int algo) throws IOException, WavFileException {


        Log.d("AudioUtils","getTonicDrone");
        int numItems;

        int i,j,k;
        WavFile wavFile = WavFile.openWavFile(new File(filename));

        numItems = (int)(wavFile.getNumFrames()*wavFile.getNumChannels());
        Log.d("AudioUtils","numItems "+numItems);
        //double[] wavFileDouble = new double[numItems];
        //wavFile.readFrames(wavFileDouble,numItems);
        int samplerate = (int)wavFile.getSampleRate();



        /* Take the required duration and store it in ex */

        double[] ex;
        int exSize;
        int dur =  numItems/samplerate - sec;

        if (dur > 1)
        {
            int r = (int)(Math.random())%dur;
            //random number to determine starting point
            exSize = sec*(samplerate);
            ex = new double[exSize];
            wavFile.readFrames(ex,r*samplerate,exSize);
            //for(i=0;i<exSize;i++)	ex[i] = wavFileDouble[i+r*samplerate];
        }
        else
        {
            exSize = numItems;
            ex = new double[exSize];
            wavFile.readFrames(ex,exSize);
        }
        wavFile.close();
        Log.d("AudioUtils","closed wavFile ");

        /* Split into frames and store in EX */

        int winSize = 1+ 2* 2048*samplerate/44100;
        int winSize2 = 1+2*Pow2Roundup(2048*samplerate/44100);

        int temp = (winSize-1)/2 - (samplerate/100);

        int height = 2 + (exSize-winSize)*100/samplerate;

        double[][] EX = new double[height][winSize2+1];


        for(i=-temp ,j=0;i<exSize && j<height;j++)
        {
            for(k=1;k<winSize2;k=k+2,i++)	//alternate numbers imaginary part
            {
                if(i<0) EX[j][k] = 0;
                else EX[j][k] = ex[i];
            }
            i = i- temp;
        }

	/* Apply FFT, calculate energy using energy parsevals theorem,
	store in first element of each frame */
        for(i=0;i<height;i++)
        {
            four1(EX[i],(winSize2-1)/2,1);
            EX[i][0] = 0.0;
            for(j=1;j<winSize2;j++)	EX[i][0] += EX[i][j]*EX[i][j];
        }

        Log.d("AudioUtils","FFT done, taking least energy frames "+height+" "+winSize2);
        /*Take frames with least energy*/
        int newHeight = (int)(per*height/100.0);

        double[][] EXLowEng = new double[newHeight][winSize2+1];

        double prevLeast = 0.0, nowLeast;
        int newIndex=0;
        for(i=0;i<newHeight;i++)
        {
            nowLeast = Double.MAX_VALUE; //infinity
            for(j=0;j<height;j++)
            {
                if(EX[j][0]>prevLeast && EX[j][0]<nowLeast)
                //also leave out zero energy frames
                {
                    newIndex = j;
                    nowLeast = EX[j][0];
                }
            }
            prevLeast = EX[newIndex][0];
            for(j=1;j<winSize2;j++)
            {
                EXLowEng[i][j] = EX[newIndex][j];
            }
        }


        int minim = getMin(metadata);
        int maxim = getMax(metadata);

        Log.d("AudioUtils","calculating now "+numItems);
        /*ENMF or LowEnergyHist*/
        int pitch;
        if(algo == 1) pitch = (int)PitchENMF(EXLowEng, newHeight, winSize2, samplerate, minim, maxim) ;

        else pitch = LowEnergyHist(EXLowEng, winSize2, newHeight, samplerate, maxim, minim);

        Log.d("AudioUtils","Calculated pitch "+pitch);
        return pitch;
    }
}
