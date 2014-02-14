package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 15-01-2014.
 */
public class MyAppInstallDialog extends DialogFragment {
    private DialogInterface.OnClickListener okListener;
    private String appName;
    private DialogInterface.OnDismissListener dismissListener;

    public MyAppInstallDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        okListener = ((AppViewActivity)getActivity()).getMyAppListener();

        dismissListener = ((AppViewActivity)getActivity()).getOnDismissListener();
        this.appName = getArguments().getString("appName");
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle(Aptoide.getConfiguration().getMarketName())
                .setIcon(android.R.drawable.ic_menu_more)
                .setMessage(getString(R.string.installapp_alrt) + appName + "?")
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        setCancelable(false);
        //builder.setOnDismissListener(dismissListener);
        return builder;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dismissListener!=null) dismissListener.onDismiss(dialog);
    }
}
