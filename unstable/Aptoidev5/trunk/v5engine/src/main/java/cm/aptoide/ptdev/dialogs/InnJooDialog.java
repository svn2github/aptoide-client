package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by rmateus on 03-09-2014.
 */
public class InnJooDialog extends DialogFragment {


    private Callback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(callback!=null)callback.onOkClick();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) callback.onCancelClick();
            }
        });

        dialogBuilder.setMessage("InnCloud App is needed to perform authentication. Do you want to download?");


        return dialogBuilder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (callback != null) callback.onCancelClick();
    }

    public interface Callback {

        void onOkClick();
        void onCancelClick();

    }
}
