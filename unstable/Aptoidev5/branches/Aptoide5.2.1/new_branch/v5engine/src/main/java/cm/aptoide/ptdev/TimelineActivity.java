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
import android.widget.ListView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import cm.aptoide.ptdev.adapters.EndlessWrapperAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */
public class TimelineActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener{

    private ArrayList<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>();
    private EndlessWrapperAdapter adapter;
    private Number lastId;
    private Number firstId;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        }

    };

    private RequestListener<TimelineListAPKsJson> listenerRefresh = new RequestListener<TimelineListAPKsJson>() {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
            onItemsReadyRefresh(new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks()));
            swipeRefreshLayout.setRefreshing(false);
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
        apks.clear();

        adapter.notifyDataSetChanged();
        adapter.restartAppending();

        manager.execute(listAPKsInstallsRequest, listenerRefresh);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        adapter = new EndlessWrapperAdapter(this, apks);
        adapter.setRunInBackground(false);
        setContentView(R.layout.page_timeline);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.timeline_PullToRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);


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
        addAccountOptions.putBoolean(LoginActivity.OPTIONS_FASTBOOK_BOOL,true);
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
}
