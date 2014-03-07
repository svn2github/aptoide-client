package cm.aptoide.ptdev;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

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
        db.clearInstalled();

        List<PackageInfo> system_installed_list = context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
        List<InstalledPackage> database_installed_list = db.getStartupInstalled();


        for (PackageInfo pkg : system_installed_list) {

            try {
                InstalledPackage apk = new InstalledPackage(
                                (String) pkg.applicationInfo.loadLabel(context.getPackageManager()),
                                pkg.packageName,
                                pkg.versionCode,
                                pkg.versionName,
                        AptoideUtils.Algorithms.computeSHA1sumFromBytes(pkg.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH));

                if (!database_installed_list.contains(apk)) {
                    Log.d("Aptoide-InstalledSync", "Adding " + apk.getPackage_name() + "-" + apk.getVersion_name());
                    db.insertInstalled(apk);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
