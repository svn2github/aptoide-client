package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.R;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class PasswordDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_pvt_store, null);
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(getString(R.string.add_pvt_store))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String username = ((EditText) v.findViewById(R.id.edit_store_username)).getText().toString();
                        String password = ((EditText) v.findViewById(R.id.edit_store_password)).getText().toString();
                        Intent i = new Intent();

                        i.putExtra("username", username);
                        i.putExtra("password", password);

                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Added_Private_Store");

                    }
                }).create();


        return builder;
    }
}
