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
    private DialogInterface.OnClickListener cancelListener;


    public ErrorDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        tryAgainListener = ((AppViewActivity)getActivity()).getTryAgainListener();
        cancelListener = ((AppViewActivity)getActivity()).getCancelListener();



        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(android.R.string.dialog_alert_title))
                .setIcon(android.R.drawable.stat_sys_warning)
                .setMessage(getString(R.string.connection_error))
                .setPositiveButton(getString(R.string.try_again), tryAgainListener)
                .setNegativeButton(android.R.string.cancel, cancelListener)
                .create();

        setCancelable(false);

        return builder;
    }

}
