package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import cm.aptoide.ptdev.R;

/**
 * Created by rmateus on 07-01-2014.
 */
public class WrongXmlVersionDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog builder = new AlertDialog.Builder(getActivity())

                .setTitle("Incompatible store")
                .setMessage("This store is not yet incompatible with this version of Aptoide. Please wait some days until we rebuild all stores. ")
                .setNeutralButton("OK", null)
                .create();

        return builder;
    }
}
