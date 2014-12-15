package cm.aptoide.ptdev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.fragments.FragmentListApps;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 26-11-2014.
 */

public class MoreActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("widgetname"));

        if(savedInstanceState == null){
            Fragment fragment = new MoreFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
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

    public static class ProgressBarRow implements FragmentListApps.Displayable{

        @Override
        public int getViewType() {
            return 123456789;
        }

        @Override
        public long getHeaderId() {
            return -1;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder viewHolder) {

        }

        @Override
        public void onBindHeaderViewHolder(FragmentListApps.RecyclerAdapter.HeaderViewHolder viewHolder) {

        }

        @Override
        public boolean isMore() {
            return false;
        }
    }

    public static class MoreFragment extends Fragment {


        private RecyclerView recyclerView;
        ArrayList<FragmentListApps.Displayable> list = new ArrayList<FragmentListApps.Displayable>(20);
        private RequestListener<Response> requestListener;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_list_apps, container, false);
        }

        private int previousTotal = 0;
        private int offset;

        private boolean loading = true;
        private int visibleThreshold = 5;
        int firstVisibleItem, visibleItemCount, totalItemCount;

        @Override
        public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            final FragmentListApps.RecyclerAdapter mAdapter = new MoreRecycler(getActivity(), list);
            StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
            StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(recyclerView, stickyRecyclerHeadersDecoration);


            touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                @Override
                public void onHeaderClick(View viewHeader, int i, long l) {
                    String widgetid = ((FragmentListApps.Row) ((FragmentListApps.RecyclerAdapter) recyclerView.getAdapter()).getList().get(i)).widgetid;

                    Intent intent = new Intent(getActivity(), MoreActivity.class);
                    intent.putExtra("widgetid", widgetid);
                    intent.putExtra("widgetrefid", ((FragmentListApps.Row) ((FragmentListApps.RecyclerAdapter) recyclerView.getAdapter()).getList().get(i)).widgetrefid);
                    intent.putExtra("widgetname", ((FragmentListApps.Row) ((FragmentListApps.RecyclerAdapter) recyclerView.getAdapter()).getList().get(i)).header);

                    startActivity(intent);
                }
            });

            recyclerView.addOnItemTouchListener(touchListener);



            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

            swipeLayout.setEnabled(false);

            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);

            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {

                    Log.d("AptoideAdapter", "scrolling");

                    if(offset>0) {

                        LinearLayoutManager mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                        visibleItemCount = mRecyclerView.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if (totalItemCount > previousTotal) {
                                previousTotal = totalItemCount;
                            }
                        }
                        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                            // End has been reached
                            loading = true;
                            TestRequest request = new TestRequest();

                            request.setWidgetId(getArguments().getString("widgetid"));
                            request.setOffset(offset);

                            list.add(new ProgressBarRow());

                            Log.d("AptoideAdapter", "Adding row");

                            recyclerView.getAdapter().notifyItemInserted(recyclerView.getAdapter().getItemCount());

                            manager.execute(request, getArguments().getString("widgetid") + AptoideUtils.getBucketSize(), DurationInMillis.ALWAYS_EXPIRED, requestListener);

                            // Do something

                        }
                    }

                }
            });
            final TestRequest request = new TestRequest();

            requestListener = new RequestListener<Response>() {

                @Override
                public void onRequestFailure(SpiceException spiceException) {


                    if(list.isEmpty()){
                        view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                        view.findViewById(R.id.error).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                manager.execute(request, getArguments().getString("widgetid") + AptoideUtils.getBucketSize(), DurationInMillis.ALWAYS_RETURNED,  requestListener);
                                view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.list).setVisibility(View.GONE);
                            }
                        });
                    }




                    if(loading && !list.isEmpty()){
                        list.remove(list.size() - 1);
                        loading = false;
                    }


                }

                @Override
                public void onRequestSuccess(Response response) {


                    int BUCKET_SIZE = AptoideUtils.getBucketSize();
                    ArrayList<FragmentListApps.Displayable> map = new ArrayList<FragmentListApps.Displayable>();

                    List<Response.GetStore.Widgets.Widget> appsList = response.responses.getStore.datasets.widgets.data.list;
                    HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();

                    if(appsList.isEmpty()){

                        Response.ListApps.Category category = dataset.get(getArguments().getString("widgetrefid"));

                        if(category !=null && category.data!=null) {

                            offset = category.data.next;

                            ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(category.data.list);

                            while (!inElements.isEmpty()) {
                                FragmentListApps.Row row = new FragmentListApps.Row();
                                row.setEnabled(false);
                                row.header = getArguments().getString("widgetname");

                                for (int j = 0; j < BUCKET_SIZE && !inElements.isEmpty(); j++) {
                                    row.addItem(inElements.remove(0));
                                }
                                map.add(row);
                            }
                        }
                    }else{

                        for(Response.GetStore.Widgets.Widget widget : appsList) {

                            if(widget.type.equals("apps_list")) {
                                Response.ListApps.Category category = dataset.get(widget.data.ref_id);

                                if(category !=null && category.data!=null) {
                                    ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(dataset.get(widget.data.ref_id).data.list);

                                    boolean showMore = inElements.size()>=BUCKET_SIZE*4;

                                    while (!inElements.isEmpty()) {
                                        FragmentListApps.Row row = new FragmentListApps.Row();
                                        row.header = widget.name;

                                        row.setMore(showMore);
                                        row.widgetid = widget.widgetid;
                                        row.widgetrefid = widget.data.ref_id;

                                        for (int j = 0; j < BUCKET_SIZE && !inElements.isEmpty(); j++) {
                                            row.addItem(inElements.remove(0));
                                        }
                                        map.add(row);
                                    }
                                }

                            } else {
                                FragmentListApps.Row row = new FragmentListApps.Row();
                                row.setEnabled(false);
                                map.add(row);
                                ((FragmentListApps.RecyclerAdapter)recyclerView.getAdapter()).getPlaceholders().put(widget.type, map.size());
                            }

                        }

                    }

                    if(loading && !list.isEmpty()){
                        list.remove(list.size() - 1);
                    }
                    //string.clear();
                    list.addAll(map);

                    //Log.d("AptoideDebug", string.toString());

                    recyclerView.getAdapter().notifyDataSetChanged();
                    //loading = false;

                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.list).setVisibility(View.VISIBLE);
                    loading = false;

                }
                                                                                                                                
            };


            request.setWidgetId(getArguments().getString("widgetid"));

            manager.execute(request, getArguments().getString("widgetid") + AptoideUtils.getBucketSize(), DurationInMillis.ALWAYS_RETURNED,  requestListener);
            view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
            view.findViewById(R.id.list).setVisibility(View.GONE);


        }

        public static class MoreRecycler extends FragmentListApps.RecyclerAdapter{

            public MoreRecycler(Context context, List<FragmentListApps.Displayable> list) {
                super(context, list);
            }





            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                if(viewType==123456789){

                    return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_bar, parent, false)) {
                    };

                }else{
                    return super.onCreateViewHolder(parent, viewType);
                }
            }
        }

        SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

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
        public interface TestService{

            @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
            Response postApk(@Body Api user);
        }

        public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {

            private int offset;
            private String widget;

            public TestRequest() {
                super(Response.class, TestService.class);

            }


            public void setOffset(int offset){

                this.offset = offset;
            }

            public void setWidgetId(String widget){

                this.widget = widget;
            }



            @Override
            public Response loadDataFromNetwork() throws Exception {
                Api api = new Api();

                api.getApi_global_params().setLang("en");
                api.getApi_global_params().setStore_name("apps");
                int BUCKET_SIZE = AptoideUtils.getBucketSize();

                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
                api.getApi_global_params().mature = String.valueOf(sPref.getBoolean("matureChkBox", false));


                Api.GetStore getStore = new Api.GetStore();
                //Api.GetStore.CategoriesParams categoriesParams = new Api.GetStore.CategoriesParams();

                //categoriesParams.setParent_ref_id("cat_2");

                Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
                widgetParams.setContext("home");
                widgetParams.setWidgetid(widget);
                //widgetParams.offset = offset;
                //widgetParams.limit = 3;
                //getStore.getDatasets_params().set(categoriesParams);
                getStore.getDatasets_params().set(widgetParams);
                Api.ListApps listApps = new Api.ListApps();
                listApps.datasets_params = null;
                listApps.datasets = null;
                if(offset>0){
                    listApps.offset = offset;
                }
                listApps.limit = BUCKET_SIZE * 4;
                //getStore.addDataset(categoriesParams.getDatasetName());
                getStore.addDataset(widgetParams.getDatasetName());

                api.getApi_params().set(getStore);
                api.getApi_params().set(listApps);

                return getService().postApk(api);

            }

        }


    }






}
