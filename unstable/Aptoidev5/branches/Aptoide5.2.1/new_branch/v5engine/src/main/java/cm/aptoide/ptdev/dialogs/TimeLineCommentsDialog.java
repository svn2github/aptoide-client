package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.TimelineCommentsAdapter;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineCommentsDialog extends DialogFragment {

    public static final String POSTID = "ID";

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
    private ListView lv;

    public void SetComments(List<ApkInstallComments.Comments.Comment> entry){
        lv.setAdapter(new TimelineCommentsAdapter(getActivity(),entry));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context c = getActivity();
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_timelinecomments, null);
        id=savedInstanceState.getLong(POSTID);
        callback.getComment(id);
        lv = (ListView) v.findViewById(R.id.TimeLineListView);
        return new AlertDialog.Builder(c)
                .setView(v)
                .setPositiveButton(R.string.add_comment,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            String s = ((EditText) v.findViewById(R.id.TimeLineCommentEditText))
                                    .getText().toString();
                            callback.commentPost(id,s);
                            callback = null;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

}