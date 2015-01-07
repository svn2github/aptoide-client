package cm.aptoidetv.pt.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import cm.aptoidetv.pt.DetailsActivity;
import cm.aptoidetv.pt.R;

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

                        ((DetailsActivity)getActivity()).updateUsername(username.getText().toString());
                    }
                }).create();


        return builder;
    }
}
