/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cm.aptoidetv.pt;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    /**
     * Returns the screen/display size
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return size;
    }

    /**
     * Shows a (long) toast
     */
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static String loadJSONFromResource( Context context, int resource ) {
        if( resource <= 0 )
            return null;

        String json = null;
        InputStream is = context.getResources().openRawResource( resource );
        try {
            if( is != null ) {
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                json = new String(buffer, "UTF-8");
            }
        } catch( IOException e ) {

        } finally {
            try {
                if( is != null )
                    is.close();
            } catch( IOException e ) {}
        }

        return json;
    }



    public static String getUserAgentString(Context mctx) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mctx);
        String myid = sPref.getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
        String myscr = sPref.getInt(EnumPreferences.SCREEN_WIDTH.name(), 0) + "x" + sPref.getInt(EnumPreferences.SCREEN_HEIGHT.name(), 0);
        String verString = null;
        try {
            verString = mctx.getPackageManager().getPackageInfo(mctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String extraId = mctx.getString(R.string.partner_id);

        return "aptoide-" + verString + ";" + HWSpecifications.TERMINAL_INFO + ";" + myscr + ";id:" + myid + ";" + "" + ";" + extraId ;
    }


    public static class HWSpecifications {


        private static String cpuAbi2;

        public static String getDeviceId(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

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

            DisplayMetrics metrics = new DisplayMetrics();
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
            }

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

        public static final String TERMINAL_INFO = getModel() + "("+ getProduct() + ")"+";v"+getRelease()+";"+System.getProperty("os.arch");

        public static String getProduct(){
            return Build.PRODUCT.replace(";", " ");
        }

        public static String getModel(){
            return Build.MODEL.replaceAll(";", " ");
        }


        public static String getRelease(){
            return Build.VERSION.RELEASE.replaceAll(";", " ");
        }

    }

    public static String screenshotToThumb(Context context, String imageUrl, String orientation) {

        String screen;
        String sizeString;

        if (imageUrl.contains("_screen")) {

            sizeString = IconSizes.generateSizeStringScreenshots(context, orientation);

            String[] splittedUrl = imageUrl.split("\\.(?=[^\\.]+$)");
            screen = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];

        } else {


            String[] splitedString = imageUrl.split("/");
            StringBuilder db = new StringBuilder();
            for (int i = 0; i != splitedString.length - 1; i++) {
                db.append(splitedString[i]);
                db.append("/");
            }
            db.append("thumbs/mobile/");
            db.append(splitedString[splitedString.length - 1]);
            screen = db.toString();
        }

        return screen;
    }

    public static class DateTimeUtils extends DateUtils {

        private static String mTimestampLabelYesterday;
        private static String mTimestampLabelToday;
        private static String mTimestampLabelJustNow;
        private static String mTimestampLabelMinutesAgo;
        private static String mTimestampLabelHoursAgo;
        private static String mTimestampLabelHourAgo;
        private static Context mCtx;
        private static DateTimeUtils instance;

        /**
         * Singleton contructor, needed to get access to the application context & strings for i18n
         *
         * @param context Context
         * @return DateTimeUtils singleton instanec
         * @throws Exception
         */
        public static DateTimeUtils getInstance(Context context) {
            mCtx = context;
            if (instance == null) {
                instance = new DateTimeUtils();
                mTimestampLabelYesterday = context.getResources().getString(R.string.WidgetProvider_timestamp_yesterday);
                mTimestampLabelToday = context.getResources().getString(R.string.WidgetProvider_timestamp_today);
                mTimestampLabelJustNow = context.getResources().getString(R.string.WidgetProvider_timestamp_just_now);
                mTimestampLabelMinutesAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_minutes_ago);
                mTimestampLabelHoursAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_hours_ago);
                mTimestampLabelHourAgo = context.getResources().getString(R.string.WidgetProvider_timestamp_hour_ago);
            }
            return instance;
        }

        /**
         * Checks if the given date is yesterday.
         *
         * @param date - Date to check.
         * @return TRUE if the date is yesterday, FALSE otherwise.
         */
        public static boolean isYesterday(long date) {

            final Calendar currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(date);

            final Calendar yesterdayDate = Calendar.getInstance();
            yesterdayDate.add(Calendar.DATE, -1);

            return yesterdayDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) && yesterdayDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR);
        }

        public static String[] weekdays = new DateFormatSymbols().getWeekdays(); // get day names
        public static final long millisInADay = 1000 * 60 * 60 * 24;


        /**
         * Displays a user-friendly date difference string
         *
         * @param timedate Timestamp to format as date difference from now
         * @return Friendly-formatted date diff string
         */
        public String getTimeDiffString(long timedate) {
            Calendar startDateTime = Calendar.getInstance();
            Calendar endDateTime = Calendar.getInstance();
            endDateTime.setTimeInMillis(timedate);
            long milliseconds1 = startDateTime.getTimeInMillis();
            long milliseconds2 = endDateTime.getTimeInMillis();
            long diff = milliseconds1 - milliseconds2;

            long hours = diff / (60 * 60 * 1000);
            long minutes = diff / (60 * 1000);
            minutes = minutes - 60 * hours;
            long seconds = diff / (1000);

            boolean isToday = DateTimeUtils.isToday(timedate);
            boolean isYesterday = DateTimeUtils.isYesterday(timedate);

            if (hours > 0 && hours < 12) {
                return hours == 1 ? String.format(mTimestampLabelHourAgo, hours) : String.format(mTimestampLabelHoursAgo, hours);
            } else if (hours <= 0) {
                if (minutes > 0)
                    return String.format(mTimestampLabelMinutesAgo, minutes);
                else {
                    return mTimestampLabelJustNow;
                }
            } else if (isToday) {
                return mTimestampLabelToday;
            } else if (isYesterday) {
                return mTimestampLabelYesterday;
            } else if (startDateTime.getTimeInMillis() - timedate < millisInADay * 6) {
                return weekdays[endDateTime.get(Calendar.DAY_OF_WEEK)];
            } else {
                return formatDateTime(mCtx, timedate, DateUtils.FORMAT_NUMERIC_DATE);
            }
        }

    }


    public static class Algorithms {

        public static String computeHmacSha1(String value, String keyString)
                throws InvalidKeyException, IllegalStateException,
                UnsupportedEncodingException, NoSuchAlgorithmException {
            System.out.println(value);
            System.out.println(keyString);
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"),
                    "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);

            byte[] bytes = mac.doFinal(value.getBytes("UTF-8"));

            return convToHex(bytes);

        }

        private static String convToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                int halfbyte = (data[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = data[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public static String computeSHA1sum(String text)
                throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash = new byte[40];
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            sha1hash = md.digest();
            return convToHex(sha1hash);
        }

        public static String computeSHA1sumFromBytes(byte[] bytes)
                throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash;
            md.update(bytes, 0, bytes.length);
            sha1hash = md.digest();
            return convToHex(sha1hash);
        }

        public static String md5Calc(File f) {
            int i;

            byte[] buffer = new byte[1024];
            int read = 0;
            String md5hash;
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                InputStream is = new FileInputStream(f);
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                byte[] md5sum = digest.digest();
                BigInteger bigInt = new BigInteger(1, md5sum);
                md5hash = bigInt.toString(16);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (md5hash.length() != 33) {
                String tmp = "";
                for (i = 1; i < (33 - md5hash.length()); i++) {
                    tmp = tmp.concat("0");
                }
                md5hash = tmp.concat(md5hash);
            }

            return md5hash;
        }

    }
}
