package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import cm.aptoide.ptdev.AutoUpdateSyncAdapter;
import cm.aptoide.ptdev.UpdatesSyncAdapter;

/**
 * Created by j-pac on 28-01-2014.
 */
public class AutoUpdateSyncService extends Service  {

    private static AutoUpdateSyncAdapter wiSyncAdapter = null;

    private static final Object wiSyncAdapterLock = new Object();

    @Override
    public void onCreate() {

        synchronized (wiSyncAdapterLock) {
            if(wiSyncAdapter == null) {
                wiSyncAdapter = new AutoUpdateSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wiSyncAdapter.getSyncAdapterBinder();
    }
}
