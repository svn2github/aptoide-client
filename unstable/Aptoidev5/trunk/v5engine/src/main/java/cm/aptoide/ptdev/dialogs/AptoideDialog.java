package cm.aptoide.ptdev.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class AptoideDialog {

    public static DialogFragment badgeDialog(String appName, String status){

        DialogFragment fragment = new DialogBadge();

        Bundle bundle = new Bundle();
        bundle.putString("appName", appName);
        bundle.putString("status", status);
        fragment.setArguments(bundle);

        return fragment;
    }


    public static DialogFragment addStoreDialog(){
        return new AddStoreDialog();
    }

    public static DialogFragment allowRootDialog(){
        return new AllowRootDialog();
    }

    public static DialogFragment pleaseWaitDialog(){
        return new ProgressDialogFragment();
    }

    public static DialogFragment passwordDialog(){ return new PasswordDialog(); }

    public static DialogFragment wrongVersionXmlDialog(){
        return new WrongXmlVersionDialog();
    }

    public static ErrorDialog errorDialog(){
        return new ErrorDialog();
    }

    public static ReplyCommentDialog replyCommentDialog(int commentId, String replyingTo) {
        ReplyCommentDialog fragment = new ReplyCommentDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("commentId", commentId);
        bundle.putString("replyingTo", replyingTo);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static DialogFragment myappInstall(String appName) {

        DialogFragment fragment = new MyAppInstallDialog();

        Bundle bundle = new Bundle();

        bundle.putString("appName", appName);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static DialogFragment addMyAppStore(String repoName) {

        DialogFragment fragment = new MyAppStoreDialog();

        Bundle bundle = new Bundle();
        bundle.putString("repoName", repoName);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static DialogFragment updateUsernameDialog() {
        return new UsernameDialog();
    }

    public static FlagApkDialog flagAppDialog(String uservote) {
        FlagApkDialog flagApkDialog = new FlagApkDialog();
        if(uservote != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FlagApkDialog.USERVOTE_ARGUMENT_KEY, uservote);
            flagApkDialog.setArguments(bundle);
        }
        return flagApkDialog;
    }

    public static DialogFragment InnJooDialog() {
        DialogFragment dialog = new InnJooDialog();
        return dialog;

    }
}
