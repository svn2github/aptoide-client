package cm.aptoide.ptdev;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by j-pac on 27-01-2014.
 */
public class AutoUpdateSyncAdapter extends AbstractThreadedSyncAdapter {




    public AutoUpdateSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(sharedPreferences.getBoolean("auto_update", false)){
            Log.d("Aptoide-AutoUpdateSync", "onPerformSync()");
            AutoInstallHelper helper = new AutoInstallHelper();
            helper.autoInstall(getContext());
        }

    }



}
