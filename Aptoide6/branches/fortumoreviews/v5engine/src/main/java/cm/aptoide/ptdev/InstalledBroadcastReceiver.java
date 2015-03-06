package cm.aptoide.ptdev;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.utils.AptoideUtils;

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

            try{
                PackageManager mPm = context.getPackageManager();
                final PackageInfo pkg = mPm.getPackageInfo(intent.getData().getEncodedSchemeSpecificPart(), PackageManager.GET_SIGNATURES);
                FragmentUpdates2.UpdatesApi.Package aPackage = new FragmentUpdates2.UpdatesApi.Package();
                aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(pkg.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                aPackage.vercode = pkg.versionCode;
                aPackage.packageName = pkg.packageName;
                db.insertInstalled(aPackage);
                Log.d("AptoideUpdates", "Inserting " + aPackage.packageName);

            }catch (Exception e){

            }


        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {

            InstalledPackage apk;
            try {
                PackageManager mPm = context.getPackageManager();
                final PackageInfo pkg = mPm.getPackageInfo(intent.getData().getEncodedSchemeSpecificPart(), PackageManager.GET_SIGNATURES);


                FragmentUpdates2.UpdatesApi.Package aPackage = new FragmentUpdates2.UpdatesApi.Package();
                aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(pkg.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                aPackage.vercode = pkg.versionCode;
                aPackage.packageName = pkg.packageName;
                db.insertInstalled(aPackage);
                Log.d("AptoideUpdates", "Inserting " + aPackage.packageName);

                db.deleteScheduledDownloadByPackageName(intent.getData().getEncodedSchemeSpecificPart());
                BusProvider.getInstance().post(new InstalledApkEvent(null));


                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    final String action = db.getNotConfirmedRollbackAction(pkg.packageName);
                    if(action != null) {
                        final String referrer;

                        if(action.contains("|")){
                            referrer = action.split("\\|")[1];
                        } else {
                            referrer = "";
                        }

                        if(action.split("\\|")[0].equals(RollBackItem.Action.INSTALLING.toString())) {

                            db.confirmRollBackAction(pkg.packageName, action, RollBackItem.Action.INSTALLED.toString());


                            if(!TextUtils.isEmpty(referrer)) {

                                Intent i = new Intent("com.android.vending.INSTALL_REFERRER");
                                i.setPackage(intent.getData().getEncodedSchemeSpecificPart());
                                i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                i.putExtra("referrer", referrer);
                                context.sendBroadcast(i);
                                Log.d("InstalledBroadcastReceiver", "Sent broadcast with referrer " + referrer);

                            }

                            Log.d("InstalledBroadcastReceiver", "Installed rollback action");
                        } else if(action.split("\\|")[0].equals(RollBackItem.Action.DOWNGRADING.toString())) {
                            db.confirmRollBackAction(pkg.packageName, action, RollBackItem.Action.DOWNGRADED.toString());
                            Log.d("InstalledBroadcastReceiver", "Downgraded rollback action");
                        }
                    }
                }

                BusProvider.getInstance().post(new InstalledApkEvent(null));

                if(Build.VERSION.SDK_INT >= 11 && context.getPackageManager().getInstallerPackageName(intent.getData().getEncodedSchemeSpecificPart())==null){
                    context.getPackageManager().setInstallerPackageName(intent.getData().getEncodedSchemeSpecificPart() , context.getPackageName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

            String packageName = intent.getData().getEncodedSchemeSpecificPart();

            db.deleteInstalledApk(packageName);

            Log.d("AptoideUpdates", "Deleting " + packageName);
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
