package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by rmateus on 07-03-2014.
 */
public class AdultDialog extends DialogFragment {

    private static final String MATUREPIN = "Maturepin";
    private static final String DONTSHOWAGAIN = "dontshowagainpin";

    private static Dialog BuildRequestAdultpin(final Context c, final DialogInterface.OnClickListener positiveButtonlistener,final int pin){
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_setpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(c)
                .setMessage(R.string.requestAdultpin)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pininput = new Integer(((EditText) v.findViewById(R.id.pininput)).getText().toString());
                        if (pininput == pin)
                            positiveButtonlistener.onClick(dialog, which);
                        else {
                            Toast.makeText(c, c.getString(R.string.adultpinwrong), Toast.LENGTH_SHORT).show();
                            BuildRequestAdultpin(c,positiveButtonlistener,pin).show();
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
    private static Dialog BuildSetAdultpin(final Context c){
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_setpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(c)
                .setMessage(R.string.asksetadultpinmessage)
                .setView(v)
                .setPositiveButton(R.string.setpin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = ((EditText) v.findViewById(R.id.pininput)).getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            new SecurePreferences(c)
                                    .edit()
                                    .putInt(MATUREPIN, new Integer(input))
                                    .commit();
                            PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit()
                                    .putBoolean(DONTSHOWAGAIN, true).commit();
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
    private static Dialog BuildAskSetAdultpin(final Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_asksetpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(c)
                .setMessage(R.string.asksetadultpinmessage)
                .setView(v)
                .setPositiveButton(R.string.setpin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BuildSetAdultpin(c).show();
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
    }
    private static Dialog DialogAsk21(Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        return new AlertDialog.Builder(c)
                .setMessage(c.getString(R.string.are_you_adult))
                .setPositiveButton(android.R.string.ok, positiveButtonlistener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
    public static Dialog BuildAreYouAdultDialog(Context c, final DialogInterface.OnClickListener positiveButtonlistener){
        int pin= new SecurePreferences(c).getInt(MATUREPIN,-1);
        if(pin==-1) {
            boolean dontask= PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean(DONTSHOWAGAIN,false);
            if(dontask)
                return DialogAsk21(c,positiveButtonlistener);
            else
                return BuildAskSetAdultpin(c,positiveButtonlistener);

        }
        else
            return BuildRequestAdultpin(c,positiveButtonlistener,pin);

    }

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

    Callback callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return BuildAreYouAdultDialog(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) callback.onOk();
            }
        });
    }

    public interface Callback{
        public void onOk();

    }

}
