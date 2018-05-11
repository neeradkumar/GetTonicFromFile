package iitm.speechlab.gettonicfromfile.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import iitm.speechlab.gettonicfromfile.Constants;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.utils.MultiPartUtils;
import iitm.speechlab.gettonicfromfile.utils.SharedPrefUtils;
import static iitm.speechlab.gettonicfromfile.utils.Utils.isInteger;

public class GetTonicDrone extends AsyncTask<Void, Integer, String> {

    private WeakReference<OnlineCalculationActivity> activityReference;
    private String fileName;
    private String metadata;
    private String percentage;
    private String seconds;
    private String method;
    private ProgressDialog dialog;
    // only retain a weak reference to the activity
    GetTonicDrone(OnlineCalculationActivity context, String fileName, String metadata) {
        activityReference = new WeakReference<>(context);
        this.fileName = fileName;
        this.metadata = metadata;
        dialog = new ProgressDialog(context);
        percentage = SharedPrefUtils.getStringData(context, Constants.PERCENTAGE, Constants.DEFAULT_PERCENTAGE);
        seconds = SharedPrefUtils.getStringData(context, Constants.NO_SECS, Constants.DEFAULT_SECONDS);
        method = SharedPrefUtils.getStringData(context, Constants.DEFAULT_METHOD, Constants.DEFAULT_METHOD);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        OnlineCalculationActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;
        dialog.setMessage(activity.getResources().getString(R.string.calculating_wait));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        return calculateTonicOnline(fileName,metadata, percentage, seconds, method);

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(dialog.isShowing()) dialog.dismiss();
        // get a reference to the activity if it is still there
        final OnlineCalculationActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing() || s==null) return;
        s= android.text.Html.fromHtml(s).toString();
        Log.d("OnlineCalculationAct","start"+s+"end");
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        if(isInteger(s.replaceAll("[^A-Za-z0-9]", "").trim())){
            s = s.replaceAll("[^A-Za-z0-9]", "");
            alertDialog.setTitle(R.string.success);
            alertDialog.setMessage(activity.getResources().getString(R.string.calculated_tonic,s));
        }
        else{
            alertDialog.setTitle(R.string.an_error_occured);
            alertDialog.setMessage(s);
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, activity.getResources().getString(R.string.change_metadata),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getResources().getString(R.string.measure_another),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        activity.finishActivity();
                    }
                });
        alertDialog.show();

    }

    private String calculateTonicOnline(String fileName, String metadata, String percentage, String seconds, String method) {
        String responseString;

        try {
            String requestURL = SharedPrefUtils.getStringData(activityReference.get(),Constants.SERVER, Constants.DEFAULT_SERVER);
            MultiPartUtils multipart = new MultiPartUtils(requestURL);
            //multipart.addDoubleArrayAsByteFile("wav_samples", wavFileSamples.getSamples());
            multipart.addFormField("file_name", fileName);
            multipart.addFormField("meta_data", metadata);
            multipart.addFormField("percentage", percentage);
            multipart.addFormField("seconds", seconds);
            multipart.addFormField("method", method);
            responseString = multipart.finish();
        } catch (IOException e) {
            responseString = e.toString();
        }

        return responseString;
    }

}

