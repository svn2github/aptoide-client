package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 07-03-2014.
 */
public class AllowRootDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.root_access_dialog))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("allowRoot", true).commit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("allowRoot", false).commit();
                    }
                })
                .create();

        return builder;
    }

}
