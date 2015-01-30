package cm.aptoidetv.pt;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.content.res.Configuration;

import java.util.Locale;

public class Filters {

    public static class HWSpecifications {

/*

        private static String cpuAbi2;

        public static String getDeviceId(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
*/

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
            return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
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

/*            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);


            int dpi = metrics.densityDpi;


            if(dpi <= 120){
                dpi = 120;
            }else if(dpi <= 160){
                dpi = 160;
            }else if(dpi <= 213){
                dpi = 213;
            }else if(dpi <= 240){
                dpi = 240;
            }else if(dpi <= 320){
                dpi = 320;
            }else{
                dpi = 480;
            }*/
            int dpi=480;
            return dpi;
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

        public static final String TERMINAL_INFO = getModel() + "("+ getProduct() + ")"
                +";v"+getRelease()+";"+System.getProperty("os.arch");

        public static String getProduct(){
            return android.os.Build.PRODUCT.replace(";", " ");
        }

        public static String getModel(){
            return android.os.Build.MODEL.replaceAll(";", " ");
        }


        public static String getRelease(){
            return android.os.Build.VERSION.RELEASE.replaceAll(";", " ");
        }

    }
    public static String filters(Context context) {



        int minSdk = HWSpecifications.getSdkVer();
        String minScreen = Filters.Screen.values()
                [HWSpecifications.getScreenSize(context)]
                .name()
                .toLowerCase(Locale.ENGLISH);
        String minGlEs = HWSpecifications.getGlEsVer(context);


        final int density = HWSpecifications.getDensityDpi(context);

        String cpuAbi = HWSpecifications.getCpuAbi();

        if (HWSpecifications.getCpuAbi2().length() > 0) {
            cpuAbi += "," + HWSpecifications.getCpuAbi2();
        }

        String filters = "maxSdk=" + minSdk + "&maxScreen=" + minScreen + "&maxGles=" + minGlEs + "&myCPU=" + cpuAbi + "&myDensity=" + density;

        return Base64.encodeToString(filters.getBytes(), 0).replace("=", "").replace("/", "*").replace("+", "_").replace("\n", "");
    }

    public static enum Screen {
        notfound,small,normal,large,xlarge;

        public static Screen lookup(String screen){
            try{
                return valueOf(screen);
            }catch (Exception e) {
                return notfound;
            }


        }

    }

    public static enum Age {
        All,Mature;
        public static Age lookup(String age){
            try{
                return valueOf(age);
            }catch (Exception e) {
                return All;
            }


        }
    }
}

