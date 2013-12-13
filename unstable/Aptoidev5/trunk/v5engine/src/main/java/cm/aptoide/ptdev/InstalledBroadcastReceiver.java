package cm.aptoide.ptdev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.InstalledPackage;

/**
 * Created by rmateus on 13-12-2013.
 */
public class InstalledBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Database db = new Database(Aptoide.getDb());

        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            InstalledPackage apk;
            try {
                PackageManager mPm = context.getPackageManager();
                PackageInfo pkg = mPm.getPackageInfo(intent.getData().getEncodedSchemeSpecificPart(), 0);

                apk = new InstalledPackage(
                        pkg.applicationInfo.loadIcon(context.getPackageManager()),
                        (String) pkg.applicationInfo.loadLabel(context.getPackageManager()),
                        pkg.packageName,
                        pkg.versionCode,
                        pkg.versionName);
                db.insertInstalled(apk);
                BusProvider.getInstance().post(new InstalledApkEvent(apk));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){

            db.deleteInstalledApk(intent.getData().getEncodedSchemeSpecificPart());
            BusProvider.getInstance().post(new UnInstalledApkEvent(intent.getData().getEncodedSchemeSpecificPart()));

        }

    }
}
