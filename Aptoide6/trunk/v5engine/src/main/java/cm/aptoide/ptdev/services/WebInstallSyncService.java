package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cm.aptoide.ptdev.WebInstallSyncAdapter;

/**
 * Created by j-pac on 28-01-2014.
 */
public class WebInstallSyncService extends Service  {

    private static WebInstallSyncAdapter wiSyncAdapter = null;

    private static final Object wiSyncAdapterLock = new Object();

    @Override
    public void onCreate() {

        synchronized (wiSyncAdapterLock) {
            if(wiSyncAdapter == null) {
                wiSyncAdapter = new WebInstallSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wiSyncAdapter.getSyncAdapterBinder();
    }
}
