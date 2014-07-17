package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by rmateus on 07-03-2014.
 */
public class AdultDialog extends DialogFragment {

    public static final String MATUREPIN = "Maturepin";
    //private static final String DONTSHOWAGAIN = "dontshowagainpin";

    public static Dialog DialogRequestMaturepin(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_requestpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(c)
                .setMessage(R.string.requestAdultpin)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pin = new SecurePreferences(c).getInt(AdultDialog.MATUREPIN,-1);
                        String pintext=((EditText) v.findViewById(R.id.pininput)).getText().toString();
                        if(pintext.length()>0 && new Integer(pintext) == pin)
                            positiveButtonlistener.onClick(dialog, which);
                        else {
                            Toast.makeText(c, c.getString(R.string.adultpinwrong), Toast.LENGTH_SHORT).show();
                            DialogRequestMaturepin(c, positiveButtonlistener).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
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
                        DialogAsk21(c,positiveButtonlistener).show();
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
    private static Dialog DialogAsk21(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        return new AlertDialog.Builder(c)
                .setMessage(c.getString(R.string.are_you_adult))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        positiveButtonlistener.onClick(dialog,which);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
    public static Dialog BuildAreYouAdultDialog(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        int pin= new SecurePreferences(c).getInt(MATUREPIN,-1);
        if(pin==-1) {
            return DialogAsk21(c,positiveButtonlistener);
        }
        else
            return DialogRequestMaturepin(c, positiveButtonlistener);
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return BuildAreYouAdultDialog(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.matureUnlock();
                    callback = null;
                }
            }
        });
    }

    public interface Callback{
        public void matureUnlock();
    }

}
