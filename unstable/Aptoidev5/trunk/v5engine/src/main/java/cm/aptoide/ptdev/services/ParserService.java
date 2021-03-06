package cm.aptoide.ptdev.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.DismissRefreshEvent;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.Parser;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.callbacks.PoolEndedCallback;
import cm.aptoide.ptdev.parser.events.StopParseEvent;
import cm.aptoide.ptdev.parser.handlers.*;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.URL;

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
    private boolean showNotification = true;



    @Override
    public void onCreate() {
        super.onCreate();
        BusProvider.getInstance().register(this);
        //Log.d("Aptoide-ParserService", "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        //Log.d("Aptoide-ParserService", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d("Aptoide-ParserService","onBind");
        parser = new Parser(spiceManager);
        parser.setPoolEndCallback(new PoolEndedCallback() {
            @Override
            public synchronized void onEnd() {
                //Log.d("Aptoide-ParserService", "onEnd");
                try {
                    if (showNotification) {
                        showNotification = false;
                        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("showUpdatesNotification", true)){
                            showUpdatesNotification();
                        }
                    }
                    if (spiceManager.isStarted()) {
                        spiceManager.shouldStopAndJoin(DurationInMillis.ONE_MINUTE);
                    }
                } catch (InterruptedException e) {
                    //Log.d("Aptoide-ParserService","InterruptedException");
                    e.printStackTrace();
                }finally {
                    //Log.d("Aptoide-ParserService","Stop order");
                    stopForeground(true);
                    stopSelf();
                }
            }
        });

        return new MainServiceBinder();
    }

    private void showUpdatesNotification() {
        int updates = 0;
        Cursor data=null;
        try {
            data = new Database(Aptoide.getDb()).getUpdates();

            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                if (data.getInt(data.getColumnIndex("is_update")) == 1) {
                    updates++;
                }
            }
        }finally {
            if(data!=null)
                data.close();
        }
        if(updates>0){
                NotificationManager managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int icon = android.R.drawable.stat_sys_download_done;


                if(Aptoide.getConfiguration().getMarketName().equals("Aptoide")){
                    icon = R.drawable.ic_stat_aptoide_notification;
                }


                CharSequence tickerText = getString(R.string.has_updates, Aptoide.getConfiguration().getMarketName());
                long when = System.currentTimeMillis();

                Context context = getApplicationContext();
                CharSequence contentTitle = Aptoide.getConfiguration().getMarketName();
                CharSequence contentText = getString(R.string.new_updates, updates);

                if(updates==1){
                    contentText = getString(R.string.one_new_update, updates);
                }

                Intent notificationIntent = new Intent();

                notificationIntent.setClassName(getPackageName(), Aptoide.getConfiguration().getStartActivityClass().getName());
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                notificationIntent.setAction("");


                notificationIntent.putExtra("new_updates", true);


                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(contentIntent)
                        .setTicker(tickerText)
                        .build();
                managerNotification.notify(1, notification);

        }
    }

    @SuppressLint("NewApi")
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

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public boolean repoIsParsing(long id){
        return handlerBundleSparseArray.get((int) id)!=null;
    }

    public boolean anyRepoParser(){
        return handlerBundleSparseArray.size()!=0;
    }

    SparseArray<HandlerBundle> handlerBundleSparseArray = new SparseArray<HandlerBundle>();

    public synchronized void startParse(final Database db, final Store store, boolean newStore) {
        if(handlerBundleSparseArray.get((int) store.getId())!=null){
            return;
        }

        startService(new Intent(getApplicationContext(), ParserService.class));
        startForeground(45, createDefaultNotification(getString(R.string.loading_stores)));
        final long id;
        if(newStore){
            if(db.existsServer(store.getBaseUrl())){
                return;
            }
            id = insertStoreDatabase(db, store);
            store.setId(id);
            BusProvider.getInstance().post(produceRepoAddedEvent());
        }else{
            id = store.getId();
        }

        if(store.getBaseUrl().contains("store.aptoide.com")){
            store.setName(AptoideUtils.RepoUtils.split(store.getBaseUrl()));
        }

        GetRepositoryInfoRequest getRepoInfoRequest = new GetRepositoryInfoRequest(store.getName());

        if (!spiceManager.isStarted()) {
            //Log.d("Aptoide-ParserService", "Starting spice");
            spiceManager.start(getApplicationContext());
        }

        RepositoryInfoJson repositoryInfoJson = null;
        getRepoInfoRequest.setHttpRequestFactory(Jackson2GoogleHttpClientSpiceService.createRequestFactory());

        try {
            repositoryInfoJson = getRepoInfoRequest.loadDataFromNetwork();
        } catch (Exception e) {
            onError(e, id);
            e.printStackTrace();
        }

        if (repositoryInfoJson != null) {
            if (!"FAIL".equals(repositoryInfoJson.getStatus())) {
                store.setName(repositoryInfoJson.getListing().getName());
                store.setDownloads(repositoryInfoJson.getListing().getDownloads());

                if(repositoryInfoJson.getListing().getAvatar_hd()!=null){

                    String sizeString = IconSizes.generateSizeStringAvatar(getApplicationContext());
                    String avatar = repositoryInfoJson.getListing().getAvatar_hd();
                    String[] splittedUrl = avatar.split("\\.(?=[^\\.]+$)");
                    avatar = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
                    store.setAvatar(avatar);

                }else{
                    store.setAvatar(repositoryInfoJson.getListing().getAvatar());
                }

                store.setDescription(repositoryInfoJson.getListing().getDescription());
                store.setTheme(repositoryInfoJson.getListing().getTheme());
                store.setView(repositoryInfoJson.getListing().getView());
                store.setItems(repositoryInfoJson.getListing().getItems());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        beginParse(db, store, id);
                    }
                }).start();
            }
        }
    }

    private void beginParse(Database db, Store store, long id) {
        //Log.d("Aptoide-ParserService", "Creating Objects");
        if(handlerBundleSparseArray.get((int) store.getId())!=null){
            return;
        }
        db.updateStore(store);
        BusProvider.getInstance().post(produceRepoAddedEvent());

        HandlerLatestXml handlerLatestXml = new HandlerLatestXml(db, id);
        HandlerTopXml handlerTopXml = new HandlerTopXml(db, id);
        HandlerInfoXml handlerInfoXml = new HandlerInfoXml(db, id);

        HandlerBundle bundle = new HandlerBundle(handlerInfoXml, handlerTopXml, handlerLatestXml );
        handlerBundleSparseArray.append((int) id, bundle);
        BusProvider.getInstance().post(new RepoCompleteEvent(id));
        //Log.d("Aptoide-ParserService", "Checking timestamps");

        long currentLatestTimestamp = 0;
        long currentTopTimestamp = 0;
        try {
            currentLatestTimestamp = AptoideUtils.NetworkUtils.getLastModified(new URL(store.getLatestXmlUrl()));
            currentTopTimestamp = AptoideUtils.NetworkUtils.getLastModified(new URL(store.getTopXmlUrl()));
        } catch (IOException e) {
            onError(e,store.getId());
            e.printStackTrace();
        }

        //Log.d("Aptoide-ParserService", "Delete");
        if (currentLatestTimestamp>store.getLatestTimestamp()) {
            parser.parse(store.getLatestXmlUrl(), store.getLogin(), 4, handlerLatestXml, new LatestPreParseRunnable(handlerLatestXml, currentLatestTimestamp, db, id) );
        }

        if (currentTopTimestamp>store.getTopTimestamp()) {
            parser.parse(store.getTopXmlUrl(), store.getLogin(), 4, handlerTopXml, new TopPreParseRunnable(handlerTopXml, currentTopTimestamp, db, id));
        }

        //Log.d("Aptoide-ParserService", "Parse");
        parser.parse(store.getInfoXmlUrl(), store.getLogin(), 10, handlerInfoXml, this, this, null);
    }

    public class TopPreParseRunnable implements Runnable{


        private HandlerTopXml handlerTopXml;
        private long currentTopTimestamp;
        private Database db;
        private long id;

        public TopPreParseRunnable(HandlerTopXml handlerLatestXml, long currentTopTimestamp, Database db, long id) {
            this.handlerTopXml = handlerLatestXml;
            this.currentTopTimestamp = currentTopTimestamp;
            this.db = db;
            this.id = id;
        }

        public void run() {
            handlerTopXml.setTimestamp(currentTopTimestamp);
            db.deleteTop(id);
        }
    }

    public class LatestPreParseRunnable implements Runnable{

        private HandlerLatestXml handlerLatestXml;
        private long currentLatestTimestamp;
        private Database db;
        private long id;

        public LatestPreParseRunnable(HandlerLatestXml handlerLatestXml, long currentLatestTimestamp, Database db, long id) {
            this.handlerLatestXml = handlerLatestXml;
            this.currentLatestTimestamp = currentLatestTimestamp;
            this.db = db;
            this.id = id;
        }

        public void run() {
            handlerLatestXml.setTimestamp(currentLatestTimestamp);
            db.deleteLatest(id);
        }
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Notification createDefaultNotification(String string) {
        /*Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new NotificationCompat.Builder(this).setSmallIcon(getApplicationInfo().icon).setContentTitle("Aptoide is working").setContentText(string).build();
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notification = new NotificationCompat.Builder(this).setSmallIcon(getApplicationInfo().icon).setContentTitle("Aptoide is working").setContentText(string).build();
        } else {
            notification = new NotificationCompat.Builder(this).setSmallIcon(getApplicationInfo().icon).setContentTitle("Aptoide is working").setContentText(string).build();
            // temporary fix https://github.com/octo-online/robospice/issues/200
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(this, "", "", pendingIntent);
            notification.when = System.currentTimeMillis();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = Notification.PRIORITY_MIN;
        }*/
        //Log.d("Aptoide-ParserService", "Notification for "+string);
        Notification notification =new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle(getString(R.string.is_working, Aptoide.getConfiguration().getMarketName()))
                .setAutoCancel(true)
                .setContentText(string).build();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = Notification.PRIORITY_MIN;
        }else{
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(this, "", "", pendingIntent);
            notification.when = System.currentTimeMillis();
        }
        //notification.defaults |= Notification.DEFAULT_LIGHTS;
        // Cancel the notification after its selected
        //notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    @Override
    public void onComplete(long repoId) {
        //Log.d("Aptoide", "Removing" + repoId);
        handlerBundleSparseArray.remove((int) repoId);
        BusProvider.getInstance().post(new RepoCompleteEvent(repoId));
    }

    @Override
    public void onError(Exception e, long repoId) {
        //Log.d("Aptoide-ParserService", "onError");
        handlerBundleSparseArray.remove((int) repoId);
        BusProvider.getInstance().post(new RepoErrorEvent(e, repoId));

        if(handlerBundleSparseArray.size()==0){
            stopForeground(true);
            stopSelf();
        }

    }

    ErrorCallback editorsErrorCallback = new ErrorCallback() {
        @Override
        public void onError(Exception e, long repoId) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            preferences.edit().remove("editorschoiceTimestamp").commit();
            BusProvider.getInstance().post(new RepoErrorEvent(e, repoId));
        }
    };

    ErrorCallback topErrorCallback = new ErrorCallback() {
        @Override
        public void onError(Exception e, long repoId) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            preferences.edit().remove("topappsTimestamp").commit();
            BusProvider.getInstance().post(new RepoErrorEvent(e, repoId));

        }
    };



    public void parseEditorsChoice(final Database db, String url) throws IOException {



        long currentTimestamp = AptoideUtils.NetworkUtils.getLastModified(new URL(url));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        long cachedTimestamp = preferences.getLong("editorschoiceTimestamp", 0);

        if (currentTimestamp > cachedTimestamp) {
            preferences.edit().putLong("editorschoiceTimestamp", currentTimestamp).commit();
            if (!spiceManager.isStarted()) {
                //Log.d("Aptoide-ParserService", "Starting spice");
                spiceManager.start(getApplicationContext());
            }


            startService(new Intent(getApplicationContext(), ParserService.class));
            startForeground(45, createDefaultNotification(getString(R.string.loading_editors_choice)));
            parser.parse(url, null, 1, new HandlerEditorsChoiceXml(db, 0), editorsErrorCallback, this, new Runnable() {
                @Override
                public void run() {
                    db.deleteFeatured(510);
                    db.deleteFeaturedGraphics();
                    BusProvider.getInstance().post(new RepoCompleteEvent(-2));
                }
            });
        }else{
            BusProvider.getInstance().post(new DismissRefreshEvent());
        }
    }

    public void parseTopApps(final Database database, String url) throws IOException {

        long currentTimestamp = AptoideUtils.NetworkUtils.getLastModified(new URL(url));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        long cachedTimestamp = preferences.getLong("topappsTimestamp", 0);
        if (currentTimestamp > cachedTimestamp) {
            preferences.edit().putLong("topappsTimestamp", currentTimestamp).commit();

            if (!spiceManager.isStarted()) {
                //Log.d("Aptoide-ParserService", "Starting spice");
                spiceManager.start(getApplicationContext());
            }
            startService(new Intent(getApplicationContext(), ParserService.class));
            startForeground(45, createDefaultNotification(getString(R.string.loading_top_apps)));
            parser.parse(url, null, 2, new HandlerFeaturedTop(database), topErrorCallback, this, new Runnable() {
                @Override
                public void run() {
                    database.deleteFeatured(511);
                    BusProvider.getInstance().post(new RepoCompleteEvent(-1));
                }
            });
        }else{
            BusProvider.getInstance().post(new DismissRefreshEvent());
        }

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
