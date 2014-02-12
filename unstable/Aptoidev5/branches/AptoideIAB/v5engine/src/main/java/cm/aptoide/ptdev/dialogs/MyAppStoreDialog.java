package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 15-01-2014.
 */
public class MyAppStoreDialog extends DialogFragment{
    private DialogInterface.OnClickListener okListener;
    private String repoName;

    public MyAppStoreDialog(DialogInterface.OnClickListener okListener, String repoName) {

        this.okListener = okListener;
        this.repoName = repoName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.add_store))
                .setIcon(android.R.drawable.ic_menu_more)
                .setCancelable(false)
                .setMessage((getString(R.string.newrepo_alrt) + repoName + " ?"))
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)

                .create();

        return builder;
    }


}
