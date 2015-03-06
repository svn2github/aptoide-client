package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;

import java.util.ArrayList;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;

/**
 * Created by rmateus on 21-02-2014.
 */
public class AutoInstallHelper {

    ArrayList<FragmentUpdates2.UpdatesResponse.UpdateApk> ids;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService serviceInstance = ((DownloadService.LocalBinder)service).getService();

            for (FragmentUpdates2.UpdatesResponse.UpdateApk apk : ids) {
                Download download = new Download();
                download.setId(apk.hashCode());
                download.setName(apk.name);
                download.setPackageName(apk.packageName);
                download.setVersion(apk.vername);
                download.setMd5(apk.md5sum);

                String icon = apk.icon;

                if (icon != null && icon.contains("_icon")) {
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
                }

                download.setIcon(icon);

                serviceInstance.startDownloadFromV6(download,apk);
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
        Database database = new Database(Aptoide.getDb());
        PackageManager pm = context.getPackageManager();
        Cursor c = database.getExcludedApks();
        ArrayList<String> excludedPackages = new ArrayList<>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            excludedPackages.add(c.getString(c.getColumnIndex("package_name")));
        }

        c.close();

        Cursor updatesTabList = database.getUpdates();
        ids = new ArrayList<FragmentUpdates2.UpdatesResponse.UpdateApk>();
        for(updatesTabList.moveToFirst(); !updatesTabList.isAfterLast(); updatesTabList.moveToNext()) {
            FragmentUpdates2.UpdatesResponse.UpdateApk apk = new FragmentUpdates2.UpdatesResponse.UpdateApk();

            int package_name = updatesTabList.getColumnIndex("package_name");
            int filesize = updatesTabList.getColumnIndex("filesize");
            int alt_path = updatesTabList.getColumnIndex("alt_url");
            int path = updatesTabList.getColumnIndex("url");
            int icon = updatesTabList.getColumnIndex("icon");
            int vername = updatesTabList.getColumnIndex("update_vername");
            int md5sum = updatesTabList.getColumnIndex("md5");
            int repo = updatesTabList.getColumnIndex(Schema.Updates.COLUMN_REPO);

            String path_url = updatesTabList.getString(path);

            if (path_url != null) {
                apk.packageName = updatesTabList.getString(package_name);
                try {
                    apk.name = (String) pm.getPackageInfo(apk.packageName, 0).applicationInfo.loadLabel(pm);
                    try {
                        PackageInfo packageInfo = pm.getPackageInfo(updatesTabList.getString(package_name), 0);
                        apk.info = packageInfo.applicationInfo;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    apk.icon = updatesTabList.getString(icon);
                    apk.md5sum = updatesTabList.getString(md5sum);
                    apk.store_name = updatesTabList.getString(repo);
                    apk.apk = new FragmentUpdates2.UpdatesResponse.UpdateApk.Apk();
                    apk.apk.filesize = updatesTabList.getInt(filesize);
                    apk.apk.path_alt = updatesTabList.getString(alt_path);
                    apk.apk.path = updatesTabList.getString(path);
                    apk.vercode = updatesTabList.getInt(updatesTabList.getColumnIndex(Schema.Updates.COLUMN_UPDATE_VERCODE));

                    String string = updatesTabList.getString(vername);
                    if (string == null) {
                        apk.vername = pm.getPackageInfo(apk.packageName, 0).versionName;
                    } else {
                        apk.vername = string;
                    }

                    if (!excludedPackages.contains(apk.packageName)) {
                        ids.add(apk);
                    }
                }catch (Exception e){

                }
            }
        }
        updatesTabList.close();

        context.bindService(new Intent(context, DownloadService.class), conn, 0);

    }
}
