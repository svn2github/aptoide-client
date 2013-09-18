package cm.aptoide.pt;


import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class HWSpecifications{


    private static String cpuAbi2;

    /**
	 * @return the sdkVer
	 */

	static public int getSdkVer() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * @return the screenSize
	 */
	static public int getScreenSize(Context context) {
		return context.getResources().getConfiguration().screenLayout&Configuration.SCREENLAYOUT_SIZE_MASK;
	}

    static public int getNumericScreenSize(Context context) {
        int size = context.getResources().getConfiguration().screenLayout&Configuration.SCREENLAYOUT_SIZE_MASK;
        return (size + 1) * 100;
    }


        /**
         * @return the esglVer
         */
	static public String getGlEsVer(Context context) {
		return ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion();
	}


    public static int getDensityDpi(Context context) {

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        return metrics.densityDpi;
    }

    public static String getCpuAbi() {
        return Build.CPU_ABI;
    }

    public static String getCpuAbi2() {

        if(getSdkVer()>=8 && !Build.CPU_ABI2.equals(Build.UNKNOWN)){
            return Build.CPU_ABI2;
        }else{
            return "";
        }

    }
}
