package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cm.aptoide.ptdev.parser.Parser;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class MainService extends Service {


    Parser parser = new Parser();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Aptoide-MainService", "onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d("Aptoide-MainService", "onBind");

        return new MainServiceBinder();
    }

    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }











}
