package iitm.speechlab.gettonicfromfile.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import com.sensorberg.permissionbitte.BitteBitte;
import com.sensorberg.permissionbitte.PermissionBitte;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.networkUtils.SharedPrefUtils;

public class ModeSelectionActivity extends AppCompatActivity {
    public static final int PICK_FILE_REQUEST = 1;
    String recordedFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);
        if(savedInstanceState!=null){
            recordedFilePath = savedInstanceState.getString("filepath");
        }
        updateSettingsText();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("filepath",recordedFilePath);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        askPermissions();
    }

    private void askPermissions() {
        if(PermissionBitte.shouldAsk(this, null)){
            PermissionBitte.ask(this, new BitteBitte() {
                @Override
                public void yesYouCan() {

                }

                @Override
                public void noYouCant() {
                    PermissionBitte.goToSettings(ModeSelectionActivity.this);
                }

                @Override
                public void askNicer() {
                    showPermissionsRequiredDialog();
                }
            });
        }
    }

    private void showPermissionsRequiredDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(ModeSelectionActivity.this).create();
        alertDialog.setMessage(getResources().getString(R.string.need_permissions));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.exit),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        askPermissions();
                    }
                });
        alertDialog.show();
    }

    public void onChooseFileClicked(View view){
        Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
        i2.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
        i2.putExtra(Constants.ALLOWED_FILE_EXTENSIONS, "wav");
        startActivityForResult(i2,PICK_FILE_REQUEST);
    }

    public void onRecordClicked(View view) {
        recordedFilePath = Environment.getExternalStorageDirectory() + "/recorded_audio_"+getTimeString()+".wav";
        int color = getResources().getColor(R.color.colorPrimaryDark);
        int requestCode = 0;
        AndroidAudioRecorder.with(this)
                .setFilePath(recordedFilePath)
                .setColor(color)
                .setRequestCode(requestCode)
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true)
                .record();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && data!=null) {
            if (resultCode == RESULT_OK) {
                Uri file = data.getData();
                startMainActivity(file);
            }
        }
        else if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri file = Uri.fromFile(new File(recordedFilePath));
                startMainActivity(file);
            } else if (resultCode == RESULT_CANCELED) {
                // Oops! User has canceled the recording
            }
        }
        else if(requestCode==1){
            updateSettingsText();
        }
    }

    protected boolean isSelectedModeOnline(){
        RadioGroup radioGroup = findViewById(R.id.radio_online_offline);
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.offline_button:
                return false;
            case R.id.online_button:
                return true;
        }
        return true;
    }
    protected void startMainActivity(Uri uri){
        if(isSelectedModeOnline()){
            Intent intent = new Intent(ModeSelectionActivity.this, OnlineCalculationActivity.class);
            intent.putExtra(iitm.speechlab.gettonicfromfile.Constants.URI,uri);
            startActivity(intent);
        }
    }

    private String getTimeString(){
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss_a", Locale.US);
        return ((format.format(today)).replaceAll("\\W+", "_"));
    }

    public void onChangeSettingsClicked(View view) {
        Intent intent = new Intent(ModeSelectionActivity.this, SettingsActivity.class);
        startActivityForResult(intent,1);
    }

    private void updateSettingsText(){
        TextView settingsText = findViewById(R.id.settings_text);
        String algoUsed = SharedPrefUtils.getStringData(this, iitm.speechlab.gettonicfromfile.Constants.ALGO, iitm.speechlab.gettonicfromfile.Constants.DEFAULT_METHOD);
        String secondsUsed = SharedPrefUtils.getStringData(this, iitm.speechlab.gettonicfromfile.Constants.NO_SECS,iitm.speechlab.gettonicfromfile.Constants.DEFAULT_SECONDS);
        String percentageFrames = SharedPrefUtils.getStringData(this, iitm.speechlab.gettonicfromfile.Constants.PERCENTAGE,iitm.speechlab.gettonicfromfile.Constants.DEFAULT_PERCENTAGE);
        settingsText.setText(getString(R.string.settings_text, algoUsed, secondsUsed, percentageFrames));
    }
}
