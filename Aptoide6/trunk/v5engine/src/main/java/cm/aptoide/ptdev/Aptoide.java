package cm.aptoide.ptdev;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.database.DatabaseHelper;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import roboguice.util.temp.Ln;

public class Aptoide extends Application {

    public static boolean DEBUG_MODE = Log.isLoggable("APTOIDE", Log.DEBUG);
    private static Context context;
    private static DatabaseHelper db;
    private static boolean webInstallServiceRunning;
    private static String sponsoredCache;
    public static String iconSize;


    public static AptoideThemePicker getThemePicker() {
        return themePicker;
    }

    private static AptoideThemePicker themePicker;

    public static AptoideConfiguration getConfiguration() {
        return configuration;
    }

    private static AptoideConfiguration configuration = null;


    public static SQLiteDatabase getDb() {
        return db.getWritableDatabase();
    }

    public static Context getContext() {
        return context;
    }


    public static void setSponsoredCache(String sponsoredCache) {
        Aptoide.sponsoredCache = sponsoredCache;
    }

    public void setThemePicker(AptoideThemePicker themePicker) {
        Aptoide.themePicker = themePicker;
    }

    public void setConfiguration(AptoideConfiguration configuration) {
        Aptoide.configuration = configuration;
    }

    public static boolean IS_SYSTEM;


    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        try {
            IS_SYSTEM = (getPackageManager().getApplicationInfo(getPackageName(), 0).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        iconSize = IconSizes.generateSizeString(context);

        boolean debugmode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debugmode", false);
        DEBUG_MODE = DEBUG_MODE | debugmode;

        if(DEBUG_MODE){
            Toast.makeText(Aptoide.getContext(), "Debug mode is: " + Aptoide.DEBUG_MODE, Toast.LENGTH_LONG).show();
        }

        Ln.getConfig().setLoggingLevel(Log.VERBOSE);

        JodaTimeAndroid.init(this);


//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectAll()  // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build());

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {

                Log.e("AptoideCrashed","AptoideCrashed at: ", ex);

                ex.printStackTrace();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);

            }
        });

        setConfiguration(getAptoideConfiguration());


        db = DatabaseHelper.getInstance(getApplicationContext());
        initDatabase(db);


        ManagerPreferences managerPreferences = new ManagerPreferences(this);

        bootImpl(managerPreferences);


        managerPreferences.init();
        setThemePicker(getNewThemePicker());

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .showStubImage(R.drawable.icon_non_available)
                .build();


        FileNameGenerator generator = new FileNameGenerator() {

            @Override
            public String generate(String s) {

                if(s!=null){
                    return s.substring(s.lastIndexOf('/') + 1);
                }else{
                    return null;
                }
            }
        };

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())

                .discCache(new UnlimitedDiscCache(new File(getConfiguration().getPathCacheIcons()),null, generator))
                .imageDownloader(new ImageDownloaderWithPermissions(getContext()))
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }

    private void initDatabase(SQLiteOpenHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db!=null) db.rawQuery("pragma synchronous = 0", null);
    }

    public void bootImpl(ManagerPreferences managerPreferences) {
        Crashlytics.start(this);

        if (managerPreferences.getAptoideClientUUID() == null) {
            managerPreferences.createLauncherShortcut(getContext(), R.drawable.icon_brand_aptoide);
        }
    }

    public AptoideConfiguration getAptoideConfiguration() {
        return new AptoideConfiguration();
    }

    public AptoideThemePicker getNewThemePicker() {
        return new AptoideThemePicker();
    }

    public static class ImageDownloaderWithPermissions extends BaseImageDownloader{

        /** {@value} */
        public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
        /** {@value} */
        public static final int DEFAULT_HTTP_READ_TIMEOUT = 10 * 1000; // milliseconds

        public ImageDownloaderWithPermissions(Context context) {
            this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);

        }

        public ImageDownloaderWithPermissions(Context context, int connectTimeout, int readTimeout) {
            super(context, connectTimeout, readTimeout);
        }

        @Override
        public InputStream getStream(String imageUri, Object extra) throws IOException {

            boolean download = AptoideUtils.NetworkUtils.isIconDownloadPermitted(context);


            switch (Scheme.ofUri(imageUri)) {
                case HTTP:
                case HTTPS:
                    if(download){
                        return getStreamFromNetwork(imageUri, extra);
                    }
                    return null;
                case FILE:
                    return getStreamFromFile(imageUri, extra);
                case CONTENT:
                    return getStreamFromContent(imageUri, extra);
                case ASSETS:
                    return getStreamFromAssets(imageUri, extra);
                case DRAWABLE:
                    return getStreamFromDrawable(imageUri, extra);
                case UNKNOWN:
                default:
                    return getStreamFromOtherSource(imageUri, extra);
            }

        }

    }

    public static boolean isUpdate() throws PackageManager.NameNotFoundException {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("version", 0) < getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
    }
    
    public static boolean isWebInstallServiceRunning() {
        return webInstallServiceRunning;
    }

    public static void setWebInstallServiceRunning(boolean webInstallServiceRunning) {
        Aptoide.webInstallServiceRunning = webInstallServiceRunning;
    }

}