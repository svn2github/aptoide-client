package cm.aptoidetv.pt;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

/**
 * Created by asantos on 19-11-2014.
 */
public class AppTV extends Application {
    static Context c;
    private static AptoideConfiguration configuration;
    private static boolean webInstallServiceRunning;

    public static Context getContext() {
        return c;
    }

    public static AptoideConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        c=this;
        configuration= new AptoideConfiguration();
        Crashlytics.start(this);
    }

    public static boolean isWebInstallServiceRunning() {
        return webInstallServiceRunning;
    }

    public static void setWebInstallServiceRunning(boolean webInstallServiceRunning) {
        AppTV.webInstallServiceRunning = webInstallServiceRunning;
    }
}
