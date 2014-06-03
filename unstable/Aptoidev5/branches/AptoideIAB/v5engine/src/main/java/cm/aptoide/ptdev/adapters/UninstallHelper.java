package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 10-12-2013.
 */
public class UninstallHelper {


    public static void uninstall(ActionBarActivity context, String package_name, boolean isDowngrade) {
        Uri uri = Uri.fromParts("package", package_name, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);

        if (!package_name.equals(context.getPackageName())) {
            if (isDowngrade) {
                context.startActivityForResult(intent, AppViewActivity.DOWGRADE_REQUEST_CODE);
            } else {
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.cannot_uninstall_self), Toast.LENGTH_LONG).show();
        }
    }
}
