package cm.aptoide.ptdev;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-11-2013
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class InstalledAppsHelper {

    public static void sync(SQLiteDatabase database, Context context) {
        Database db = new Database(database);

        try{
            //db.clearInstalled();

            db.getDatabaseInstance().beginTransaction();

            List<InstalledPackage> database_installed_list = db.getStartupInstalled();


            PackageManager packageManager = Aptoide.getContext().getPackageManager();
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);

            for (PackageInfo anInstalledPackage : installedPackages) {

                try {

                    FragmentUpdates2.UpdatesApi.Package aPackage = new FragmentUpdates2.UpdatesApi.Package();
                    aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(anInstalledPackage.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                    aPackage.vercode = anInstalledPackage.versionCode;
                    aPackage.packageName = anInstalledPackage.packageName;

                    InstalledPackage apk = new InstalledPackage(
                            "",
                            anInstalledPackage.packageName,
                            anInstalledPackage.versionCode,
                            anInstalledPackage.versionName,
                            aPackage.signature);

                    if (!database_installed_list.contains(apk)) {
                        Log.d("Aptoide-InstalledSync", "Adding " + apk.getPackage_name() + "-" + apk.getVersion_name());
                        db.insertInstalled(aPackage);
                    } else {
                        database_installed_list.remove(apk);
                        Log.d("Aptoide-InstalledSync", "Removing from list" + apk.getPackage_name() + "-" + apk.getVersion_name());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (!database_installed_list.isEmpty()) {
                for (InstalledPackage installedPackage : database_installed_list) {
                    db.deleteInstalledApk(installedPackage.getPackage_name());
                    Log.d("Aptoide-InstalledSync", "Removing from database" + installedPackage.getPackage_name() + "-" + installedPackage.getVersion_name());
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.getDatabaseInstance().setTransactionSuccessful();
            db.getDatabaseInstance().endTransaction();
        }



    }



}
