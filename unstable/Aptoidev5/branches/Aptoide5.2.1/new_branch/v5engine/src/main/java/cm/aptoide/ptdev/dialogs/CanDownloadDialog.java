package cm.aptoide.ptdev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.R;

/**
 * Created by asantos on 07-08-2014.
 */
public class CanDownloadDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getActivity().getString(R.string.are_you_adult))
                .setPositiveButton(R.string.downloadAnyWay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Dialog_Adult_Content_Confirmed_More_Than_21_Years_Old");

                    }
                })
                .setNeutralButton(R.string.schDwnBtn,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                } )
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
}