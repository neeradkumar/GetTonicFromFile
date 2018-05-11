package iitm.speechlab.gettonicfromfile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import iitm.speechlab.gettonicfromfile.Constants;
import iitm.speechlab.gettonicfromfile.R;
import iitm.speechlab.gettonicfromfile.utils.SharedPrefUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        String algoUsed = SharedPrefUtils.getStringData(this, Constants.ALGO, Constants.DEFAULT_METHOD);
        String secondsUsed = SharedPrefUtils.getStringData(this,Constants.NO_SECS,Constants.DEFAULT_SECONDS);
        String percentageFrames = SharedPrefUtils.getStringData(this,Constants.PERCENTAGE,Constants.DEFAULT_PERCENTAGE);

        final Spinner algoSpinner = findViewById(R.id.algo_spinner);
        final Spinner secondsSpinner = findViewById(R.id.seconds_spinner);
        final Spinner percetageSpinner = findViewById(R.id.percentage_spinner);

        selectSpinnerItemByValue(algoSpinner, algoUsed);
        selectSpinnerItemByValue(secondsSpinner, secondsUsed);
        selectSpinnerItemByValue(percetageSpinner, percentageFrames);

        EditText server = findViewById(R.id.server_edit_text);
        server.setText(SharedPrefUtils.getStringData(this,Constants.SERVER,Constants.DEFAULT_SERVER));

        EditText ftpServer = findViewById(R.id.ftp_server_edit_text);
        ftpServer.setText(SharedPrefUtils.getStringData(this,Constants.FTP_SERVER,Constants.DEFAULT_FTP_SERVER));

        algoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPrefUtils.saveData(SettingsActivity.this,Constants.ALGO, (String)algoSpinner.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        secondsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPrefUtils.saveData(SettingsActivity.this,Constants.NO_SECS, (String)secondsSpinner.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        percetageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPrefUtils.saveData(SettingsActivity.this,Constants.PERCENTAGE, (String)percetageSpinner.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public static void selectSpinnerItemByValue(Spinner spnr, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if(value.equals(adapter.getItem(position))) {
                spnr.setSelection(position);
                return;
            }
        }
    }

    public void updateServer(View view) {
        String server = ((EditText)findViewById(R.id.server_edit_text)).getText().toString();
        String ftpServer = ((EditText)findViewById(R.id.ftp_server_edit_text)).getText().toString();

        SharedPrefUtils.saveData(SettingsActivity.this,Constants.SERVER,server);
        SharedPrefUtils.saveData(SettingsActivity.this,Constants.FTP_SERVER,ftpServer);
        Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
    }
}
