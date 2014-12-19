package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreActivity;
import cm.aptoide.ptdev.MoreTopStoresActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StickyRecyclerHeadersDecoration;
import cm.aptoide.ptdev.StickyRecyclerHeadersTouchListener;
import cm.aptoide.ptdev.adapters.V6.Displayable;
import cm.aptoide.ptdev.adapters.V6.Holders.AppViewHolder;
import cm.aptoide.ptdev.adapters.V6.Rows.AppsRow;
import cm.aptoide.ptdev.adapters.V6.Rows.StoreRow;
import cm.aptoide.ptdev.adapters.V6.V6StoresRecyclerAdapter;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 21-11-2014.
 */
public class FragmentListTopApps extends Fragment {
    private RecyclerView view;
    private ArrayList<Displayable> string;
    private RequestListener<Response> requestListener;

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manager.start(getActivity());

    }

    @Override
    public void onDetach() {
        super.onDetach();
        manager.shouldStop();

    }

    public interface TestService{
        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps,listStores/")
        Response postApk(@Body Api user);
    }

    public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {



        public TestRequest() {
            super(Response.class, TestService.class);

        }

        @Override

        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();

            api.getApi_global_params().setLang(AptoideUtils.getMyCountry(Aptoide.getContext()));
            api.getApi_global_params().setStore_name("apps");
            api.getApi_global_params().limit = 10;

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());

            if(Aptoide.DEBUG_MODE){
                api.getApi_global_params().country = AptoideUtils.getSharedPreferences().getString("forcecountry", null);
            }

            api.getApi_global_params().mature = String.valueOf(sPref.getBoolean("matureChkBox", false));
            
            Api.GetStore getStore = new Api.GetStore();


            Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
            widgetParams.setContext("top");

            //getStore.getDatasets_params().set(categoriesParams);
            getStore.getDatasets_params().set(widgetParams);

            //getStore.addDataset(categoriesParams.getDatasetName());
            getStore.addDataset(widgetParams.getDatasetName());

            api.getApi_params().set(getStore);

            Response response = null;
            try{
                response = getService().postApk(api);
            }catch (RetrofitError error){
                throw error;
            }

            return response;

        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);

        view = (RecyclerView) rootView.findViewById(R.id.list);
        string = new ArrayList<>(20);

        final RecyclerAdapter mAdapter = new RecyclerAdapter(getActivity(), string);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
        StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(view, stickyRecyclerHeadersDecoration);
        touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View viewHeader, int i, long l) {
                Displayable displayable = ((RecyclerAdapter) view.getAdapter()).list.get(i);
                if(displayable.getViewType()==1000) {
                    Intent intent = new Intent(getActivity(), MoreTopStoresActivity.class);
                    startActivity(intent);

                    FlurryAgent.logEvent("Top_Page_Clicked_On_More_Top_Stores");

                }else{
                    Intent intent = new Intent(getActivity(), MoreActivity.class);

                    intent.putExtra("widgetid", ((AppsRow) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetid);
                    intent.putExtra("widgetrefid", ((AppsRow) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetrefid);
                    intent.putExtra("widgetname", ((AppsRow) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);

                    Map<String, String> flurryParams = new HashMap<String, String>();
                    flurryParams.put("WidgetCategory", ((AppsRow) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);
                    FlurryAgent.logEvent("Top_Page_Clicked_On_More_Top_Stores", flurryParams);

                    startActivity(intent);
                }
                //Toast.makeText(Aptoide.getContext(), "" + ((Row)((RecyclerAdapter)view.getAdapter()).list.get(i)).widgetid + " " + l, Toast.LENGTH_LONG).show();
            }
        });

        view.addOnItemTouchListener(touchListener);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        view.setAdapter(mAdapter);
        view.setLayoutManager(linearLayoutManager);
        view.addItemDecoration(stickyRecyclerHeadersDecoration);

        final TestRequest request = new TestRequest();
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        requestListener = new RequestListener<Response>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.error).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.list).setVisibility(View.GONE);
                        rootView.findViewById(R.id.error).setVisibility(View.GONE);
                        manager.execute(request, "top", DurationInMillis.ALWAYS_RETURNED, requestListener);
                    }
                });
            }

            @Override
            public void onRequestSuccess(Response response) {
                ArrayList<Displayable> map = new ArrayList<>();
                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.error).setVisibility(View.GONE);

                try {

                    List<Response.GetStore.Widgets.Widget> list = response.responses.getStore.datasets.widgets.data.list;
                    HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();
                    HashMap<String, Response.ListStores.StoreGroup> storesdataset = response.responses.listStores.datasets.getDataset();

                    for (Response.GetStore.Widgets.Widget widget : list) {

                        if (widget.type.equals("apps_list")) {

                            Response.ListApps.Category category = dataset.get(widget.data.ref_id);

                            if (category != null && category.data != null) {
                                ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(dataset.get(widget.data.ref_id).data.list);

                                while (!inElements.isEmpty()) {
                                    AppsRow row = new AppsRow(getActivity());
                                    row.widgetid = widget.widgetid;
                                    row.header = widget.name;
                                    row.widgetrefid = widget.data.ref_id;
                                    row.addItem(inElements.remove(0));
                                    map.add(row);
                                }
                            }
                        } else if (widget.type.equals("stores_list")) {
                            ArrayList<Response.ListStores.Store> inElements = new ArrayList<Response.ListStores.Store>(storesdataset.get(widget.data.ref_id).data.list);

                            while (!inElements.isEmpty()) {
                                StoreRow row = new StoreRow(getActivity());
                                Response.ListStores.Store store = inElements.remove(0);
                                row.header = widget.name;
                                row.appscount = store.apps_count.intValue();
                                row.downloads = store.downloads.intValue();
                                row.avatar = store.avatar;
                                row.name = store.name;
                                map.add(row);
                            }

                        } else {
                            AppsRow row = new AppsRow(getActivity());
                            row.setEnabled(false);
                            map.add(row);
                            ((RecyclerAdapter) view.getAdapter()).getPlaceholders().put(widget.type, map.size());
                        }

                    }

                    string.clear();
                    string.addAll(map);

                    Log.d("AptoideDebug", string.toString());

                    view.getAdapter().notifyDataSetChanged();

                    swipeLayout.setRefreshing(false);

                    rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                }catch (Exception e){

                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        String s = mapper.writeValueAsString(response);
                        Crashlytics.logException(new Throwable(s, e));
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }

                    rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    rootView.findViewById(R.id.error).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.list).setVisibility(View.GONE);
                            rootView.findViewById(R.id.error).setVisibility(View.GONE);
                            manager.execute(request, "top", DurationInMillis.ALWAYS_RETURNED, requestListener);
                        }
                    });

                }
            }

        };
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                manager.execute(request, "top" , DurationInMillis.ALWAYS_EXPIRED,  requestListener);
            }
        });
        manager.execute(request, "top" , DurationInMillis.ALWAYS_RETURNED,  requestListener);
        return rootView;
    }

    public static class RecyclerAdapter extends V6StoresRecyclerAdapter {

        public RecyclerAdapter(Context context, List<Displayable> list) {
            super(context, list);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate;
            if(viewType<1000){
                inflate = LayoutInflater.from(context).inflate(R.layout.top_item, parent, false);
                return new AppViewHolder(inflate, viewType, context);

            }else{
                return super.onCreateViewHolder(parent,viewType);
            }
        }
    }
}
