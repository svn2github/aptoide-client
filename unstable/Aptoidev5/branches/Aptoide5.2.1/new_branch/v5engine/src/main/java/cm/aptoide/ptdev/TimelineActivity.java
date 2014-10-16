package cm.aptoide.ptdev;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.adapters.EndlessWrapperAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.TimeLineCommentsDialog;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallCommentRequest;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallLikeRequest;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserApkInstallStatusRequest;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserSettingsRequest;
import cm.aptoide.ptdev.webservices.timeline.GetUserApkInstallCommentsRequest;
import cm.aptoide.ptdev.webservices.timeline.GetUserSettingsRequest;
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
    private static final String TIMELINEFRIENDSLISTDIALOGTAG = "TLFLD";

    private ArrayList<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>();
    private EndlessWrapperAdapter adapter;
    private Number lastId;
    private Number firstId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean mListShown = false;
    private View mProgressContainer;
    private boolean showDialog;

    public void onItemsReady(ArrayList<TimelineListAPKsJson.UserApk> data) {
        if (data.isEmpty()) {
            adapter.stopAppending();
        } else {
            if (firstId == null) {
                firstId = data.get(0).getInfo().getId();
            }
            apks.addAll(data);
            lastId = apks.get(apks.size() - 1).getInfo().getId();
            adapter.onDataReady();
        }   // Tell the EndlessAdapter to
        // remove it's pending
        // view and call
        // notifyDataSetChanged()
    }

    public void onItemsReadyRefresh(ArrayList<TimelineListAPKsJson.UserApk> data) {
        apks.addAll(0, data);
        if (apks.size() > 0) {
            firstId = apks.get(0).getInfo().getId();
            adapter.notifyDataSetChanged();
        }
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

        if (lastId != null) {
            listAPKsInstallsRequest.setOffset_id(String.valueOf(lastId.intValue()));
            listAPKsInstallsRequest.setDownwardsDirection();
        }

        manager.execute(listAPKsInstallsRequest, listener);
    }

    public void refreshRequest() {
        listAPKsInstallsRequest = new ListApksInstallsRequest();

        //listAPKsInstallsRequest.setOffset(String.valueOf(firstId.intValue()));
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
        Bundle addAccountOptions = null;
        if (AptoideUtils.isLoggedIn(this)) {
            if ("FACEBOOK".equals(PreferenceManager.getDefaultSharedPreferences(this).getString("loginType", null))) {
                GetUserSettingsRequest request = new GetUserSettingsRequest();
                request.addSetting(GetUserSettingsRequest.TIMELINE);
                manager.execute(request, new GetUserSettingsRequestListener());
                return;
            } else {
                addAccountOptions = new Bundle();
                addAccountOptions.putBoolean(LoginActivity.OPTIONS_LOGOUT_BOOL, true);
            }
        }
        if (addAccountOptions == null)
            addAccountOptions = new Bundle();
        addAccountOptions.putBoolean(LoginActivity.OPTIONS_FASTBOOK_BOOL, true);
        AccountManager.get(this).addAccount(
                Aptoide.getConfiguration().getAccountType(),
                AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
                null, addAccountOptions, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        String name = "";

                        try {
                            name = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
                        } catch (Exception e) {
                            finish();
                        }

                        if (TextUtils.isEmpty(name)) {
                            finish();
                        } else {
                            init();
                        }
                    }
                },
                new Handler(Looper.getMainLooper())
        );
    }

    private void init() {
        adapter = new EndlessWrapperAdapter(this, apks);
        adapter.setRunInBackground(false);
        setContentView(R.layout.page_timeline);
        mProgressContainer = findViewById(android.R.id.empty);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.timeline_PullToRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.default_progress_bar_color,
                R.color.custom_color, R.color.default_progress_bar_color, R.color.custom_color);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        setListShown(false, false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.social_timeline);


        ListView lv = (ListView) findViewById(R.id.timeline_list);
        lv.setAdapter(adapter);


        //force loading
        adapter.getView(0, null, null);
        if (!Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {


                    ChangeUserSettingsRequest request = new ChangeUserSettingsRequest();
                    request.addTimeLineSetting(ChangeUserSettingsRequest.TIMELINEACTIVE);
                    request.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());


                    try {
                        request.loadDataFromNetwork();
                        Preferences.putBooleanAndCommit(Preferences.TIMELINE_ACEPTED_BOOL, true);

                        if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).contains(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL)) {
                            Preferences.putBooleanAndCommit(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            init();
        } else {
            finish();
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        if (i == R.id.menu_invite_friends) {

        }
        return super.onOptionsItemSelected(item);
    }

    /* *************** Methods of the TimeLineManager Interface *************** */

    @Override
    public void hidePost(long id) {
        changeUserApkInstallStatusPost(id, ChangeUserApkInstallStatusRequest.STATUSHIDDEN);
    }

    @Override
    public void unHidePost(long id) {
        changeUserApkInstallStatusPost(id, ChangeUserApkInstallStatusRequest.STATUSACTIVE);
    }

    private void changeUserApkInstallStatusPost(long id, String status) {
        ChangeUserApkInstallStatusRequest request = new ChangeUserApkInstallStatusRequest();
        request.setPostStatus(status);
        request.setPostId(id);
        manager.execute(request, new TimelineRequestListener<GenericResponse>());
    }

    @Override
    public void likePost(long id) {
        likeRequestPost(id, AddUserApkInstallLikeRequest.LIKE);
    }

    @Override
    public void unlikePost(long id) {
        likeRequestPost(id, AddUserApkInstallLikeRequest.UNLIKE);
    }

    private void likeRequestPost(long id, String like) {
        AddUserApkInstallLikeRequest request = new AddUserApkInstallLikeRequest();
        request.setLike(like);
        request.setPostId(id);
        manager.execute(request, new TimelineRequestListener<GenericResponse>());
    }

    @Override
    public void commentPost(long id, String comment) {
        AddUserApkInstallCommentRequest request = new AddUserApkInstallCommentRequest();
        request.setPostId(id);
        request.setComment(comment);
        manager.execute(request, new TimelineRequestListener<GenericResponse>());

    }

    @Override
    public void getComments(long id) {
        GetUserApkInstallCommentsRequest request = new GetUserApkInstallCommentsRequest();
        request.setPostID(id);
        request.setPostLimit(COMMENTSLIMIT);
        manager.execute(request, new GetUserApkInstallCommentsRequestListener());
    }

    @Override
    public void openCommentsDialog(long id) {
        Bundle args = new Bundle();
        args.putLong(TimeLineCommentsDialog.POSTID, id);
        TimeLineCommentsDialog commentsDialog = new TimeLineCommentsDialog();
        commentsDialog.setArguments(args);
        commentsDialog.show(getSupportFragmentManager(), COMMENTSDIALOGTAG);
    }

    /* *************** Methods of the TimeLineManager Interface *************** */

    public class GetUserSettingsRequestListener extends TimelineRequestListener<GetUserSettingsJson> {
        @Override
        protected void caseOK(GetUserSettingsJson response) {
            if (response.getResults() != null) {
                boolean serverResponse = response.getResults().getTimeline().equals("active");
                if (serverResponse) {
                    init();
                } else {
                    startTimeLineFriendsListActivity();
                }
            }
        }

        @Override
        protected void caseFAIL() {
            finish();
        }
    }

    private void startTimeLineFriendsListActivity() {
        startActivityForResult(new Intent(this, TimeLineFriendsListActivity.class), 0);
    }

    public class GetUserApkInstallCommentsRequestListener extends TimelineRequestListener<ApkInstallComments> {
        @Override
        protected void caseOK(ApkInstallComments response) {
            if ((response).getComments() != null) {
                ((TimeLineCommentsDialog) getSupportFragmentManager().findFragmentByTag(COMMENTSDIALOGTAG))
                        .SetComments((response).getComments());
            }
        }
    }
}
