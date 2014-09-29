package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.List;

import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineCommentsDialog extends DialogFragment {

    public static final java.lang.String POSTID = "ID";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (TimeLineManager) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private TimeLineManager callback;
    private long id;

    public void SetComments(List<ApkInstallComments.Comments.Comment> entry){

    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        id=savedInstanceState.getLong(POSTID);
        callback.getComment(id);
        return super.onCreateDialog(savedInstanceState);
    }

/*    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.row_comment, null);
        return new AlertDialog.Builder(getActivity())
                .setView(v).create();
                *//*if (callback != null) {
                    callback.();
                    callback = null;
                }*//*

    }*/
}