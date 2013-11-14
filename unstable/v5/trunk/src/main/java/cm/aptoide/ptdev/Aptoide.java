package cm.aptoide.ptdev;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cm.aptoide.ptdev.database.DatabaseHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */

public class Aptoide extends Application {


    public static final boolean DEBUG_MODE = true;/**Log.isLoggable("Aptoide", Log.DEBUG);**/
    private static Context context;
    private static DatabaseHelper db;


    public static SQLiteDatabase getDb() {

        return db.getWritableDatabase();

    }

    public static Context getContext() {

        return context;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        db = DatabaseHelper.getInstance(getApplicationContext());

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);

        //Ion.with(this, "http://webservices.aptoide.com/webservices/checkUserCredentials/rfa.mateus@gmail.com/4b288f73587b1db7700c9661ce011e3b92b36443/json").proxy("192.168.1.70", 8888).asJsonObject();

    }

}