package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.TimeLineNoFriendsInviteActivity;
import cm.aptoide.ptdev.adapters.TimeLineFriendsListAdapter;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.RegisterUserFriendsInviteRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by rmateus on 24-10-2014.
 */
public class FragmentFriendsInvite extends Fragment {


    private TimeLineFriendsListAdapter adapter;
    private TextView friends_using_timeline;
    private TextView friends_to_invite;
    private LinearLayout friends_list;
    private ListView listView;
    private View layout;
    private View timeline_empty_start_invite;
    private View timeline_empty;
    private View email_friends;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_timeline_empty, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        timeline_empty_start_invite = view.findViewById(R.id.timeline_empty_start_invite);
        email_friends = view.findViewById(R.id.email_friends);
        timeline_empty = view.findViewById(R.id.timeline_empty);
        listView = (ListView) view.findViewById(android.R.id.list);
        layout = view.findViewById(R.id.layout_no_friends);
        View footer_friends_to_invite = LayoutInflater.from(getActivity()).inflate(R.layout.footer_invite_friends, null);
        listView.addFooterView(footer_friends_to_invite);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        rebuildList(savedInstanceState);
        Button invite = (Button) footer_friends_to_invite.findViewById(R.id.timeline_invite);
        final Context c = getActivity();
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Social_Timeline_Clicked_On_Invite_Friends");
                RegisterUserFriendsInviteRequest request = new RegisterUserFriendsInviteRequest();
                for(long id : listView.getCheckItemIds()){
                    request.addEmail(adapter.getItem((int) id).getEmail());
                }
                manager.execute(request,new TimelineRequestListener<GenericResponse>(){
                    @Override
                    protected void caseOK(GenericResponse response) {
                        Toast.makeText(c, c.getString(R.string.facebook_timeline_friends_invited), Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

    private void rebuildList(final Bundle savedInstanceState) {

        ListUserFriendsRequest request = new ListUserFriendsRequest();
        request.setOffset(0);
        request.setLimit(150);


        manager.execute(request, "friendslist" + SecurePreferences.getInstance().getString("access_token", "") , DurationInMillis.ONE_HOUR ,new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {



                adapter = new TimeLineFriendsListAdapter(getActivity(), response.getInactiveFriends());
                //adapter.setOnItemClickListener(this);
                //adapter.setAdapterView(listView);


                if(response.getInactiveFriends().isEmpty()){
                    layout.setVisibility(View.VISIBLE);
                    email_friends.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getActivity(), TimeLineNoFriendsInviteActivity.class));
                        }
                    });
                    timeline_empty_start_invite.setVisibility(View.GONE);
                    timeline_empty.setVisibility(View.GONE);
                }else{
                    timeline_empty_start_invite.setVisibility(View.VISIBLE);
                    timeline_empty.setVisibility(View.VISIBLE);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        }
                    });
                }

            }
        });

    }

}
