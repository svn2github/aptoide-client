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


        switch (density){
            case 213:
                densityMultiplier = 1.5f;
                break;
        }

        int size = (int) (baseLine * densityMultiplier);

        Log.d("Aptoide-IconSize", "Size is " + size);

        return size+"x"+size;
    }


    public static String generateSizeStringAvatar(Context context) {
        int density = context.getResources().getDisplayMetrics().densityDpi;
        float densityMultiplier = context.getResources().getDisplayMetrics().density;


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


//        switch (density){
//            case 213:
//                densityMultiplier = 1.5f;
//                break;
//        }


        int size;
        if(orient.equals("portrait")){
            size = Math.round(baseLineScreenshotPort * densityMultiplier);

        }else{

            size = Math.round(baseLineScreenshotLand * densityMultiplier);

        }

        Log.d("Aptoide-IconSize", "Size is " + size);

        return size+"x"+AptoideUtils.HWSpecifications.getDensityDpi(context);
    }



}
