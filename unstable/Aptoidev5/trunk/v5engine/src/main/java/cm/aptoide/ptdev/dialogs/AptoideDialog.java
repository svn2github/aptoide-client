package cm.aptoide.ptdev.dialogs;

import android.support.v4.app.DialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class AptoideDialog {

    public static DialogFragment addStoreDialog(){
        return new AddStoreDialog();
    }

    public static DialogFragment pleaseWaitDialog(){
        return new ProgressDialogFragment();
    }

    public static DialogFragment passwordDialog(){
        return new PasswordDialog();
    }

    public static DialogFragment wrongVersionXmlDialog(){
        return new WrongXmlVersionDialog();
    }




}
