package cm.aptoide.ptdev.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import cm.aptoide.ptdev.MainActivity;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.Parser;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.callbacks.PoolEndedCallback;
import cm.aptoide.ptdev.parser.events.StopParseEvent;
import cm.aptoide.ptdev.parser.handlers.*;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class ParserService extends Service implements ErrorCallback, CompleteCallback{

    Parser parser;
    private SpiceManager spiceManager = new SpiceManager(ParserHttp.class);
    private boolean stopAfterComplete = false;
    private boolean alreadyStopped;



    @Override
    public void onCreate() {
        super.onCreate();
        BusProvider.getInstance().register(this);
        Log.d("Aptoide-ParserService", "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        Log.d("Aptoide-ParserService", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {

        parser = new Parser(spiceManager);
        parser.setPoolEndCallback(new PoolEndedCallback() {
            @Override
            public void onEnd() {
                Log.d("Aptoide-", "onEnd");
                try {
                    spiceManager.shouldStopAndJoin(DurationInMillis.ONE_MINUTE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopForeground(true);
                stopSelf();
            }
        });

        return new MainServiceBinder();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }


    public boolean repoIsParsing(long id){
        return handlerBundleSparseArray.get((int) id)!=null;
    }

    SparseArray<HandlerBundle> handlerBundleSparseArray = new SparseArray<HandlerBundle>();

    public void startParse(Database db, Store store) {
        if (!spiceManager.isStarted()) {
            Log.d("Aptoide-Parser", "Starting spice");
            spiceManager.start(getApplicationContext());
        }
        startService(new Intent(getApplicationContext(), ParserService.class));
        startForeground(45, createDefaultNotification());
        long id = insertStoreDatabase(db, store);
        BusProvider.getInstance().post(produceRepoAddedEvent());
        HandlerLatestXml handlerLatestXml = new HandlerLatestXml(db, id);
        HandlerTopXml handlerTopXml = new HandlerTopXml(db, id);
        HandlerInfoXml handlerInfoXml = new HandlerInfoXml(db, id);

        HandlerBundle bundle = new HandlerBundle(handlerInfoXml, handlerTopXml, handlerLatestXml );

        parser.parse(store.getLatestXmlUrl(), store.getLogin(), 4, handlerLatestXml);
        parser.parse(store.getTopXmlUrl(), store.getLogin(), 4, handlerTopXml);
        parser.parse(store.getInfoXmlUrl(), store.getLogin(), 10, handlerInfoXml, this, this);
        handlerBundleSparseArray.append((int) id, bundle);
    }
    @Subscribe
    public void cancelRepo(StopParseEvent event){
        HandlerBundle bundle = handlerBundleSparseArray.get((int) event.getRepoId());

        if(bundle!=null){
            bundle.cancel();
        }

    }

    private long insertStoreDatabase(Database db, Store store) {
        return db.insertStore(store);
    }

    public RepoAddedEvent produceRepoAddedEvent() {
        return new RepoAddedEvent();
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

    @Override
    public void onComplete(long repoId) {
        handlerBundleSparseArray.remove((int) repoId);

        BusProvider.getInstance().post(new RepoCompleteEvent(repoId));

    }

    @Override
    public void onError(Exception e, long repoId) {
        handlerBundleSparseArray.remove((int) repoId);
        BusProvider.getInstance().post(new RepoErrorEvent(e, repoId));
    }

    public void parseEditorsChoice(Database db, String url) {
        if (!spiceManager.isStarted()) {
            Log.d("Aptoide-Parser", "Starting spice");
            spiceManager.start(getApplicationContext());
        }
        startService(new Intent(getApplicationContext(), ParserService.class));
        startForeground(45, createDefaultNotification());
        parser.parse(url, null, 1, new HandlerEditorsChoiceXml(db, 0), this, this);

    }

    public void parseTopApps(Database database, String url) {
        if (!spiceManager.isStarted()) {
            Log.d("Aptoide-Parser", "Starting spice");
            spiceManager.start(getApplicationContext());
        }
        startService(new Intent(getApplicationContext(), ParserService.class));
        startForeground(45, createDefaultNotification());
        parser.parse(url, null, 2, new HandlerFeaturedTop(database), this, this);

    }

    public class MainServiceBinder extends Binder {
        public ParserService getService() {
            return ParserService.this;
        }
    }


    static private class HandlerBundle {


        private final HandlerLatestXml handlerLatestXml;
        private final HandlerTopXml handlerTopXml;
        private final HandlerInfoXml handlerInfoXml;

        public HandlerBundle(HandlerInfoXml handlerInfoXml, HandlerTopXml handlerTopXml, HandlerLatestXml handlerLatestXml) {

            this.handlerInfoXml = handlerInfoXml;
            this.handlerTopXml = handlerTopXml;
            this.handlerLatestXml = handlerLatestXml;


        }

        public void cancel(){
            handlerInfoXml.stopParse();
            handlerTopXml.stopParse();
            handlerLatestXml.stopParse();
        }
    }
}