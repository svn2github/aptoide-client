package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.callbacks.ApkFlagCallback;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class FlagApkDialog extends DialogFragment {

    ApkFlagCallback flagApkCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        flagApkCallback = (ApkFlagCallback) activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        flagApkCallback = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_flag_app, null);
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(getString(R.string.flag_this_app))
                .create();

        view.findViewById(R.id.button_mark_flag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flagApkCallback != null) {
                    int checkedButtonId = ((RadioGroup) view.findViewById(R.id.flag_group)).getCheckedRadioButtonId();

                    if (checkedButtonId != -1) {
                        Button checkedButton = ((Button) view.findViewById(checkedButtonId));

                        String flag = "";
                        int i = checkedButton.getId();
                        if (i == R.id.radioButton) {
                            flag = "good";
                        } else if (i == R.id.radioButton1) {
                            flag = "license";
                        } else if (i == R.id.radioButton2) {
                            flag = "fake";
                        } else if (i == R.id.radioButton3) {
                            flag = "freeze";
                        } else if (i == R.id.radioButton4) {
                            flag = "virus";
                        }

                        Log.d("apkflag", "flag: " + flag);
                        flagApkCallback.addApkFlagClick(flag);
                        dismiss();
                    }
                }
            }
        });

        return builder;
    }


}
