package iitm.speechlab.gettonicfromfile.utils;

import android.util.Log;

public class Utils {

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}
