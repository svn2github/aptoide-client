package cm.aptoide.ptdev;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.DatabaseHelper;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ACRAConfigurationException;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


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

        formUri = "https://rmateus.cloudant.com/acra-aptoidev5/_design/acra-storage/_update/report"
)
public class Aptoide extends Application {


    public static final boolean DEBUG_MODE = true;/**Log.isLoggable("Aptoide", Log.DEBUG);**/
    private static Context context;
    private static DatabaseHelper db;


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

    public void setConfiguration(AptoideConfiguration configuration) {
        Aptoide.configuration = configuration;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        ACRA.init(this);
        ACRAConfiguration acraConfiguration = ACRA.getNewDefaultConfig(this);
        try {
            acraConfiguration.setMode(ReportingInteractionMode.TOAST);
        } catch (ACRAConfigurationException e) {
            e.printStackTrace();
        }
        acraConfiguration.setResDialogText(R.string.crash_text);

        ACRA.setConfig(acraConfiguration);

        db = DatabaseHelper.getInstance(getApplicationContext());

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .showStubImage(R.drawable.ic_launcher)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);

        ManagerPreferences managerPreferences = new ManagerPreferences(this);

        bootImpl(managerPreferences);
        setConfiguration(getAptoideConfiguration());

        //Ion.with(this, "http://webservices.aptoide.com/webservices/checkUserCredentials/rfa.mateus@gmail.com/4b288f73587b1db7700c9661ce011e3b92b36443/json").proxy("192.168.1.70", 8888).asJsonObject();
    }



    public void bootImpl(ManagerPreferences managerPreferences) {

    }

    public AptoideConfiguration getAptoideConfiguration() {
        return new AptoideConfiguration();
    }

}