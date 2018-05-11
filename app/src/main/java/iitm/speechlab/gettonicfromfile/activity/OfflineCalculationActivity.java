package iitm.speechlab.gettonicfromfile.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;

import java.lang.ref.WeakReference;

import iitm.speechlab.gettonicfromfile.Constants;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.utils.AudioUtils;
import iitm.speechlab.gettonicfromfile.utils.SharedPrefUtils;
import iitm.speechlab.gettonicfromfile.views.TableButtonGroupLayout;

import static iitm.speechlab.gettonicfromfile.activity.OnlineCalculationActivity.getMetadata;
import static iitm.speechlab.gettonicfromfile.utils.Utils.isInteger;

public class OfflineCalculationActivity extends AppCompatActivity {

    String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_calculation);

        Uri file = getIntent().getParcelableExtra(Constants.URI);
        filePath = file.getPath();

        //set default as male
        RadioButton maleRadioButton = findViewById(R.id.metadata_male);
        TableButtonGroupLayout tableButtonGroupLayout = findViewById(R.id.metadata_radio_group);
        tableButtonGroupLayout.setChecked(maleRadioButton);

    }



    public void onCalculateClickedOffline(View view) {
        TableButtonGroupLayout tableButtonGroupLayout = findViewById(R.id.metadata_radio_group);
        String metadata = getMetadata(tableButtonGroupLayout.getCheckedRadioButtonId());
        OfflineCalculationActivity.GetTonicDrone calculateTonic  = new OfflineCalculationActivity.GetTonicDrone (OfflineCalculationActivity.this, filePath,metadata);
        calculateTonic.execute();
    }

    public static class GetTonicDrone extends AsyncTask<Void, Integer, String> {

        private WeakReference<OfflineCalculationActivity> activityReference;
        String fileName;
        String metadata;
        String percentage;
        String seconds;
        String method;
        private ProgressDialog dialog;
        // only retain a weak reference to the activity
        GetTonicDrone (OfflineCalculationActivity context, String fileName, String metadata) {
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
            OfflineCalculationActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            dialog.setMessage(activity.getResources().getString(R.string.calculating_wait));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            return calculateTonicOffline(fileName,metadata, percentage, seconds, method);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog.isShowing()) dialog.dismiss();
            // get a reference to the activity if it is still there
            final OfflineCalculationActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;


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

                            activity.finish();
                        }
                    });
            alertDialog.show();

        }

        private String calculateTonicOffline(String fileName, String metadata,  String percentage, String seconds, String method) {
            String responseString;
            try{
                int methodInt = 2;
                if("Peak picking method".equals(method)) methodInt =1;
                responseString = ""+ AudioUtils.getTonicDrone(fileName,Integer.parseInt(metadata),Integer.parseInt(seconds), Integer.parseInt(percentage),methodInt);
            } catch (Exception e) {
                responseString = e.toString();
            }
            return responseString;
        }
    }
}
