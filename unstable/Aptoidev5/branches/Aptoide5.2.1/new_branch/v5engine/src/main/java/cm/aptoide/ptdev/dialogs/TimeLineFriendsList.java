package cm.aptoide.ptdev.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.FriendsListAdapter;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineFriendsList extends Activity {
    private TimeLineManager callback;
    private ListView lv;
    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

    public void setFriends(ListUserFriendsJson friends){
        this.lv.setAdapter(new FriendsListAdapter(this, friends));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_timeline_friends);
        getFriends();
        lv = (ListView) findViewById(R.id.TimeLineListView);
    }
    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.shouldStop();
    }
    public void getFriends(){
        ListUserFriendsRequest request = new ListUserFriendsRequest();
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                setFriends((response));
            }
        });
    }

}