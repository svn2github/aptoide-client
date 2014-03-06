package cm.aptoide.ptdev;

import android.content.*;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.EnumDownloadFailReason;
import cm.aptoide.ptdev.downloadmanager.state.EnumState;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.Parser;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.ParserService;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by rmateus on 21-01-2014.
 */
public class NetworkStateListener extends BroadcastReceiver {

    ParserService service;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            final Database db = new Database(Aptoide.getDb());

            service = ((ParserService.MainServiceBinder) binder).getService();

            ArrayList<Number> storeids = db.getFailedStores();

            for (Number number : storeids) {

                final Store store = new Store();
                Cursor c = db.getStore(number.longValue());

                if (c.moveToFirst()) {
                    store.setBaseUrl(c.getString(c.getColumnIndex("url")));
                    store.setTopTimestamp(c.getLong(c.getColumnIndex("top_timestamp")));
                    store.setLatestTimestamp(c.getLong(c.getColumnIndex("latest_timestamp")));
                    store.setDelta(c.getString(c.getColumnIndex("hash")));
                    store.setId(c.getLong(c.getColumnIndex("id_repo")));
                    if (c.getString(c.getColumnIndex("username")) != null) {
                        Login login = new Login();
                        login.setUsername(c.getString(c.getColumnIndex("username")));
                        login.setPassword(c.getString(c.getColumnIndex("password")));
                        store.setLogin(login);
                    }
                    service.startParse(db, store, false);
                    BusProvider.getInstance().post(new RepoCompleteEvent(store.getId()));
                }
                c.close();
            }



            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
                    long cachedTimestamp = preferences.getLong("editorschoiceTimestamp", 0);
                    if(cachedTimestamp==0){
                        try {

                            service.parseEditorsChoice(new Database(Aptoide.getDb()),"http://apps.store.aptoide.com/editors_more.xml?country=us");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    cachedTimestamp = preferences.getLong("topappsTimestamp", 0);
                    if(cachedTimestamp==0){
                        try {
                            service.parseTopApps(new Database(Aptoide.getDb()),"http://apps.store.aptoide.com/top.xml");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();



            Aptoide.getContext().unbindService(conn);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Object lock = new Object();
    private DownloadService downloadService;
    private ServiceConnection downloadConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            downloadService = ((DownloadService.LocalBinder)service).getService();

            for(Download download : downloadService.getAllNotActiveDownloads()){

                if(download.getDownloadState().equals(EnumState.ERROR) && (download.getParent().getFailReason().equals(EnumDownloadFailReason.TIMEOUT)||download.getParent().getFailReason().equals(EnumDownloadFailReason.CONNECTION_ERROR))){
                    download.getParent().download();
                }

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onReceive(Context context, Intent incomingIntent) {


        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (((wifi != null && wifi.getState() == NetworkInfo.State.CONNECTED) && service == null)) {

            synchronized (lock) {
                Aptoide.getContext().bindService(new Intent(Aptoide.getContext(), ParserService.class), conn, Context.BIND_AUTO_CREATE);
            }

        }


        if (((wifi != null && wifi.getState() == NetworkInfo.State.CONNECTED) && downloadService == null)) {
            synchronized (lock) {
                Aptoide.getContext().bindService(new Intent(Aptoide.getContext(), DownloadService.class), downloadConn, Context.BIND_AUTO_CREATE);
            }
        }


    }

}
