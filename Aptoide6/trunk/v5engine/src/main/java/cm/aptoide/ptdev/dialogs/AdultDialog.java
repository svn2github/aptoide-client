package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by rmateus on 07-03-2014.
 */
public class AdultDialog extends DialogFragment {

    public static final String MATUREPIN = "Maturepin";
    //private static final String DONTSHOWAGAIN = "dontshowagainpin";

    public static Dialog dialogRequestMaturepin(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_requestpin, null);
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        int pin = SecurePreferences.getInstance().getInt(AdultDialog.MATUREPIN, -1);
                        String pintext = ((EditText) v.findViewById(R.id.pininput)).getText().toString();
                        if (pintext.length() > 0 && Integer.valueOf(pintext) == pin) {
                            FlurryAgent.logEvent("Dialog_Adult_Content_Inserted_Pin");
                            positiveButtonlistener.onClick(dialog, which);
                        } else {
                            FlurryAgent.logEvent("Dialog_Adult_Content_Inserted_Wrong_Pin");
                            Toast.makeText(c, c.getString(R.string.adultpinwrong), Toast.LENGTH_SHORT).show();
                            dialogRequestMaturepin(c, positiveButtonlistener).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        positiveButtonlistener.onClick(dialog, which);
                        break;
                }

            }
        };

        AlertDialog.Builder builder= new AlertDialog.Builder(c)
                .setMessage(R.string.requestAdultpin)
                .setView(v)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setNegativeButton(android.R.string.cancel, onClickListener);
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.matureLock();
    }

    /*private static Dialog BuildAskSetAdultpin(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
            final View v = LayoutInflater.from(c).inflate(R.layout.dialog_asksetpin, null);
            AlertDialog.Builder builder= new AlertDialog.Builder(c)
                    .setMessage(R.string.asksetadultpinmessage)
                    .setView(v)
                    .setPositiveButton(R.string.setpin, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DialogSetAdultpin(c).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogAsk21(c,positiveButtonlistener).show();
                        }
                    });
            CheckBox dontShowAgain = (CheckBox)v.findViewById(R.id.dontshowagain);
            dontShowAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit()
                            .putBoolean(DONTSHOWAGAIN,((CheckBox)view).isChecked()).commit();
                }
            });
            return builder.create();
        }*/
    private static Dialog dialogAsk21(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        return new AlertDialog.Builder(c)
                .setMessage(c.getString(R.string.are_you_adult))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FlurryAgent.logEvent("Dialog_Adult_Content_Confirmed_More_Than_21_Years_Old");
                        positiveButtonlistener.onClick(dialog,which);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }
    public static Dialog buildAreYouAdultDialog(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        int pin= SecurePreferences.getInstance().getInt(MATUREPIN,-1);
        if(pin==-1) {
            return dialogAsk21(c, positiveButtonlistener);
        }
        else{
            FlurryAgent.logEvent("Dialog_Adult_Content_Requested_Mature_Content_Pin");
            return dialogRequestMaturepin(c, positiveButtonlistener);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    Callback callback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        return buildAreYouAdultDialog( getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == DialogInterface.BUTTON_POSITIVE){
                    if (callback != null) {
                        callback.matureUnlock();
                        callback = null;
                    }
                }else{
                    Log.d("Mature", "lockedclicked");

                    if (callback != null) {
                        callback.matureLock();
                        callback = null;
                    }
                }


            }
        });
    }

    public interface Callback{
        public void matureUnlock();
        public void matureLock();
    }

}
