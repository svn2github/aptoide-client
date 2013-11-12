package cm.aptoide.ptdev.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-10-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class ProgressDialogFragment extends SherlockDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Please wait.");
        return pd;
    }
}
