package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.adapters.InviteFriendsListAdapter;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 20-10-2014.
 */
public class TimeLineFriendsInviteActivity extends ActionBarActivity {
    private InviteFriendsListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_invite_friends);
        rebuildList(savedInstanceState);
        Button invite = (Button) findViewById(R.id.timeline_invite);
        final Context c = this;
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private void rebuildList(final Bundle savedInstanceState) {
        final TimeLineFriendsInviteActivity c = this;
        ListUserFriendsRequest request = new ListUserFriendsRequest();
        request.setOffset(0);
        request.setLimit(150);
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>(){
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                adapter = new InviteFriendsListAdapter(savedInstanceState, c, response.getFriends());
                //adapter.setOnItemClickListener(this);
                adapter.setAdapterView(c.getListView());
            }
        });
    }
    private ListView getListView() {
        return (ListView) findViewById(android.R.id.list);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        adapter.save(outState);
    }
}
