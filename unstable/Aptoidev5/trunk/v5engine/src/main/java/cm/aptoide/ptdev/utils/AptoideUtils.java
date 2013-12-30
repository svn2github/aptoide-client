package cm.aptoide.ptdev.utils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateUtils;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledAppsHelper;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-10-2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class AptoideUtils {

    private final static int sdk = HWSpecifications.getSdkVer();
    private final static String gles = HWSpecifications.getGlEsVer(Aptoide.getContext());
    private final static int screen = HWSpecifications.getScreenSize(Aptoide.getContext());
    private final static String screenSpec = HWSpecifications.getNumericScreenSize(Aptoide.getContext())+ "/" + HWSpecifications.getDensityDpi(Aptoide.getContext());
    private final static String cpu = HWSpecifications.getCpuAbi();
    private final static String cpu2 = HWSpecifications.getCpuAbi2();
    private final static String cpu3 = cpu2.equals("armeabi-v7a") ? "armeabi" : "";


    public static boolean isCompatible(Apk apk) {
        return apk.getMinSdk() <= sdk &&
                apk.getMinScreen().ordinal() <= screen &&
                Float.parseFloat(apk.getMinGlEs()) <= Float.parseFloat(gles) &&
                (apk.getScreenCompat() == null || apk.getScreenCompat().contains(screenSpec)) &&
                (apk.getCpuAbi()==null || checkCpuCompatibility(apk.getCpuAbi()));
    }

    private static boolean checkCpuCompatibility(String cpuAbi) {

        ArrayList<String> cpus = new ArrayList<String>(Arrays.asList(cpuAbi.split(",")));
        for(String cpuToCompare : cpus){
            if((cpuToCompare.equals(cpu) || (cpu2.length()>0 && cpuToCompare.equals(cpu2)) || (cpu3.length()>0 && cpuToCompare.equals(cpu3))))
                return true;
        }
        return false;
    }

    public static String checkStoreUrl(String uri_str){
        uri_str = uri_str.trim();
        if (!uri_str.contains(".")) {
            uri_str = uri_str.concat(".store.aptoide.com");
        }

        uri_str = RepoUtils.formatRepoUri(uri_str);

        if (uri_str.contains("bazaarandroid.com")) {
            uri_str = uri_str.replaceAll("bazaarandroid\\.com", "store.aptoide.com");
        }

        return uri_str;
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
            return context.getResources().getConfiguration().screenLayout& Configuration.SCREENLAYOUT_SIZE_MASK;
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

        public static final String TERMINAL_INFO = android.os.Build.MODEL + "("+ android.os.Build.PRODUCT + ")"
                +";v"+android.os.Build.VERSION.RELEASE+";"+System.getProperty("os.arch");

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

            return new String(convToHex(bytes));

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

    public static class NetworkUtils {


        private static int TIME_OUT = 15000;

        public BufferedInputStream getInputStream(String url, Context context) throws IOException {
            return getInputStream(url, null, null, context);
        }


        public BufferedInputStream getInputStream(String url, String username, String password, Context mctx) throws IOException {

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            if (username != null && password != null) {
                String basicAuth = "Basic " + new String(Base64.encode((username + ":" + password).getBytes(), Base64.NO_WRAP));
                connection.setRequestProperty("Authorization", basicAuth);
            }
            connection.setConnectTimeout(TIME_OUT);
            connection.setReadTimeout(TIME_OUT);
            connection.setRequestProperty("User-Agent", getUserAgentString(mctx));
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream(), 8 * 1024);




            

            return bis;

        }

        public static void setTimeout(int timeout) {
            NetworkUtils.TIME_OUT = timeout;
        }

        static void getIconSize(Context context){



        }

        public static int checkServerConnection(final String string, final String username, final String password) throws Exception {


                HttpURLConnection client = (HttpURLConnection) new URL(string
                        + "info.xml").openConnection();
                if (username != null && password != null) {
                    String basicAuth = "Basic "
                            + new String(Base64.encode(
                            (username + ":" + password).getBytes(),
                            Base64.NO_WRAP));
                    client.setRequestProperty("Authorization", basicAuth);
                }

                client.setRequestMethod("HEAD");
                client.setConnectTimeout(TIME_OUT);
                client.setReadTimeout(TIME_OUT);

            String contentType = client.getContentType();
            int responseCode = client.getResponseCode();
            client.disconnect();
                if (Aptoide.DEBUG_MODE)
                    Log.i("Aptoide-NetworkUtils-checkServerConnection", "Checking on: " + client.getURL().toString());
                if (contentType.equals("application/xml")) {
                    return 0;
                } else {
                    return responseCode;
                }
        }

        public JSONObject getJsonObject(String url, Context mctx) throws IOException, JSONException {
            String line = null;
            InputStreamReader reader = new java.io.InputStreamReader(getInputStream(url, null, null, mctx));
            BufferedReader br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + '\n');
            }

            br.close();

            return new JSONObject(sb.toString());

        }

        public static String getUserAgentString(Context mctx) {
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mctx);
            String myid = sPref.getString("myId", "NoInfo");
            String myscr = sPref.getInt("scW", 0) + "x" + sPref.getInt("scH", 0);
            String verString = null;
            try {
                verString = mctx.getPackageManager().getPackageInfo(mctx.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String partnerid = "";
//            if (Aptoide.PARTNERID != null) {
//                partnerid = "PartnerID:" + Aptoide.PARTNERID + ";";
//            }

            return "aptoide-" + verString + ";" + HWSpecifications.TERMINAL_INFO + ";" + myscr + ";id:" + myid + ";" + sPref.getString(Configs.LOGIN_USER_LOGIN, "") + ";" + partnerid;
        }


        public static boolean isConnectionAvailable(Context context) {
            ConnectivityManager connectivityState = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean connectionAvailable = false;
            try {
                connectionAvailable = connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
                Log.d("ManagerDownloads", "isConnectionAvailable mobile: " + connectionAvailable);
            } catch (Exception e) {
            }
            try {
                connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
                Log.d("ManagerDownloads", "isConnectionAvailable wifi: " + connectionAvailable);
            } catch (Exception e) {
            }
            try {
                connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
                Log.d("ManagerDownloads", "isConnectionAvailable wimax: " + connectionAvailable);
            } catch (Exception e) {
            }
            try {
                connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
                Log.d("ManagerDownloads", "isConnectionAvailable ethernet: " + connectionAvailable);
            } catch (Exception e) {
            }

            return connectionAvailable;
        }

        public static long getLastModified(URL url) throws IOException {

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIME_OUT);
            connection.setReadTimeout(TIME_OUT);
            long lastModified = connection.getLastModified();
            connection.disconnect();
            return lastModified;

        }


    }

    public static class RepoUtils {

        public static String split(String repo) {
            return repo.split("http://")[1].split("\\.store")[0].split("\\.bazaarandroid.com")[0];
        }

        public static String formatRepoUri(String uri_str) {
            if (uri_str.contains("http//")) {
                uri_str = uri_str.replaceFirst("http//", "http://");
            }

            if (uri_str.length() != 0 && uri_str.charAt(uri_str.length() - 1) != '/') {
                uri_str = uri_str + '/';
                Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
            }
            if (!uri_str.startsWith("http://")) {
                uri_str = "http://" + uri_str;
                Log.d("Aptoide-ManageRepo", "repo uri: " + uri_str);
            }
            return uri_str;
        }

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



    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }
    public static int getPixels(Context context, int dipValue){
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
        Log.d("getPixels", "" + px);
        return px;
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

    public static String getMyCountryCode(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage() + "_" + context.getResources().getConfiguration().locale.getCountry();
    }

    public static String getMyCountry(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    public static void syncInstalledApps(Context context, SQLiteDatabase db) {
        Log.d("Aptoide-InstalledSync", "Syncing");
        long startTime = System.currentTimeMillis();
        InstalledAppsHelper.sync(db, context);
        Log.d("Aptoide-InstalledSync", "Sync complete in " + (System.currentTimeMillis() - startTime)+"ms");

    }


    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
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
}

