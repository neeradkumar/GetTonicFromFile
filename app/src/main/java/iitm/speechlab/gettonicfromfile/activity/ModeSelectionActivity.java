package iitm.speechlab.gettonicfromfile.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;

import iitm.speechlab.gettonicfromfile.R;

public class ModeSelectionActivity extends AppCompatActivity {
    public static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);
    }

    public void onChooseFileClicked(View view){
        Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
        i2.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
        i2.putExtra(Constants.ALLOWED_FILE_EXTENSIONS, "wav");
        startActivityForResult(i2,PICK_FILE_REQUEST);
    }

    public void onRecordClicked(View view) {

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
}
