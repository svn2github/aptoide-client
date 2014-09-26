package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import cm.aptoide.ptdev.adapters.EndlessWrapperAdapter;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
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
                firstId =  apks.get(0).getInfo().getId();
            }
            apks.addAll(data);
            lastId = apks.get(apks.size()).getInfo().getId();
            adapter.onDataReady();
        }   // Tell the EndlessAdapter to
            // remove it's pending
            // view and call
            // notifyDataSetChanged()
    }

    public void onItemsReadyRefresh(ArrayList<TimelineListAPKsJson.UserApk> data) {
        apks.addAll(data);
        firstId = apks.get(0).getInfo().getId();
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

        if(lastId!=null) {
            listAPKsInstallsRequest.setOffset_id(String.valueOf(lastId.intValue()));
            listAPKsInstallsRequest.setDownwardsDirection();
        }

        manager.execute(listAPKsInstallsRequest, listener);
    }

    public void refreshRequest() {
        listAPKsInstallsRequest.setOffset_id(String.valueOf(firstId.intValue()));
        listAPKsInstallsRequest.setUpwardsDirection();
        manager.execute(listAPKsInstallsRequest, listenerRefresh);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EndlessWrapperAdapter(this, apks);


        adapter.setRunInBackground(false);
        setContentView(R.layout.page_timeline);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.timeline_PullToRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        ListView lv = (ListView) findViewById(R.id.timeline_list);
        lv.setAdapter(adapter);
        listAPKsInstallsRequest = new ListApksInstallsRequest();


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
}
