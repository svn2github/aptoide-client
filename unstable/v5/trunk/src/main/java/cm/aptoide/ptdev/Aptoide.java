package cm.aptoide.ptdev;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.DatabaseHelper;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.webservices.GetApkInfo;
import cm.aptoide.ptdev.webservices.Webservice;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.security.MessageDigest;
import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public class Aptoide extends Application {

    public static final boolean DEBUG_MODE = true;/**Log.isLoggable("Aptoide", Log.DEBUG);**/
    private Context context;
    private DatabaseHelper db;


    public SQLiteDatabase getDb() {
        return db.getWritableDatabase();
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        db = DatabaseHelper.getInstance(getApplicationContext());
        Ion.getDefault(getContext()).setLogging("MyLogs", Log.DEBUG);
        Ion.getDefault(getContext()).proxy("192.168.1.70", 8888);

        //Ion.with(this, "http://webservices.aptoide.com/webservices/checkUserCredentials/rfa.mateus@gmail.com/4b288f73587b1db7700c9661ce011e3b92b36443/json").proxy("192.168.1.70", 8888).asJsonObject();



    }

}
