package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by rmateus on 14-02-2014.
 */
public class UpdateUsernameDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {




        AlertDialog dialog = new AlertDialog.Builder(getActivity())



                .create();





        return super.onCreateDialog(savedInstanceState);
    }
}
