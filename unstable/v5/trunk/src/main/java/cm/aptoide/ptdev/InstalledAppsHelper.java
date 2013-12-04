package cm.aptoide.ptdev;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.InstalledPackage;

import java.util.List;

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

        List<PackageInfo> system_installed_list = context.getPackageManager().getInstalledPackages(0);
        List<InstalledPackage> database_installed_list = db.getStartupInstalled();


        for (PackageInfo pkg : system_installed_list) {

            try {
                InstalledPackage apk = new InstalledPackage(
                                pkg.applicationInfo.loadIcon(context.getPackageManager()),
                                (String) pkg.applicationInfo.loadLabel(context.getPackageManager()),
                                pkg.packageName,
                                pkg.versionCode,
                                pkg.versionName);

                if (!database_installed_list.contains(apk)) {
                    Log.d("Aptoide-InstalledSync", "Adding" + apk.getPackage_name() + "-" + apk.getVersion_name());
                    db.insertInstalled(apk);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
