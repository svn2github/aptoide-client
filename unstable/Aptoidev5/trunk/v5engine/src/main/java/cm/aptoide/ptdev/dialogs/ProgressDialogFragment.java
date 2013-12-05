package cm.aptoide.ptdev.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-10-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class ProgressDialogFragment extends SherlockDialogFragment {

    public interface OnCancelListener{
        void onCancel();
    }

    OnCancelListener onCancelListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Please wait.");
        pd.setCanceledOnTouchOutside(false);
        return pd;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(onCancelListener!=null)onCancelListener.onCancel();
    }

    public void setOnCancelListener(OnCancelListener onCancelListener){
        this.onCancelListener = onCancelListener;
    }
}
