package cm.aptoide.ptdev.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.Parser;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.callbacks.PoolEndedCallback;
import cm.aptoide.ptdev.parser.handlers.HandlerInfoXml;
import cm.aptoide.ptdev.parser.handlers.HandlerLatestXml;
import cm.aptoide.ptdev.parser.handlers.HandlerTopXml;
import com.octo.android.robospice.SpiceManager;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class ParserService extends Service {

    private SpiceManager spiceManager = new SpiceManager(ParserHttp.class);


    Parser parser;
    private boolean stopAfterComplete = false;
    private boolean alreadyStopped;
    private ErrorCallback errorCallback;
    private CompleteCallback parserCompleteCallback;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Aptoide-ParserService", "onStart");
    }




    @Override
    public IBinder onBind(Intent intent) {

        parser = new Parser(spiceManager);
        parser.setPoolEndCallback(new PoolEndedCallback() {
            @Override
            public void onEnd() {
                Log.d("Aptoide-", "onEnd");
                spiceManager.shouldStop();
                stopForeground(true);

            }
        });

        return new MainServiceBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Aptoide-ParserService", "onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public Parser getParser() {
        return parser;
    }



    public Notification createDefaultNotification() {
        Notification notification = new Notification();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            notification.icon = getApplicationInfo().icon;
            //temporary fix https://github.com/octo-online/robospice/issues/200
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(this, "", "", pendingIntent);
        } else {
            notification.icon = 0;
        }
        notification.tickerText = null;
        notification.when = System.currentTimeMillis();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = Notification.PRIORITY_MIN;
        }
        return notification;
    }


    public void setParser(Parser parser) {
        this.parser = parser;
    }



    public void get(Database db, Store store) {
        if(!spiceManager.isStarted()){
            Log.d("Aptoide-Parser", "Starting spice");
            spiceManager.start(getApplicationContext());
        }

        startForeground(45, createDefaultNotification());
        long id = insertStoreDatabase(db, store);
        BusProvider.getInstance().post(produceRepoAddedEvent());
        parser.parse(store.getLatestXmlUrl(),  4, new HandlerLatestXml(db, id));
        parser.parse(store.getTopXmlUrl(),     4, new HandlerTopXml(db, id));
        parser.parse(store.getInfoXmlUrl(),   10, new HandlerInfoXml(db, id), errorCallback, parserCompleteCallback);


        //parser.parse("http://onairda.store.aptoide.com/" + "info.xml", 10, new HandlerInfoXml(db, 2));
        //parser.parse("http://onairda.store.aptoide.com/" + "latest.xml", 4, new HandlerLatestXml(db, 2));

        //parser.parse("http://m3taxx.store.aptoide.com/" + "info.xml", 10, new HandlerInfoXml(db, 3));
        //parser.parse("http://m3taxx.store.aptoide.com/" + "latest.xml", 4, new HandlerLatestXml(db, 3));


        //parser.parse("http://savou.store.aptoide.com/" + "info.xml", 10, new HandlerInfoXml(db , 4));
        //parser.parse("http://moustwantedapps.store.aptoide.com/" + "info.xml", 10, new HandlerInfoXml(db, 5));
        //parser.parse("http://htcsense.store.aptoide.com/" + "info.xml", 10, new HandlerInfoXml(db, 6));


        //parser.parse(url + "latest.xml", 5, new HandlerLatestXml(db, repoId));
        //parser.parse(url + "top.xml", 5, new HandlerTopXml(db, repoId));


    }
    public RepoAddedEvent produceRepoAddedEvent() {
        return new RepoAddedEvent();
    }

    private long insertStoreDatabase(Database db, Store store) {
        return db.insertStore(store);
    }

    public void setCallbacks(ErrorCallback errorCallback, CompleteCallback parserCompleteCallback) {

        this.errorCallback = errorCallback;
        this.parserCompleteCallback = parserCompleteCallback;

    }


    public class MainServiceBinder extends Binder {
        public ParserService getService() {
            return ParserService.this;
        }
    }











}
