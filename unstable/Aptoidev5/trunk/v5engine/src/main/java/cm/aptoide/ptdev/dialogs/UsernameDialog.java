package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class UsernameDialog extends DialogFragment {

    EditText username;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_username, null);


        username = (EditText) v.findViewById(R.id.update_username);

        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(getString(R.string.update_username))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((AppViewActivity)getActivity()).updateUsername(username.getText().toString());
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Updated_Username_To_Insert_Comment");
                    }
                }).create();


        return builder;
    }
}
