package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.services.DownloadService;

import java.util.ArrayList;

/**
 * Created by rmateus on 21-02-2014.
 */
public class AutoInstallHelper {

    ArrayList<Number> ids;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService serviceInstance = ((DownloadService.LocalBinder)service).getService();

            for(Number id:ids){
                serviceInstance.startDownloadFromAppId(id.longValue());
            }

            context.unbindService(conn);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Context context;

    public void autoInstall(Context context) {
        this.context = context;
        Cursor data = new Database(Aptoide.getDb()).getUpdates();

        ids = new ArrayList<Number>();
        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
            if(data.getInt(data.getColumnIndex("is_update"))==1){
                ids.add(data.getLong(data.getColumnIndex("_id")));
            }
        }
        data.close();

        context.bindService(new Intent(context, DownloadService.class), conn, 0);

    }
}
