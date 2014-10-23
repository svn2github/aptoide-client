package cm.aptoide.ptdev.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.LoginButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;

import java.util.List;

import java.util.concurrent.Executors;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserSettingsRequest;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by rmateus on 22-10-2014.
 */
public class FragmentSocialTimelineLayouts extends Fragment {


    public static final String LOGOUT_FIRST_ARG = "logoutFirst";
    public static final String LOGGED_IN_ARG = "loggedIn";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        boolean loggedin = getArguments().getBoolean(LOGGED_IN_ARG, false);
        if (loggedin) {
            return inflater.inflate(R.layout.page_timeline_logged_in, container, false);
        } else {

            boolean logoutFirst = getArguments().getBoolean(LOGOUT_FIRST_ARG, false);

            if (logoutFirst) {
                return inflater.inflate(R.layout.page_timeline_logout_and_login, container, false);
            } else {
                return inflater.inflate(R.layout.page_timeline_not_logged_in, container, false);
            }
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
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                setFriends((response));
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments().getBoolean(LOGGED_IN_ARG, false)){


            friends_using_timeline = (TextView) view.findViewById(R.id.friends_using_timeline);
            join_friends = (TextView) view.findViewById(R.id.join_friends);
            getFriends();

//          lv = (ListView) findViewById(R.id.TimeLineListView);
            friends_list = (LinearLayout) view.findViewById(R.id.friends_list);

            Button start_timeline = (Button) view.findViewById(R.id.start_timeline);

            start_timeline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Preferences.putBooleanAndCommit(Preferences.TIMELINE_ACEPTED_BOOL, true);
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            ChangeUserSettingsRequest request = new ChangeUserSettingsRequest();
                            request.addTimeLineSetting(ChangeUserSettingsRequest.TIMELINEACTIVE);
                            request.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
                            try {
                                request.loadDataFromNetwork();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((Callback) getParentFragment()).onStartTimeline();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });


        } else {

            LoginButton fb_login_button = (LoginButton) view.findViewById(R.id.fb_login_button);
            fb_login_button.setFragment(getParentFragment());
        }
    }

    public interface Callback {

        public void onStartTimeline();

    }
}
