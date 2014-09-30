package cm.aptoide.ptdev;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.google.api.client.extensions.android.http.AndroidHttp;

import java.util.ArrayList;

import cm.aptoide.ptdev.adapters.TimelineAdapter;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 25-09-2014.
 */
public class TestActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EndlessAdapterCustomTaskFragment()).commit();
        }

    }

    public static class EndlessAdapterCustomTaskFragment extends ListFragment {
        DemoAdapter adapter=null;
        ArrayList<TimelineListAPKsJson.UserApk> items=null;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);
            if (adapter == null) {
                items=new ArrayList<TimelineListAPKsJson.UserApk>();

                adapter = new DemoAdapter(items);
                adapter.setRunInBackground(false); // Tell the adapter
// we will handle
// starting the
// background task
            }
            setListAdapter(adapter);
        }
        class DemoAdapter extends EndlessAdapter implements
                IItemsReadyListener {
            private RotateAnimation rotate=null;
            DemoAdapter(ArrayList<TimelineListAPKsJson.UserApk> list) {
                super(new TimelineAdapter(getActivity(), list), true);

            }
            @Override
            protected View getPendingView(ViewGroup parent) {
                return getActivity().getLayoutInflater().inflate(R.layout.progress_bar, parent, false);
            }

            @Override
            protected boolean cacheInBackground() throws Exception {
                new FetchDataTask(this, items.size()).execute();
                return true;

            }
            @Override
            public void onItemsReady(ArrayList<TimelineListAPKsJson.UserApk> data) {

                if(data.isEmpty()){
                    adapter.stopAppending();
                }else{
                    items.addAll(data);
                    adapter.onDataReady();
                }

                 // Tell the EndlessAdapter to
// remove it's pending
// view and call
// notifyDataSetChanged()
            }
            @Override
            protected void appendCachedData() {
            }
        }
        interface IItemsReadyListener {
            public void onItemsReady(ArrayList<TimelineListAPKsJson.UserApk> data);
        }
        class FetchDataTask extends AsyncTask<Void, Void, ArrayList<TimelineListAPKsJson.UserApk>> {
            IItemsReadyListener listener;
            /*
            * The point from where to start counting. In a real
            * life scenario this could be a pagination number
            */
            int startPoint;
            protected FetchDataTask(IItemsReadyListener listener, int startPoint) {
                this.listener=listener;
                this.startPoint=startPoint;
            }
            @Override
            protected ArrayList<TimelineListAPKsJson.UserApk> doInBackground(Void... params) {
                ArrayList<TimelineListAPKsJson.UserApk> result=new ArrayList<TimelineListAPKsJson.UserApk>();


                ListApksInstallsRequest request = new ListApksInstallsRequest();
                request.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
                request.setOffset_id(String.valueOf(startPoint));

                try {
                    result.addAll(request.loadDataFromNetwork().getUsersapks());
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return result;
            }
            @Override
            protected void onPostExecute(ArrayList<TimelineListAPKsJson.UserApk> result) {
                listener.onItemsReady(result);
            }
        }
    }

}
