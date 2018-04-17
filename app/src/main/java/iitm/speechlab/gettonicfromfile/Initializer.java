package iitm.speechlab.gettonicfromfile;

import android.app.Application;

import net.gotev.uploadservice.UploadService;

public class Initializer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // setup the broadcast action namespace string which will
        // be used to notify upload status
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

    }
}
