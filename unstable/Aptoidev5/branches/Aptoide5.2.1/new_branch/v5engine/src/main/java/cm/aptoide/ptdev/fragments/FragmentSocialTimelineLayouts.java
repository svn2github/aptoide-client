package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.util.List;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.TimeLineNoFriendsInviteActivity;
import cm.aptoide.ptdev.adapters.InviteFriendsListAdapter;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserSettingsRequest;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.RegisterUserFriendsInviteRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by rmateus on 22-10-2014.
 */
public class FragmentSocialTimelineLayouts extends Fragment {


    public static final String LOGOUT_FIRST_ARG = "logoutFirst";
    public static final String LOGGED_IN_ARG = "loggedIn";
    public static final java.lang.String STATE_ARG = "state";
    private View timeline_empty_start_invite;
    private View email_friends;
    private ListView listView;
    private View timeline_empty;
    private InviteFriendsListAdapter adapter;
    private View layout;
    private View layout_with_friends;
    private View loading;


    public enum State{
        NONE, LOGGED_IN, LOGOUT_FIRST, FRIENDS_INVITE
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        State state;
        if(getArguments()!=null){
            state = State.values()[getArguments().getInt(STATE_ARG, 0)];
        }else{
            state = State.NONE;
        }


        switch (state){
            case LOGGED_IN:
                return inflater.inflate(R.layout.page_timeline_logged_in, container, false);
            case LOGOUT_FIRST:
                return inflater.inflate(R.layout.page_timeline_logout_and_login, container, false);
            case FRIENDS_INVITE:
                return inflater.inflate(R.layout.page_timeline_empty, container, false);
            default:
                return inflater.inflate(R.layout.page_timeline_not_logged_in, container, false);

        }
    }

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private TextView friends_using_timeline, join_friends;
    private LinearLayout friends_list;


    public void setFriends(ListUserFriendsJson friends){
        List<ListUserFriendsJson.Friend> friendsList = friends.getActiveFriends() ; 
        
        StringBuilder friendsString;
        int i = 0;

        if(friendsList !=null && !friendsList.isEmpty()){

            int j = i;


            do {
                friendsString = new StringBuilder(friendsList.get(j).getUsername());
                j++;
            }while (friendsString.length() == 0);


            for(i = j; i<friendsList.size() && i < 3 + j; i++){
                String friendName = friendsList.get(i).getUsername();
                if(!TextUtils.isEmpty(friendName)){
                    friendsString.append(", ").append(friendName);
                }
            }

            String text;
            text = getString(R.string.facebook_friends_list_using_timeline);

            if ( friendsList.size() - i <= 0 ){
                text = friendsString.toString() + " " +text;

            }else{
                text=friendsString.toString()
                        +" "+ getString(R.string.and)
                        +" "+ String.valueOf(friendsList.size() - i)
                        +" "+ getString(R.string.more_friends)
                        +" "+ text;
            }

            friends_using_timeline.setText(text);

            DisplayImageOptions options = new DisplayImageOptions.Builder().build();

            for(ListUserFriendsJson.Friend friend : friendsList){
                String avatar = friend.getAvatar();
                final View v = LayoutInflater.from(getActivity()).inflate(R.layout.row_facebook_friends_on_timeline, friends_list, false);
                final ImageView avatarIv = (ImageView) v.findViewById(R.id.user_avatar);
                ImageLoader.getInstance().displayImage(avatar, avatarIv, options );
                friends_list.addView(v);

            }

            friends_list.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            friends_using_timeline.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            join_friends.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));

