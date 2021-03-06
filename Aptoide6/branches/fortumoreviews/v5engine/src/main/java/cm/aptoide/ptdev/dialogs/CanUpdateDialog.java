package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;

/**
 * Created by asantos on 11-08-2014.
 */
public class CanUpdateDialog extends DialogFragment {
    public final static String ARGKEYIDS = "ids";
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (Start) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();


    }

    Start callback;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getArguments().getString("appName");
        return new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.Data_Usage_warning))
                .setMessage(getActivity().getString(R.string.Data_Usage_Message))
                .setPositiveButton(R.string.downloadAnyWay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FlurryAgent.logEvent("CanUpdateDialog_DownLoad_AnyWay");

                        if (callback != null && getArguments().getLongArray(ARGKEYIDS)!=null) {
                            callback.updateAll(getArguments().getLongArray(ARGKEYIDS));
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FlurryAgent.logEvent("CanUpdateDialog_Cancel");
                    }
                })
                .create();
    }
}
