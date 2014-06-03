package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MyAppsAddStoreInterface;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 15-01-2014.
 */
public class MyAppStoreDialog extends DialogFragment{
    private DialogInterface.OnClickListener okListener;
    private String repoName;

    public MyAppStoreDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.repoName = getArguments().getString("repoName");
        okListener = ((MyAppsAddStoreInterface)getActivity()).getOnMyAppAddStoreListener(repoName);
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.add_store))
                .setIcon(android.R.drawable.ic_menu_more)
                .setCancelable(false)
                .setMessage((getString(R.string.newrepo_alrt) + repoName + " ?"))
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)

                .create();

        return builder;
    }


}
