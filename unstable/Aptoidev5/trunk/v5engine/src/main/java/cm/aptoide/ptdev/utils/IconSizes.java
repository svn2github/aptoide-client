package cm.aptoide.ptdev.utils;

import android.content.Context;
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


}
