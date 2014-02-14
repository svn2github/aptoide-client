package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 15-01-2014.
 */
public class ErrorDialog extends DialogFragment {

    private DialogInterface.OnClickListener tryAgainListener;

    public ErrorDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

//        tryAgainListener = ((AppViewActivity)getActivity()).getTryAgainListener();


        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(android.R.string.dialog_alert_title))
                .setIcon(android.R.drawable.stat_sys_warning)
                .setMessage(getString(R.string.connection_error))
                .setPositiveButton(getString(R.string.try_again), tryAgainListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        setCancelable(false);

        return builder;
    }

}
