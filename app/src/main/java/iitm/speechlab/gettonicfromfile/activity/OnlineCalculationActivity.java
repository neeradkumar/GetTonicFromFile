package iitm.speechlab.gettonicfromfile.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;
import net.gotev.uploadservice.ftp.FTPUploadRequest;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import iitm.speechlab.gettonicfromfile.Constants;
import iitm.speechlab.gettonicfromfile.networkUtils.MultiPartUtils;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.networkUtils.SharedPrefUtils;
import iitm.speechlab.gettonicfromfile.views.TableButtonGroupLayout;

public class OnlineCalculationActivity extends AppCompatActivity {

    String fileName;
    boolean uploadComplete = false;
    String uploadId;
    String filePath;
    boolean finishing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_calculation);
        Uri file = getIntent().getParcelableExtra(Constants.URI);
        filePath = file.getPath();
        fileName = (new File(filePath)).getName();

        //set default as male
        RadioButton maleRadioButton = findViewById(R.id.metadata_male);
        TableButtonGroupLayout tableButtonGroupLayout = findViewById(R.id.metadata_radio_group);
        tableButtonGroupLayout.setChecked(maleRadioButton);

        final Button calculateButton = findViewById(R.id.calculate_button);
        final TextView statusTextView = findViewById(R.id.upload_status_text);
        final ImageView uploadedImageView = findViewById(R.id.upload_done_image);
        final TextView uploadSpeed = findViewById(R.id.upload_speed_text);
        final ProgressBar progressBar = findViewById(R.id.upload_progress_bar);

        if(!uploadComplete){
            calculateButton.setEnabled(false);
            statusTextView.setText(R.string.upload_in_progress);
            uploadedImageView.setVisibility(View.GONE);
            progressBar.setProgress(0);

            final UploadStatusDelegate uploadStatusDelegate = new UploadStatusDelegate() {
                @Override
                public void onProgress(Context context, UploadInfo uploadInfo) {
                    uploadSpeed.setText(uploadInfo.getUploadRateString());
                    progressBar.setProgress(uploadInfo.getProgressPercent());
                }

                @Override
                public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                    final UploadStatusDelegate uploadStatusDelegate1 = this;
                    AlertDialog alertDialog = new AlertDialog.Builder(OnlineCalculationActivity.this).create();
                    alertDialog.setMessage(getResources().getString(R.string.upload_failed));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.retry),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    uploadFTP(OnlineCalculationActivity.this,filePath, uploadStatusDelegate1);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.leave),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    OnlineCalculationActivity.super.onPause();
                                    finishActivity();
                                }
                            });
                    alertDialog.show();
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    calculateButton.setEnabled(true);
                    statusTextView.setText(R.string.upload_complete);
                    uploadedImageView.setVisibility(View.VISIBLE);
                    uploadSpeed.setVisibility(View.GONE);
                    progressBar.setProgress(100);
                }

                @Override
                public void onCancelled(Context context, UploadInfo uploadInfo) {

                }
            };
            uploadFTP(this, filePath, uploadStatusDelegate);
        }
        else{
            calculateButton.setEnabled(true);
            statusTextView.setText(R.string.upload_complete);
            uploadedImageView.setVisibility(View.VISIBLE);
            uploadSpeed.setVisibility(View.GONE);
            progressBar.setProgress(100);
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            System.out.println("KEYCODE_HOME");
            showLeaveDialog();
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            System.out.println("KEYCODE_BACK");
            showLeaveDialog();
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_MENU)) {
            System.out.println("KEYCODE_MENU");
            showLeaveDialog();
            return true;
        }
        return false;
    }

    private void showLeaveDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.are_you_sure);
        alertDialog.setMessage(getResources().getString(R.string.all_progress_will_be_lost));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.stay),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.leave),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        OnlineCalculationActivity.super.onPause();
                        finishActivity();
                    }
                });
        alertDialog.show();
    }


    public void uploadFTP(final Context context, String filePath, UploadStatusDelegate uploadStatusDelegate) {
        try {
            String server = SharedPrefUtils.getStringData(context,Constants.FTP_SERVER,Constants.DEFAULT_FTP_SERVER);
            String[] words = server.split(":");
            String
             uploadId =
                    new FTPUploadRequest(context, words[0], Integer.parseInt(words[1]))
                            .setUsernameAndPassword("user", "1234")
                            .addFileToUpload( filePath,"/")
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(4)
                            .setDelegate(uploadStatusDelegate)
                            .startUpload();


        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    public void onCalculateClicked(View view) {
        TableButtonGroupLayout tableButtonGroupLayout = findViewById(R.id.metadata_radio_group);
        String metadata = getMetadata(tableButtonGroupLayout.getCheckedRadioButtonId());
        GetTonicDrone uploadFileToServer = new OnlineCalculationActivity.GetTonicDrone (OnlineCalculationActivity.this, fileName,metadata);
        uploadFileToServer.execute();
    }

    public static String getMetadata(int checkedRadioButtonId) {
        switch (checkedRadioButtonId){
            case R.id.metadata_female:
                return "2";
            case R.id.metadata_male:
                return "1";
            case R.id.metadata_instrument:
                return "3";
            case R.id.metadata_other:
                return "4";
        }
        return "4";
    }

    private void finishActivity(){
        finishing = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        UploadService.stopAllUploads();
        super.onDestroy();
    }



    public static class GetTonicDrone extends AsyncTask<Void, Integer, String> {

        private WeakReference<OnlineCalculationActivity> activityReference;
        String fileName;
        String metadata;
        String percentage;
        String seconds;
        String method;
        private ProgressDialog dialog;
        // only retain a weak reference to the activity
        GetTonicDrone (OnlineCalculationActivity context, String fileName, String metadata) {
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
            if (activity == null || activity.isFinishing()) return;

            Log.d("OnlineCalculationAct","start"+s+"end");
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            if(isInteger(s.trim())){
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

        private String calculateTonicOnline(String fileName, String metadata,  String percentage, String seconds, String method) {
            String responseString;

            try {
                String requestURL = "http://"+SharedPrefUtils.getStringData(activityReference.get(),Constants.SERVER, Constants.DEFAULT_SERVER)+"/getTonicDrone";
                MultiPartUtils multipart = new MultiPartUtils(requestURL);
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

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            Log.d("OnlineCalculationAct","i "+i+"char "+Character.digit(s.charAt(i),radix));
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}
