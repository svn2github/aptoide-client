package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentCallback;

/**
 * Created by jcosta on 01-07-2014.
 */
public class ReplyCommentDialog extends DialogFragment {

    private AddCommentCallback addCommentCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        addCommentCallback = (AddCommentCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        addCommentCallback = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String replyingTo = getArguments().getString("replyingTo");
        final int commentId = getArguments().getInt("commentId");

        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_reply_comment, null);

        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Reply " + replyingTo + " comment")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String replyText = ((EditText) v.findViewById(R.id.edit_reply)).getText().toString();

                        if (addCommentCallback != null) {
                            addCommentCallback.addComment(replyText, Integer.toString(commentId));
                        }
                    }
                }).create();

        return builder;
    }

}
