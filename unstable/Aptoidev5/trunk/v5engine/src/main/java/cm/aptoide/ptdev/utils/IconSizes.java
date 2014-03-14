package cm.aptoide.ptdev.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 03-12-2013
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
public class IconSizes {

    static final private int baseLine = 96;
    static final private int baseLineAvatar = 150;
    private static float baseLineScreenshotLand = 256;
    private static float baseLineScreenshotPort = 96;



    public static String generateSizeString(Context context){
        int density = context.getResources().getDisplayMetrics().densityDpi;
        float densityMultiplier = context.getResources().getDisplayMetrics().density;

        if (densityMultiplier <= 0.75f) {
            densityMultiplier = 0.75f;
        } else if (densityMultiplier <= 1) {
            densityMultiplier = 1f;
        } else if (densityMultiplier <= 1.3125f) {
            densityMultiplier = 1.3125f;
        } else if (densityMultiplier <= 1.5f) {
            densityMultiplier = 1.5f;
        } else if (densityMultiplier <= 2f) {
            densityMultiplier = 2f;
        }else if (densityMultiplier <= 3f) {
            densityMultiplier = 3f;
        }
//        switch (density){
//            case 213:
//                densityMultiplier = 1.5f;
//                break;
//        }

        int size = (int) (baseLine * densityMultiplier);

        Log.d("Aptoide-IconSize", "Size is " + size);

        return size+"x"+size;
    }


    public static String generateSizeStringAvatar(Context context) {

        float densityMultiplier = context.getResources().getDisplayMetrics().density;

        if (densityMultiplier <= 0.75f) {
            densityMultiplier = 0.75f;
        } else if (densityMultiplier <= 1) {
            densityMultiplier = 1f;
        } else if (densityMultiplier <= 1.3125f) {
            densityMultiplier = 1.3125f;
        } else if (densityMultiplier <= 1.5f) {
            densityMultiplier = 1.5f;
        } else if (densityMultiplier <= 2f) {
            densityMultiplier = 2f;
        }else if (densityMultiplier <= 3f) {
            densityMultiplier = 3f;
        }

//        switch (density){
//            case 213:
//                densityMultiplier = 1.5f;
//                break;
//        }

        int size = Math.round(baseLineAvatar * densityMultiplier);

        Log.d("Aptoide-IconSize", "Size is " + size);

        return size+"x"+size;
    }

    public static String generateSizeStringScreenshots(Context context, String orient) {
        int density = context.getResources().getDisplayMetrics().densityDpi;
        float densityMultiplier = context.getResources().getDisplayMetrics().density;

        Log.d("Aptoide-IconSize", "Original mult is" + densityMultiplier);

        if (densityMultiplier <= 0.75f) {
            densityMultiplier = 0.75f;
        } else if (densityMultiplier <= 1) {
            densityMultiplier = 1f;
        } else if (densityMultiplier <= 1.333f) {
            densityMultiplier = 1.3312500f;
        } else if (densityMultiplier <= 1.5f) {
            densityMultiplier = 1.5f;
        } else if (densityMultiplier <= 2f) {
            densityMultiplier = 2f;
        }else if (densityMultiplier <= 3f) {
            densityMultiplier = 3f;
        }

        int size;
        if(orient.equals("portrait")){
            size = (int) (baseLineScreenshotPort * densityMultiplier);

        }else{

            size = (int) (baseLineScreenshotLand * densityMultiplier);

        }

        Log.d("Aptoide-IconSize", "Size is " + size + " baseline is " + baseLineScreenshotPort + " with multiplier " +densityMultiplier );

        return size+"x"+AptoideUtils.HWSpecifications.getDensityDpi(context);
    }



}
