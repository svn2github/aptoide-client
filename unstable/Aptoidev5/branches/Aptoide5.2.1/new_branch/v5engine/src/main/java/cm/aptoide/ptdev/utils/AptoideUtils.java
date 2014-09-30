package cm.aptoide.ptdev.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
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
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledAppsHelper;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.DownloadPermissions;
import cm.aptoide.ptdev.model.Error;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.webservices.Errors;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_DUN;
import static android.net.ConnectivityManager.TYPE_MOBILE_HIPRI;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.ConnectivityManager.TYPE_MOBILE_SUPL;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static com.mopub.mobileads.AdUrlGenerator.MoPubNetworkType.ETHERNET;
import static com.mopub.mobileads.AdUrlGenerator.MoPubNetworkType.MOBILE;
import static com.mopub.mobileads.AdUrlGenerator.MoPubNetworkType.UNKNOWN;
import static com.mopub.mobileads.AdUrlGenerator.MoPubNetworkType.WIFI;

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


    public static boolean isLoggedIn(Context context) {
        AccountManager manager = AccountManager.get(context);

        return manager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length != 0;
    }
    public static Account getUser(Context context) {
        AccountManager manager = AccountManager.get(context);

        Account[] accounts = manager.getAccountsByType(Aptoide.getConfiguration().getAccountType());
        if(accounts.length != 0)
            return accounts[0];
        return null;
    }

    public static boolean isLoggedInOrAsk(Activity activity) {
        final AccountManager manager = AccountManager.get(activity);

        if (manager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length == 0) {
            manager.addAccount(Aptoide.getConfiguration().getAccountType(),
                    AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, activity,null, null);

            return false;
        }
        return true;
    }

    public static boolean isCompatible(Apk apk) {
        return apk.getMinSdk() <= sdk &&
                apk.getMinScreen().ordinal() <= screen &&
                Float.parseFloat(apk.getMinGlEs()) <= Float.parseFloat(gles) &&
                (apk.getScreenCompat() == null || apk.getScreenCompat().contains(screenSpec)) &&
                (apk.getCpuAbi()==null || checkCpuCompatibility(apk.getCpuAbi()));
    }

    public static SharedPreferences getSharedPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
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

    public static void toastError(List<Error> errors) {
        for (Error error: errors){
            String localizedError = Aptoide.getContext().getString(Errors.getErrorsMap().get(error.getCode()));
            if(localizedError==null){
                localizedError = error.getMsg();
            }
            if (localizedError!=null) Toast.makeText(Aptoide.getContext(), localizedError, Toast.LENGTH_LONG).show();
        }
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



        private static int getActiveNetworkType(Context mContext) {

            ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);


            if (mContext.checkCallingOrSelfPermission(ACCESS_NETWORK_STATE) == PERMISSION_GRANTED) {
                NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null ? activeNetworkInfo.getType() : -1;
            }
            return -1;
        }

        public static enum NetworkType {
            UNKNOWN,
            ETHERNET,
            WIFI,
            MOBILE;

            @Override
            public String toString() {
                return super.toString().toLowerCase(Locale.ENGLISH);
            }
        }



        public static NetworkType getConnectionType(){
            Context context = Aptoide.getContext();
            int type = getActiveNetworkType(context);

            switch(type) {
                case TYPE_ETHERNET:
                    return NetworkType.ETHERNET;
                case TYPE_WIFI:
                    return NetworkType.WIFI;
                case TYPE_MOBILE:
                case TYPE_MOBILE_DUN:
                case TYPE_MOBILE_HIPRI:
                case TYPE_MOBILE_MMS:
                case TYPE_MOBILE_SUPL:
                    return NetworkType.MOBILE;
                default:
                    return NetworkType.UNKNOWN;
            }


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

            String extraId = Aptoide.getConfiguration().getExtraId();

            return "aptoide-" + verString + ";" + HWSpecifications.TERMINAL_INFO + ";" + myscr + ";id:" + myid + ";" + sPref.getString(Configs.LOGIN_USER_LOGIN, "") + ";" + extraId;
        }

