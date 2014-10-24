package cm.aptoidetv.pt;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AllowRootDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.DialogBadge;
import cm.aptoide.ptdev.dialogs.ErrorDialog;
import cm.aptoide.ptdev.dialogs.FlagApkDialog;
import cm.aptoide.ptdev.dialogs.MyAppInstallDialog;
import cm.aptoide.ptdev.dialogs.MyAppStoreDialog;
import cm.aptoide.ptdev.dialogs.PasswordDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.dialogs.ReplyCommentDialog;
import cm.aptoide.ptdev.dialogs.UsernameDialog;
import cm.aptoide.ptdev.dialogs.WrongXmlVersionDialog;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-10-2013
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class AptoideDialogTV extends AptoideDialog {

    public static DialogFragment badgeDialogTV(String appName, String status){

        DialogFragment fragment = new DialogBadgeTV();

        Bundle bundle = new Bundle();
        bundle.putString("appName", appName);
        bundle.putString("status", status);
        fragment.setArguments(bundle);

        return fragment;
    }
}
