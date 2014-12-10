package cm.aptoidetv.pt;

import android.app.Application;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created by asantos on 19-11-2014.
 */
public class AppTV extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(!PreferenceManager.getDefaultSharedPreferences(this).contains("Rooted")) {
            boolean isRooted = isRooted();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("Rooted",isRooted).commit();
        }
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    private static boolean findBinary(String binaryName) {
        boolean found = false;
        final String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;
                break;
            }
        }
        return found;
    }
}
