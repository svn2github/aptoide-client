package cm.aptoide.ptdev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.model.RollBackItem;

/**
 * Created by rmateus on 13-12-2013.
 */
public class InstalledBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Database db = new Database(Aptoide.getDb());

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {

            db.confirmRollBackAction(intent.getData().getEncodedSchemeSpecificPart(), RollBackItem.Action.UPDATING.toString(), RollBackItem.Action.UPDATED.toString());

            Log.d("InstalledBroadcastReceiver", "Updated rollback action");

        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {


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


                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    db.confirmRollBackAction(pkg.packageName, RollBackItem.Action.INSTALLING.toString(), RollBackItem.Action.INSTALLED.toString());


                    Log.d("InstalledBroadcastReceiver", "Installed rollback action");

                }


                /*
                Cursor c = db.getInstalledMd5(apk.getPackage_name());
                c.moveToFirst();
                String existentMd5 = c.getString(1);

                    File file = new File(pkg.applicationInfo.sourceDir);
                    String apk_md5 = AptoideUtils.Algorithms.md5Calc(file);
                    apk.setMd5(apk_md5);


                    RollBackItem rollBackItem;
                    if(existentMd5 != null) {
                        db.insertInstalled(apk);

                        rollBackItem = new RollBackItem(apk.getPackage_name(), apk.getIcon(), timestamp, apk.getVersion_name(), existentMd5);
                        db.insertRollbackAction(rollBackItem, RollBackItem.UPDATED);
                    } else {
                        db.insertInstalled(apk);

                        rollBackItem = new RollBackItem(apk.getPackage_name(), apk.getIcon(), timestamp, apk.getVersion_name(), apk_md5);
                        db.insertRollbackAction(rollBackItem, RollBackItem.INSTALLED);
                    }
                    Intent i = new Intent(context, RollbackActivity.class);
                    context.startActivity(i);
*/

                BusProvider.getInstance().post(new InstalledApkEvent(apk));

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

            db.deleteInstalledApk(intent.getData().getEncodedSchemeSpecificPart());
            BusProvider.getInstance().post(new UnInstalledApkEvent(intent.getData().getEncodedSchemeSpecificPart()));

            if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                db.confirmRollBackAction(intent.getData().getEncodedSchemeSpecificPart(), RollBackItem.Action.UNINSTALLING.toString(), RollBackItem.Action.UNINSTALLED.toString());

                Log.d("InstalledBroadcastReceiver", "unistalled rollback action");

            }

            BusProvider.getInstance().post(new UnInstalledApkEvent(intent.getData().getEncodedSchemeSpecificPart()));



        }
    }
}
