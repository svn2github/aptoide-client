package cm.aptoide.ptdev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;

/**
 * Created by rmateus on 21-01-2014.
 */
public class WifiStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent incomingIntent) {


        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (wifi.getState() == NetworkInfo.State.CONNECTED) {
            Database db = new Database(Aptoide.getDb());
            Log.d("Receiver", "Wireless Connected");
            if (sPref.getBoolean("schDwnBox", false) && db.getScheduledDownloads().getCount() != 0 && sPref.getBoolean("schTrigger", true)) {
                Intent intent = new Intent(context, ScheduledDownloadsActivity.class);
                sPref.edit().putBoolean("schTrigger", false).commit();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |-Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra("downloadAll", true);
                Log.i("Reeceiver", sPref.getBoolean("intentChanged", true) + "");
                context.startActivity(intent);
            }
        }

    }


}
