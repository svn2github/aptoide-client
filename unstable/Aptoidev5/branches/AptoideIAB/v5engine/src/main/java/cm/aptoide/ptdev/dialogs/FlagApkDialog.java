package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.flurry.android.FlurryAgent;

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

    public enum Uservote {
        good, license, fake, freeze, virus, novote;
    }

    public static final String USERVOTE_ARGUMENT_KEY = "uservote";

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

        if(getArguments() != null && getArguments().containsKey(FlagApkDialog.USERVOTE_ARGUMENT_KEY)) {
            Log.d("apkflag", "uservote: " + getArguments().getString(FlagApkDialog.USERVOTE_ARGUMENT_KEY));
            Uservote uservote = Uservote.valueOf(getArguments().getString(FlagApkDialog.USERVOTE_ARGUMENT_KEY));

            int uservoteButtonId = getButtonIdFromUservote(uservote);
            if(uservoteButtonId != -1) {
                ((RadioButton) view.findViewById(uservoteButtonId)).setChecked(true);
            }
        }

        view.findViewById(R.id.button_mark_flag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flagApkCallback != null) {
                    int checkedButtonId = ((RadioGroup) view.findViewById(R.id.flag_group)).getCheckedRadioButtonId();

                    if (checkedButtonId != -1) {
                        Log.d("apkflag", "flag: " + getUservoteFromButtonId(checkedButtonId).name());
                        flagApkCallback.addApkFlagClick(getUservoteFromButtonId(checkedButtonId).name());
                        dismiss();
                    }
                }
            }
        });

        return builder;
    }

    private static int getButtonIdFromUservote(Uservote uservote) {
        switch (uservote) {
            case good:
                return R.id.button_good;
            case license:
                return R.id.button_license;
            case fake:
                return R.id.button_fake;
            case freeze:
                return R.id.button_freeze;
            case virus:
                return R.id.button_virus;
            default:
                return -1;
        }
    }

    private static Uservote getUservoteFromButtonId(int buttonId) {
        if (buttonId == R.id.button_good) {
            return Uservote.good;
        } else if (buttonId == R.id.button_license) {
            return Uservote.license;
        } else if (buttonId == R.id.button_fake) {
            return Uservote.fake;
        } else if (buttonId == R.id.button_freeze) {
            return Uservote.freeze;
        } else if (buttonId == R.id.button_virus) {
            return Uservote.virus;
        }
        return Uservote.novote;
    }



}
