package iitm.speechlab.gettonicfromfile.networkUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefUtils {

    private static final String PREF_APP = "pref_app";

    // Get Data
    static public String getStringData(Context context, String key, String defaultValue) {
        return context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).getString(key, defaultValue);
    }


    // Save Data
    static public void saveData(Context context, String key, String val) {
        context.getSharedPreferences(PREF_APP, Context.MODE_PRIVATE).edit().putString(key, val).apply();
    }


    static public SharedPreferences.Editor getSharedPrefEditor(Context context, String pref) {
        return context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit();
    }

    static public void saveData(Editor editor) {
        editor.apply();
    }

    private SharedPrefUtils() {
        throw new UnsupportedOperationException(
                "Should not create instance of Util class. Please use as static..");
    }
}