            join_friends.setVisibility(View.VISIBLE);
        }else{
            friends_using_timeline.setText(getString(R.string.facebook_friends_list_using_timeline_empty));
            join_friends.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        manager.start(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.shouldStop();
    }

    public void getFriends(){
        ListUserFriendsRequest request = new ListUserFriendsRequest();
        String username = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", "");
        manager.execute(request, "facebook-friends-" + username, DurationInMillis.ONE_HOUR ,new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                setFriends((response));
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        State state;
        if(getArguments()!=null){
            state = State.values()[getArguments().getInt(STATE_ARG, 0)];
        }else{
            state = State.NONE;
        }

        switch (state){
            case LOGGED_IN:
                showFriends(view);
                break;
            case FRIENDS_INVITE:
                showInviteFriends(view);
                break;
            default:
            case LOGOUT_FIRST:
                LoginButton fb_login_button = (LoginButton) view.findViewById(R.id.fb_login_button);
                fb_login_button.setFragment(getParentFragment());
                fb_login_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FlurryAgent.logEvent("Social_Timeline_Clicked_On_Login_With_Facebook");
                    }
                });
                break;
        }

    }

    private void rebuildList() {

        ListUserFriendsRequest request = new ListUserFriendsRequest();
        request.setOffset(0);
        request.setLimit(150);

        manager.execute(request, "friendslist" + SecurePreferences.getInstance().getString("access_token", "") , DurationInMillis.ONE_HOUR ,new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {

                loading.setVisibility(View.GONE);
                adapter = new InviteFriendsListAdapter(getActivity(), response.getInactiveFriends());
                //adapter.setOnItemClickListener(this);
                //adapter.setAdapterView(listView);


                if(response.getInactiveFriends().isEmpty()){
                    layout.setVisibility(View.VISIBLE);
                    email_friends.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TimeLineNoFriendsInviteActivity.sendMail(getActivity());
                        }
                    });
                }else{
                    layout_with_friends.setVisibility(View.VISIBLE);
                    listView.setAdapter(adapter);
                }

            }
        });

    }

    private void showInviteFriends(final View view) {
        loading = view.findViewById(android.R.id.empty);
        email_friends = view.findViewById(R.id.email_friends);
        listView = (ListView) view.findViewById(android.R.id.list);
        layout = view.findViewById(R.id.layout_no_friends);
        layout_with_friends = view.findViewById(R.id.layout_with_friends);
        View footer_friends_to_invite = LayoutInflater.from(getActivity()).inflate(R.layout.footer_invite_friends, null);
        listView.addFooterView(footer_friends_to_invite);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        rebuildList();
        Button invite = (Button) footer_friends_to_invite.findViewById(R.id.timeline_invite);
        final Context c = getActivity();

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Social_Timeline_Clicked_On_Invite_Friends");
                RegisterUserFriendsInviteRequest request = new RegisterUserFriendsInviteRequest();
                long[] ids = listView.getCheckItemIds();
                if(ids.length>0) {
                    for (long id : ids) {
                        request.addEmail(adapter.getItem((int) id).getEmail());
                    }
                    manager.execute(request, new TimelineRequestListener<GenericResponse>() {
                        private void cleanUI(){
                            view.findViewById(R.id.layout_with_friends).setVisibility(View.VISIBLE);
                            view.findViewById(android.R.id.empty).setVisibility(View.GONE);
                        }

                        @Override
                        protected void caseFAIL() {
                            cleanUI();
                        }
                        @Override
                        protected void caseOK(GenericResponse response) {
                            cleanUI();
                            Toast.makeText(c, c.getString(R.string.facebook_timeline_friends_invited), Toast.LENGTH_LONG).show();
                        }
                    });
                    view.findViewById(R.id.layout_with_friends).setVisibility(View.GONE);
                    view.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(c, c.getString(R.string.select_friends_to_invite), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public interface Callback {

        public void onStartTimeline();

    }


    public void showFriends(View view){
        friends_using_timeline = (TextView) view.findViewById(R.id.friends_using_timeline);
        join_friends = (TextView) view.findViewById(R.id.join_friends);
        getFriends();

//          lv = (ListView) findViewById(R.id.TimeLineListView);
        friends_list = (LinearLayout) view.findViewById(R.id.friends_list);

        Button start_timeline = (Button) view.findViewById(R.id.start_timeline);

        start_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Social_Timeline_Clicked_On_Join_Social_Timeline");
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ChangeUserSettingsRequest request = new ChangeUserSettingsRequest();
                        request.addTimeLineSetting(ChangeUserSettingsRequest.TIMELINEACTIVE);
                        request.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
                        try {
                            request.loadDataFromNetwork();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Preferences.putBooleanAndCommit(Preferences.TIMELINE_ACEPTED_BOOL, true);
                ((Callback) getParentFragment()).onStartTimeline();
            }
        });
    }
}
