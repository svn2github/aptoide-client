package cm.aptoide.ptdev;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.adapters.EndlessWrapperAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.TimeLineCommentsDialog;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallCommentRequest;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallLikeRequest;
import cm.aptoide.ptdev.webservices.timeline.GetUserApkInstallCommentsRequest;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */
public class TimelineActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, TimeLineManager {
    private static final int COMMENTSLIMIT = 10;
    private static final String COMMENTSDIALOGTAG = "CD";

    private ArrayList<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>();
    private EndlessWrapperAdapter adapter;
    private Number lastId;
    private Number firstId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean mListShown = false;
    private View mProgressContainer;

    public void onItemsReady(ArrayList<TimelineListAPKsJson.UserApk> data) {
        if (data.isEmpty()) {
            adapter.stopAppending();
        } else {
            if(firstId == null){
                firstId =  data.get(0).getInfo().getId();
            }
            apks.addAll(data);
            lastId = apks.get(apks.size()-1).getInfo().getId();
            adapter.onDataReady();
        }   // Tell the EndlessAdapter to
            // remove it's pending
            // view and call
            // notifyDataSetChanged()
    }

    public void onItemsReadyRefresh(ArrayList<TimelineListAPKsJson.UserApk> data) {
        apks.addAll(0, data);
        firstId = apks.get(0).getInfo().getId();
        adapter.notifyDataSetChanged();
        // remove it's pending
        // view and call
        // notifyDataSetChanged()
    }



    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private ListApksInstallsRequest listAPKsInstallsRequest;


    private RequestListener<TimelineListAPKsJson> listener = new RequestListener<TimelineListAPKsJson>() {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
            onItemsReady(new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks()));
            swipeRefreshLayout.setRefreshing(false);
            if (!mListShown) setListShown(true, true);

        }

    };

    private RequestListener<TimelineListAPKsJson> listenerRefresh = new RequestListener<TimelineListAPKsJson>() {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
            apks.clear();
            onItemsReadyRefresh(new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks()));
            swipeRefreshLayout.setRefreshing(false);

            if (!mListShown) setListShown(true, true);

        }

    };

    public void runRequest() {
        listAPKsInstallsRequest = new ListApksInstallsRequest();

        if(lastId!=null) {
            listAPKsInstallsRequest.setOffset_id(String.valueOf(lastId.intValue()));
            listAPKsInstallsRequest.setDownwardsDirection();
        }

        manager.execute(listAPKsInstallsRequest, listener);
    }

    public void refreshRequest() {
        listAPKsInstallsRequest = new ListApksInstallsRequest();

        //listAPKsInstallsRequest.setOffset_id(String.valueOf(firstId.intValue()));
        //listAPKsInstallsRequest.setUpwardsDirection();


        adapter.notifyDataSetChanged();
        adapter.restartAppending();

        manager.execute(listAPKsInstallsRequest, listenerRefresh);
    }


    private void setListShown(boolean shown, boolean animate) {

        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
                swipeRefreshLayout.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                swipeRefreshLayout.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                       this, android.R.anim.fade_in));
                swipeRefreshLayout.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                swipeRefreshLayout.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        adapter = new EndlessWrapperAdapter(this, apks);
        adapter.setRunInBackground(false);
        setContentView(R.layout.page_timeline);
        mProgressContainer = findViewById(android.R.id.empty);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.timeline_PullToRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.default_progress_bar_color, R.color.custom_color, R.color.default_progress_bar_color, R.color.custom_color);


        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        setListShown(false, false);

        Bundle addAccountOptions=new Bundle();
        if(AptoideUtils.isLoggedIn(this)){
            if (PreferenceManager.getDefaultSharedPreferences(this).getString("loginType", null).equals("FACEBOOK")){
                init();
                return;
            }
            else{
                addAccountOptions.putBoolean(LoginActivity.OPTIONS_LOGOUT_BOOL,true);
            }
        }
        addAccountOptions.putBoolean(LoginActivity.OPTIONS_FASTBOOK_BOOL, true);
        AccountManager.get(this).addAccount(
                Aptoide.getConfiguration().getAccountType(),
                AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
                null, addAccountOptions, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {

                        try {
                            String name = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);

                            if(TextUtils.isEmpty(name)){
                                finish();
                            }else{
                                init();
                            }

                        } catch (Exception e) {
                            finish();
                        }

                    }
                },
                new Handler(Looper.getMainLooper())
        );

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.social_timeline);

    }




    private void init() {

        ListView lv = (ListView) findViewById(R.id.timeline_list);
        lv.setAdapter(adapter);

        //force loading
        adapter.getView(0, null, null);

    }


    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onRefresh() {
        refreshRequest();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /* *************** Methods of the TimeLineManager Interface *************** */

    @Override
    public void likePost(long id){
        likeRequestPost(id, AddUserApkInstallLikeRequest.LIKE);
    }
    @Override
    public void unlikePost(long id){
        likeRequestPost(id,AddUserApkInstallLikeRequest.UNLIKE);
    }
    private void likeRequestPost(long id,String like){
        AddUserApkInstallLikeRequest request = new AddUserApkInstallLikeRequest();
        request.setLike(like);
        request.setPostId(id);
        manager.execute(request, new NothingRequestListener<GenericResponse>());
    }
    @Override
    public void commentPost(long id,String comment){
        AddUserApkInstallCommentRequest request = new AddUserApkInstallCommentRequest();
        request.setPostId(id);
        request.setComment(comment);
        manager.execute(request,new NothingRequestListener<GenericResponse>());

    }
    @Override
    public void getComment(long id) {
        GetUserApkInstallCommentsRequest request = new GetUserApkInstallCommentsRequest();
        request.setPostID(id);
        request.setPostLimit(COMMENTSLIMIT);
        manager.execute(request,new GetUserApkInstallCommentsRequestListener());
    }
    @Override
    public void openCommentsDialog(long id){
        new TimeLineCommentsDialog().show(getSupportFragmentManager(), COMMENTSDIALOGTAG);
    }

    /* *************** Methods of the TimeLineManager Interface *************** */

    public class NothingRequestListener<E> extends TimelineRequestListener<E> {
        @Override
        protected void caseOK(E response) {

        }
    }
    /**
     * Listener to be used on ChangeUserSettingsRequest and GetUserSettingsRequest
     */
    public class GetUserSettingsRequestListener extends TimelineRequestListener<GetUserSettingsJson> {
        @Override
        protected void caseOK(GetUserSettingsJson response) {
            if (((GetUserSettingsJson)response).getResults() != null) {
                boolean serverResponse = ((GetUserSettingsJson)response).getResults().getTimeline().equals("active");
                OnGetServerSetting(serverResponse);
            }
        }
    }

    public class GetUserApkInstallCommentsRequestListener extends TimelineRequestListener<ApkInstallComments> {
        @Override
        protected void caseOK(ApkInstallComments response) {
            if (((ApkInstallComments)response).getComment()!=null &&
                    ((ApkInstallComments)response).getComment().getEntry() !=null) {
                OnGetUserApkInstallComments(((ApkInstallComments) response).getComment().getEntry());
            }
        }
    }

    /**
     * Called be the listener of ChangeUserSettingsRequest and GetUserSettingsRequest
     */
    private void OnGetServerSetting(boolean timeline){
        //TODO
    }

    private void OnGetUserApkInstallComments(List<ApkInstallComments.Comments.Comment> entry) {
        ((TimeLineCommentsDialog) getSupportFragmentManager().findFragmentByTag(COMMENTSDIALOGTAG))
                .SetComments(entry);
    }
}
