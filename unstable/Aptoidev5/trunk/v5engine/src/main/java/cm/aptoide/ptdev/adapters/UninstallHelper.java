package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

/**
 * Created by rmateus on 10-12-2013.
 */
public class UninstallHelper {


    public static void uninstall(Activity context, String package_name) {
        Uri uri = Uri.fromParts("package", package_name, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivityForResult(intent, 150);
    }
}
