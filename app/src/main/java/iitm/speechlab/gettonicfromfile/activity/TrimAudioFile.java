package iitm.speechlab.gettonicfromfile.activity;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.IOException;
import java.lang.ref.WeakReference;

import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.utils.AudioUtils;
import iitm.speechlab.gettonicfromfile.wavFileUtils.WavFileException;

import static iitm.speechlab.gettonicfromfile.activity.OnlineCalculationActivity.isFtp;

public class TrimAudioFile extends AsyncTask<Void, Integer, String> {

    private String filePath;
    private String trimmedFilePath;
    private int seconds;
    private WeakReference<OnlineCalculationActivity> activityReference;
    private UploadStatusDelegate uploadStatusDelegate;

    TrimAudioFile(OnlineCalculationActivity context, String filePath, String trimmedFilePath,
                  int seconds, UploadStatusDelegate uploadStatusDelegate) {
        activityReference = new WeakReference<>(context);
        this.filePath = filePath;
        this.trimmedFilePath = trimmedFilePath;
        this.seconds = seconds;
        this.uploadStatusDelegate = uploadStatusDelegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        final OnlineCalculationActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;
        final TextView statusTextView = activity.findViewById(R.id.upload_status_text);
        statusTextView.setText(R.string.trimming);
        final ProgressBar progressBar = activity.findViewById(R.id.upload_progress_bar);
        progressBar.setIndeterminate(true);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            AudioUtils.trim(filePath,trimmedFilePath, seconds);
            return "Success";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        return "Failure";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        final OnlineCalculationActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;
        final ProgressBar progressBar = activity.findViewById(R.id.upload_progress_bar);
        progressBar.setIndeterminate(false);
        if("Success".equals(s)){
            final TextView statusTextView = activity.findViewById(R.id.upload_status_text);
            statusTextView.setText(R.string.upload_in_progress);
            activity.uploadFTP(activity, trimmedFilePath, uploadStatusDelegate, isFtp);
        }
        else{
            final TextView statusTextView = activity.findViewById(R.id.upload_status_text);
            statusTextView.setText(R.string.failed_to_trim);
        }

    }
}