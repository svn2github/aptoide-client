package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;

import java.util.List;

import cm.aptoide.ptdev.adapters.InviteFriendsListAdapter;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.RegisterUserFriendsInviteRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 20-10-2014.
 */
public class TimeLineFriendsInviteActivity extends ActionBarActivity {
    private InviteFriendsListAdapter adapter;
    private TextView friends_using_timeline;
    private TextView friends_to_invite;
    private LinearLayout friends_list;
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_invite_friends);
        listView = getListView();

        friends_list = (LinearLayout) findViewById(R.id.friends_list);
        friends_using_timeline = (TextView) findViewById(R.id.friends_using_timeline);
        friends_to_invite = (TextView) findViewById(R.id.friends_to_invite);
        View footer_friends_to_invite = LayoutInflater.from(this).inflate(R.layout.footer_invite_friends, null);
        listView.addFooterView(footer_friends_to_invite);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        rebuildList(savedInstanceState);
        Button invite = (Button) footer_friends_to_invite.findViewById(R.id.timeline_invite);
        final Context c = this;
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserFriendsInviteRequest request = new RegisterUserFriendsInviteRequest();
                for(long id : listView.getCheckItemIds()){
                    Log.d("pois","id:"+id);
                    Log.d("pois","id:"+adapter.getItem((int) id).getEmail());
                    request.addEmail(adapter.getItem((int) id).getEmail());
                }
                manager.execute(request,new TimelineRequestListener<GenericResponse>(){
                    @Override
                    protected void caseOK(GenericResponse response) {
                        Toast.makeText(c, c.getString(R.string.facebook_timeline_friends_invited), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.invite_friends);

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
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                adapter = new InviteFriendsListAdapter(c, response.getInactiveFriends());
                //adapter.setOnItemClickListener(this);
                //adapter.setAdapterView(listView);

                listView.setAdapter(adapter);
                setFriends(response.getActiveFriends());

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });

            }
        });

    }
    private ListView getListView() {
        return (ListView) findViewById(android.R.id.list);
    }

    public void setFriends(List<ListUserFriendsJson.Friend> activeFriendsList){
        StringBuilder friendsString;
        int i = 0;
        String text;
        if(activeFriendsList !=null && !activeFriendsList.isEmpty() ){
            int j = i;

            do {
                friendsString = new StringBuilder(activeFriendsList.get(j).getUsername());
                j++;
            }while (friendsString.length() == 0);

           for(i = j; i<activeFriendsList.size() && i < 3 + j; i++){
                String friendName = activeFriendsList.get(i).getUsername();
                if(!TextUtils.isEmpty(friendName)){
                    friendsString.append(", ").append(friendName);
                }
            }

            text = getString(R.string.friends_that_use_timeline);

            if ( activeFriendsList.size() - i <= 0 ){
                text = friendsString.toString() + " " +text;

            }else{
                text=friendsString.toString()
                        +" "+ getString(R.string.and)
                        +" "+ String.valueOf(activeFriendsList.size() - i)
                        +" "+ getString(R.string.more_friends)
                        +" "+ text;
            }

            friends_using_timeline.setText(text);

            DisplayImageOptions options = new DisplayImageOptions.Builder().build();

            for(ListUserFriendsJson.Friend friend : activeFriendsList){
                String avatar = friend.getAvatar();
                final View v = LayoutInflater.from(this).inflate(R.layout.row_facebook_friends_on_timeline, friends_list, false);
                final ImageView avatarIv = (ImageView) v.findViewById(R.id.user_avatar);
                ImageLoader.getInstance().displayImage(avatar, avatarIv, options );
                friends_list.addView(v);

            }

            friends_list.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            friends_using_timeline.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }else{
            friends_using_timeline.setText(getString(R.string.facebook_friends_list_using_timeline_empty));
        }

        if(adapter.isEmpty()){
            friends_to_invite.setVisibility(View.GONE);
        }else{
            friends_to_invite.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

}
