package cm.aptoide.ptdev;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by rmateus on 13-12-2013.
 */
public class InstalledBroadcastReceiver extends BroadcastReceiver {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
                final PackageInfo pkg = mPm.getPackageInfo(intent.getData().getEncodedSchemeSpecificPart(), PackageManager.GET_SIGNATURES);


                apk = new InstalledPackage(

                        (String) pkg.applicationInfo.loadLabel(context.getPackageManager()),
                        pkg.packageName,
                        pkg.versionCode,
                        pkg.versionName,
                        AptoideUtils.Algorithms.computeSHA1sumFromBytes(pkg.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH));


                db.insertInstalled(apk);
                db.deleteScheduledDownloadByPackageName(intent.getData().getEncodedSchemeSpecificPart());
                BusProvider.getInstance().post(new InstalledApkEvent(apk));


                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    final String action = db.getNotConfirmedRollbackAction(pkg.packageName);
                    if(action != null) {

                        final String referrer = action.split("\\|")[1];

                        if(action.split("\\|")[0].equals(RollBackItem.Action.INSTALLING.toString())) {

                            db.confirmRollBackAction(pkg.packageName, action, RollBackItem.Action.INSTALLED.toString());
                            Intent i = new Intent("com.android.vending.INSTALL_REFERRER");
                            i.setPackage(intent.getData().getEncodedSchemeSpecificPart());
                            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            i.putExtra("referrer", referrer);
                            context.sendBroadcast(i);

                            Log.d("InstalledBroadcastReceiver", "Installed rollback action");
                        } else if(action.split("|")[0].equals(RollBackItem.Action.DOWNGRADING.toString())) {
                            db.confirmRollBackAction(pkg.packageName, action, RollBackItem.Action.DOWNGRADED.toString());
                            Log.d("InstalledBroadcastReceiver", "Downgraded rollback action");
                        }
                    }
                }

                BusProvider.getInstance().post(new InstalledApkEvent(apk));

                if(Build.VERSION.SDK_INT >= 11 && context.getPackageManager().getInstallerPackageName(intent.getData().getEncodedSchemeSpecificPart())==null){
                    context.getPackageManager().setInstallerPackageName(intent.getData().getEncodedSchemeSpecificPart() , context.getPackageName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

            String packageName = intent.getData().getEncodedSchemeSpecificPart();

            db.deleteInstalledApk(packageName);
            BusProvider.getInstance().post(new UnInstalledApkEvent(packageName));

            if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {

                String action = db.getNotConfirmedRollbackAction(packageName);
                if(action != null) {
                    if(action.equals(RollBackItem.Action.UNINSTALLING.toString())) {
                        db.confirmRollBackAction(packageName, action, RollBackItem.Action.UNINSTALLED.toString());
                        Log.d("InstalledBroadcastReceiver", "unistalled rollback action");

                    }
                }

                BusProvider.getInstance().post(new UnInstalledApkEvent(intent.getData().getEncodedSchemeSpecificPart()));

            }
    }
 }}
