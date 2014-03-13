package cm.aptoide.ptdev;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.preference.Preference;
import android.preference.PreferenceManager;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.database.DatabaseHelper;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FlushedInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import org.acra.*;
import org.acra.annotation.ReportsCrashes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import static org.acra.ReportField.*;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
@ReportsCrashes(
        formKey = "",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="mmorthemysisserealstandl",
        formUriBasicAuthPassword="2tERYIQeYVpC2Cpq8v35PQMb",
        // Your usual ACRA configuration

        formUri = "https://rmateus.cloudant.com/acra-aptoidev5-inhouse/_design/acra-storage/_update/report"
)
public class Aptoide extends Application {


    public static final boolean DEBUG_MODE = true;/**Log.isLoggable("Aptoide", Log.DEBUG);**/
    private static Context context;
    private static DatabaseHelper db;
    private static boolean webInstallServiceRunning;

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
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectAll()  // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build());


        ACRA.init(this);
        ACRAConfiguration acraConfiguration = ACRA.getNewDefaultConfig(this);
        try {
            acraConfiguration.setMode(ReportingInteractionMode.TOAST);
            acraConfiguration.setCustomReportContent(new ReportField[]{ APP_VERSION_CODE, APP_VERSION_NAME,
                    PACKAGE_NAME, BRAND, PRODUCT, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT, REPORT_ID, BUILD });
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }
        acraConfiguration.setResDialogText(R.string.crash_text);

        ACRA.setConfig(acraConfiguration);

        db = DatabaseHelper.getInstance(getApplicationContext());
        setConfiguration(getAptoideConfiguration());

        ManagerPreferences managerPreferences = new ManagerPreferences(this);

        bootImpl(managerPreferences);
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

                .discCache(new UnlimitedDiscCache(new File(getConfiguration().getPathCacheIcons()), generator))
                .imageDownloader(new ImageDownloaderWithPermissions(getContext(),managerPreferences))
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);



    }




    public void bootImpl(ManagerPreferences managerPreferences) {

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

        private int connectTimeout;
        private int readTimeout;
        private ManagerPreferences managerPreferences;

        public ImageDownloaderWithPermissions(Context context, ManagerPreferences managerPreferences) {
            this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
            this.managerPreferences = managerPreferences;
        }

        public ImageDownloaderWithPermissions(Context context, int connectTimeout, int readTimeout) {
            super(context, connectTimeout, readTimeout);

            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
        }

        @Override
        public InputStream getStream(String imageUri, Object extra) throws IOException {

            boolean download = AptoideUtils.NetworkUtils.isPermittedConnectionAvailable(context, managerPreferences.getIconDownloadPermissions());



            switch (Scheme.ofUri(imageUri)) {
                case HTTP:
                case HTTPS:
                    if(download){
                        return getStreamFromNetwork(imageUri, extra);
                    }
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