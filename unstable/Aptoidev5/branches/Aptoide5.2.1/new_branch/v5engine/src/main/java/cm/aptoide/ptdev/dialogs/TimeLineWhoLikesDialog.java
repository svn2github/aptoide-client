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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.TimeLineFriendsListAdapter;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.Friend;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineWhoLikesDialog extends DialogFragment {

    public static final String POSTID = "ID";
    public static final String LIKES = "LIKES";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    public void setCallback(TimeLineManager callback) {
        this.callback = callback;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private TimeLineManager callback;
    private long id;
    private ListView lv;
    private TextView likes;
    private ImageButton send_button;
    private int likesNumber;

    public void setFriends(List<Friend> entry){
        lv.setAdapter(new TimeLineFriendsListAdapter(getActivity(), entry));
        lv.setVisibility(View.VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle( DialogFragment.STYLE_NORMAL, R.style.TimelineCommentsDialog );
        final Context c = getActivity();

        final View dialogView = LayoutInflater.from(c).inflate(R.layout.dialog_wholikes, null);
        likesNumber = Integer.valueOf(getArguments().getString(LIKES));
        id=getArguments().getLong(POSTID);
        lv = (ListView) dialogView.findViewById(R.id.TimeLineListView);
        likes = (TextView) dialogView.findViewById(R.id.likes);

        if(likesNumber == 1) {
            likes.setText(likesNumber + " " + getString(R.string.timeline_like));
        }else{
            likes.setText(likesNumber + " " + getString(R.string.likes));
        }

        return new AlertDialog.Builder(c)
                .setView(dialogView)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        TimeLineManager parentFragment = (TimeLineManager) getParentFragment();
        parentFragment.openCommentsDialog(id,getArguments().getInt(TimeLineCommentsDialog.POSITION));
    }

    @Override
    public void onResume() {
        super.onResume();
        TimeLineManager parentFragment = (TimeLineManager) getParentFragment();
        parentFragment.getWhoLiked(id);
    }
}