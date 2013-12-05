package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import cm.aptoide.ptdev.R;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class PasswordDialog extends SherlockDialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getSherlockActivity()).inflate(R.layout.dialog_storepassword, null);
        AlertDialog builder = new AlertDialog.Builder(getSherlockActivity())
                .setView(v)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String username = ((EditText) v.findViewById(R.id.edit_store_username)).getText().toString();
                        String password = ((EditText) v.findViewById(R.id.edit_store_password)).getText().toString();
                        Intent i = new Intent();

                        i.putExtra("username", username);
                        i.putExtra("password", password);

                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                    }
                }).create();


        return builder;
    }
}
