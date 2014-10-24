package cm.aptoide.ptdev;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by asantos on 20-10-2014.
 */

public class TimeLineFriendsInviteActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_no_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.invite_friends));


    }

    public void SendMail(View view) {
        String subject = getString(R.string.aptoide_timeline);
        String text;

        String username = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", null);
        String Invitation= getString(R.string.timeline_email_invitation);
        String whatIs= getString(R.string.whats_timeline);
        String TOS= getString(R.string.facebook_tos);

        String howTo= getString(R.string.timeline_email_how_to_join);
        String step1= getString(R.string.timeline_email_step1) +
                "<a href=\"http://m.aptoide.com/install.\">"+getString(R.string.install) +" Aptoide</a>";
        String step2= getString(R.string.timeline_email_step2);

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT,subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                username+"\n"+
                Invitation+"\n"+
                whatIs+"\n"+
                TOS+"\n"+
                howTo+"\n"+
                step1+"\n"+
                step2);

        try {
            startActivity(emailIntent);
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.feedback_no_email, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home || i == R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

/*

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
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>(){
            @Override
            protected void caseFAIL() {
                Toast.makeText(c,R.string.error_occured,Toast.LENGTH_LONG);
                finish();
            }

            @Override
            protected void caseOK(ListUserFriendsJson response) {
                adapter = new InviteFriendsListAdapter(c, response.getInactiveFriends());
                //adapter.setOnItemClickListener(this);
                //adapter.setAdapterView(listView);

                listView.setAdapter(adapter);
                setFriends(response.getActiveFriends());
                c.findViewById(android.R.id.empty).setVisibility(View.GONE);
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
        Log.d("pois","setF");
        if(activeFriendsList !=null && !activeFriendsList.isEmpty() ){
            friends_using_timeline.setText(getString(R.string.friends_that_use_timeline));

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
            friends_to_invite.setVisibility(View.VISIBLE);
        }else{
            Log.d("pois","setF Empty");
            friends_using_timeline.setText(getString(R.string.facebook_friends_list_using_timeline_empty));
            friends_to_invite.setVisibility(View.GONE);
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
*/
