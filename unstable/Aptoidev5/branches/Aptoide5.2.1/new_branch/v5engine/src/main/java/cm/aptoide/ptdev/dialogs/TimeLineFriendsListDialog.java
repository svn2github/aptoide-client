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
import android.widget.ListView;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.FriendsListAdapter;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineFriendsListDialog extends DialogFragment {

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
    private ListView lv;

    public void setFriends(ListUserFriendsJson friends){
        this.lv.setAdapter(new FriendsListAdapter(getActivity(), friends));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context c = getActivity();
        final View v = LayoutInflater.from(c).inflate(R.layout.dialog_timeline_friends, null);

        lv = (ListView) v.findViewById(R.id.TimeLineListView);
        return new AlertDialog.Builder(c)
                .setView(v)
                .setTitle(R.string.do_you_accept_timeline)
                .setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.acceptTimeLine(true);
                            callback = null;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.acceptTimeLine(false);
                            callback = null;
                        }
                    }
                })
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        callback.getFriends();
    }
}