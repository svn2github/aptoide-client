package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import cm.aptoide.ptdev.R;

import static cm.aptoide.ptdev.utils.AptoideUtils.getSharedPreferences;

/**
 * Created by rmateus on 30-12-2014.
 */
public class AdultHiddenDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CheckBox checkBox = (CheckBox) getDialog().findViewById(R.id.dontshow_checkbox);
                getSharedPreferences().edit().putBoolean("showadulthidden", !checkBox.isChecked()).apply();
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        new AdultDialog().show(getFragmentManager(), "adultDialog");
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }

            }
        };

        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity())
                .setView(LayoutInflater.from(getActivity()).inflate(R.layout.hidden_adult, null))
                .setPositiveButton(android.R.string.yes, onClickListener)
                .setNegativeButton(android.R.string.no, onClickListener);
        return builder.create();


    }
}
