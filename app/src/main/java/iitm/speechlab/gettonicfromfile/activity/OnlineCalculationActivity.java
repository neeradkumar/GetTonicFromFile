package iitm.speechlab.gettonicfromfile.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
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

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;
import net.gotev.uploadservice.ftp.FTPUploadRequest;

import iitm.speechlab.gettonicfromfile.Constants;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.utils.AudioUtils;
import iitm.speechlab.gettonicfromfile.utils.SharedPrefUtils;
import iitm.speechlab.gettonicfromfile.views.TableButtonGroupLayout;

public class OnlineCalculationActivity extends AppCompatActivity {

    String fileName;
    boolean uploadComplete = false;
    String filePath;
    String trimmedFilePath;
    boolean finishing = false;
    public static final boolean isFtp = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_calculation);
        Uri file = getIntent().getParcelableExtra(Constants.URI);
        filePath = file.getPath();
        fileName = AudioUtils.getTrimmedFileName(this);
        trimmedFilePath = AudioUtils.getTrimmedFilePath(this);
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
                                    uploadFTP(OnlineCalculationActivity.this,trimmedFilePath, uploadStatusDelegate1, isFtp);
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
            String seconds = SharedPrefUtils.getStringData(this, Constants.NO_SECS, Constants.DEFAULT_SECONDS);
            new TrimAudioFile(this,filePath, trimmedFilePath, Integer.parseInt(seconds), uploadStatusDelegate).execute();
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


    public void uploadFTP(final Context context, String filePath, UploadStatusDelegate uploadStatusDelegate, boolean isFtp) {
        try {
            if(isFtp){
                String server = SharedPrefUtils.getStringData(context,Constants.FTP_SERVER,Constants.DEFAULT_FTP_SERVER);
                String[] words = server.split(":");
                new FTPUploadRequest(context, words[0], Integer.parseInt(words[1]))
                    .setUsernameAndPassword("user", "1234")
                    .addFileToUpload( filePath,"/")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(4)
                    .setDelegate(uploadStatusDelegate)
                    .startUpload();
            }
            else{
                String server = SharedPrefUtils.getStringData(context,Constants.FTP_SERVER,Constants.DEFAULT_FTP_SERVER);
                new MultipartUploadRequest(context, server)
                    .addFileToUpload(filePath,"file")

                    .setAutoDeleteFilesAfterSuccessfulUpload(true)
                    .setDelegate(uploadStatusDelegate)
                    .setMaxRetries(4)
                    .setNotificationConfig( new UploadNotificationConfig())
                    .startUpload();
            }
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    public void onCalculateClicked(View view) {
        TableButtonGroupLayout tableButtonGroupLayout = findViewById(R.id.metadata_radio_group);
        String metadata = getMetadata(tableButtonGroupLayout.getCheckedRadioButtonId());
        GetTonicDrone getTonicDrone = new GetTonicDrone (OnlineCalculationActivity.this, fileName,metadata);
        getTonicDrone.execute();
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

    protected void finishActivity(){
        finishing = true;
        UploadService.stopAllUploads();
        finish();
    }

    @Override
    protected void onDestroy() {
        UploadService.stopAllUploads();
        super.onDestroy();
    }
}
