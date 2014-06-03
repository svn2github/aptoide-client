package cm.aptoide.ptdev.downloadmanager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Base64;
import cm.aptoide.ptdev.utils.Filters;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-07-2013
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class Utils {


     public static String formatEta(long eta, String left){

        if (eta > 0) {
            long days = eta / (1000 * 60 * 60 * 24);
            eta -= days * 1000 * 60 * 60 * 24;
            long hours = eta / (1000 * 60 * 60);
            eta -= hours * 1000 * 60 * 60;
            long minutes = eta / (1000 * 60);
            eta -= minutes * 1000 * 60;
            long seconds = eta / 1000;

            String etaString = "";
            if (days > 0) {
                etaString += days +	"d ";
            }
            if (hours > 0) {
                etaString += hours + "h ";
            }
            if (minutes > 0) {
                etaString += minutes + "m ";
            }
            if (seconds > 0) {
                etaString += seconds + "s";
            }


            return etaString + " " + left;
        }
        return "";
    }

     public static String formatBits(long bytes) {
         int unit = 1024;
         if (bytes < unit) return bytes + " B";
         int exp = (int) (Math.log(bytes) / Math.log(unit));
         String pre = ("KMGTPE").charAt(exp-1)+"";
         return String.format(Locale.ENGLISH, "%.1f %sb", bytes / Math.pow(unit, exp), pre);
     }

    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1)+"";
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String filters(Context context) {

        int minSdk = AptoideUtils.HWSpecifications.getSdkVer();
        String minScreen = Filters.Screen.values()
                [AptoideUtils.HWSpecifications.getScreenSize(context)]
                .name()
                .toLowerCase(Locale.ENGLISH);
        String minGlEs = AptoideUtils.HWSpecifications.getGlEsVer(context);


        final int density = AptoideUtils.HWSpecifications.getDensityDpi(context);

        String cpuAbi = AptoideUtils.HWSpecifications.getCpuAbi();

        if(AptoideUtils.HWSpecifications.getCpuAbi2().length()>0){
            cpuAbi += ","+ AptoideUtils.HWSpecifications.getCpuAbi2();
        }
        int myversionCode = 0;
        PackageManager manager = context.getPackageManager();
         try {
             myversionCode = manager.getPackageInfo(context.getPackageName(),0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        String filters = (Build.DEVICE.equals("alien_jolla_bionic")?"apkdwn=myapp&":"")+"maxSdk="+minSdk+"&maxScreen="+minScreen+"&maxGles="+minGlEs+"&myCPU="+cpuAbi+"&myDensity="+density+"&myApt="+myversionCode;

        return Base64.encodeToString(filters.getBytes(), 0).replace("=","").replace("/","*").replace("+","_").replace("\n", "");

    }


}

