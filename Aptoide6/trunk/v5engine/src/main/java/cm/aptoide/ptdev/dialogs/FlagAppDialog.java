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
import cm.aptoide.ptdev.R;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class FlagAppDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_flag_app, null);
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(getString(R.string.flag_this_app))

                .create();


        return builder;
    }
}
