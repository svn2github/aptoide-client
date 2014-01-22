package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import cm.aptoide.ptdev.AppViewActivity;

/**
 * Created by rmateus on 10-12-2013.
 */
public class UninstallHelper {


    public static void uninstall(ActionBarActivity context, String package_name, boolean isDowngrade) {
        Uri uri = Uri.fromParts("package", package_name, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);

        if(isDowngrade) {
            context.startActivityForResult(intent, AppViewActivity.DOWGRADE_REQUEST_CODE);
        } else {
            context.startActivityForResult(intent, 150);
        }
    }
}