/*
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
        }*/

        public static boolean isIconDownloadPermitted(Context context){
            return isPermittedConnectionAvailable(context,
                    new DownloadPermissions(
                            getSharedPreferences().getBoolean("wifi", true),
                            getSharedPreferences().getBoolean("ethernet", true),
                            getSharedPreferences().getBoolean("4g", true),
                            getSharedPreferences().getBoolean("3g", true)));
        }
        public static boolean isGeneral_DownloadPermitted(Context context){
            return isPermittedConnectionAvailable(context,
                    new DownloadPermissions(
                    getSharedPreferences().getBoolean("generalnetworkwifi", true),
                    getSharedPreferences().getBoolean("generalnetworkethernet", true),
                    getSharedPreferences().getBoolean("generalnetwork4g", true),
                    getSharedPreferences().getBoolean("generalnetwork3g", true)));
        }
        private static boolean isPermittedConnectionAvailable(Context context, DownloadPermissions permissions){
            ConnectivityManager connectivityState = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean connectionAvailable = false;
            try {
                if(permissions.isWiFi()){
                    connectionAvailable = connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
                    if(connectionAvailable) {
                        return true;
                    }
                }
                if(permissions.isWiMax()){
                    connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
                }
                if(permissions.isMobile()){
                    connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
                }
                if(permissions.isEthernet()){
                    connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
                }
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
            Log.d("Aptoide-RepoUtils", "Splitting " + repo);
            repo = formatRepoUri(repo);
            return repo.split("http://")[1].split("\\.store")[0].split("\\.bazaarandroid.com")[0];
        }

        public static String formatRepoUri(String uri_str) {

            uri_str = uri_str.toLowerCase(Locale.ENGLISH);

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



    public static class DateDiffUtils{

        final static int MONTHS = 0;
        final static int DAYS = 1;
        final static int HOURS = 2;
        final static int MINUTES = 3;
        final static int SECONDS = 4;

        public static String getDiffDate(Context context, Date date){

            int TIME_TYPE = MONTHS;
            DateTime startDate = new DateTime(date);
            DateTime now = DateTime.now();

            int time = Months.monthsBetween(startDate, now).getMonths();

            if(time == 0){
                TIME_TYPE = DAYS;
                time = Days.daysBetween(startDate, now).getDays();
            }

            if(time == 0){
                TIME_TYPE = HOURS;
                time = Hours.hoursBetween(startDate, now).getHours();
            }

            if(time == 0){
                TIME_TYPE = MINUTES;
                time = Minutes.minutesBetween(startDate, now).getMinutes();
            }

            if(time == 0){
                TIME_TYPE = SECONDS;
                time = Seconds.secondsBetween(startDate, now).getSeconds();
            }


            if(time == 1){
                return getSingleUnitStringBasedOnType(context, TIME_TYPE);
            }else{
                return getStringBasedOnType(context, TIME_TYPE, time);
            }


        }

        private static String getSingleUnitStringBasedOnType(Context context, int type){

            switch (type){
                case MONTHS:
                    return context.getString(R.string.timestamp_month, 1);
                case DAYS:
                    return context.getString(R.string.timestamp_day, 1);
                case HOURS:
                    return context.getString(R.string.WidgetProvider_timestamp_hour_ago, 1);
                case MINUTES:
                    return context.getString(R.string.WidgetProvider_timestamp_just_now);
                case SECONDS:
                    return context.getString(R.string.WidgetProvider_timestamp_just_now);
            }
            return null;
        }

        private static String getStringBasedOnType(Context context, int type, int time){

            switch (type){
                case MONTHS:
                    return context.getString(R.string.timestamp_months, time);
                case DAYS:
                    return context.getString(R.string.timestamp_days, time);
                case HOURS:
                    return context.getString(R.string.WidgetProvider_timestamp_hours_ago, time);
                case MINUTES:
                    return context.getString(R.string.WidgetProvider_timestamp_minutes_ago, time);
                case SECONDS:
                    return context.getString(R.string.WidgetProvider_timestamp_just_now);
            }
            return null;
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
        //Log.d("Aptoide-InstalledSync", "Syncing");
        long startTime = System.currentTimeMillis();
        InstalledAppsHelper.sync(db, context);
        //Log.d("Aptoide-InstalledSync", "Sync complete in " + (System.currentTimeMillis() - startTime)+"ms");

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

    public static String withSuffix(String input) {
        long count = Long.parseLong(input);
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
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

    public static long[] LongListtolongArray(List<Long> LongList){
        int s = LongList.size();
        long[] ret = new long[s];
        for(int i=0;i<s;i++){
            ret[i]=LongList.get(i);
        }
        return ret;
    }
}

