package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.MoreActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.HttpService;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 21-11-2014.
 */
public class FragmentListApps extends Fragment {
    private RecyclerView view;
    private ArrayList<Displayable> string;
    private RequestListener<Response> requestListener;

    SpiceManager manager = new SpiceManager(HttpService.class);

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
        Response postApk(@Body Api user) throws Response.TicketException;
    }

    public interface Displayable {
        int getViewType();

        long getHeaderId();



        void bindView(FragmentListApps.RecyclerAdapter.RowViewHolder viewHolder);

        void onBindHeaderViewHolder(FragmentListApps.RecyclerAdapter.HeaderViewHolder viewHolder);
    }


    public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {

        private final String context;
        private int offset;

        public TestRequest(String context) {
            super(Response.class, TestService.class);
            this.context = context;
        }


        public void setOffset(int offset){

            this.offset = offset;
        }



        @Override
        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();

            api.getApi_global_params().setLang("en");
            api.getApi_global_params().setStore_name("apps");


            Api.GetStore getStore = new Api.GetStore();
            Api.GetStore.CategoriesParams categoriesParams = new Api.GetStore.CategoriesParams();

            categoriesParams.setParent_ref_id("cat_2");

            Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
            widgetParams.setContext("home");
            //widgetParams.offset = offset;
            //widgetParams.limit = 3;
            getStore.getDatasets_params().set(categoriesParams);
            getStore.getDatasets_params().set(widgetParams);

            //getStore.addDataset(categoriesParams.getDatasetName());
            getStore.addDataset(widgetParams.getDatasetName());

            api.getApi_params().set(getStore);

            Api.ListApps listApps = new Api.ListApps();
            listApps.limit = 6;
            listApps.datasets = null;
            api.getApi_params().set(listApps);

            Response response;

            response = getService().postApk(api);

            return response;

        }

    }

    public static class Row implements Displayable{

        private final Context context;
        public String header;
        public String widgetid;
        public List<Response.ListApps.Apk> apks = new ArrayList<Response.ListApps.Apk>(3);
        public String widgetrefid;
        //private Picasso picasso;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled = true;

        public Row(Context context) {
            //picasso = Picasso.with(context);
            //picasso.setIndicatorsEnabled(true);
            this.context = context;
        }

        public void addItem(Response.ListApps.Apk apk){
            apks.add(apk);
        }


        @Override
        public String toString() {
            return "Row:" + header + " enabled: " + String.valueOf(enabled);
        }

        @Override
        public int getViewType() {
            return apks.size();
        }

        @Override
        public long getHeaderId() {

            if(enabled){
                return Math.abs(header.hashCode());
            }else{
                return -1;
            }
        }



        @Override
        public void bindView(RecyclerAdapter.RowViewHolder viewHolder) {

            int i=0;
            for(Response.ListApps.Apk apk : apks){
                RecyclerAdapter.RowViewHolder.ItemViewHolder itemViewHolder =
                        (RecyclerAdapter.RowViewHolder.ItemViewHolder) viewHolder.views[i].getTag();
                itemViewHolder.name.setText(apk.name);
                String icon = apk.icon;
                itemViewHolder.category.setText(apk.downloads.intValue() + " Downloads");

                if(icon.contains("_icon")){
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_96x96"  + "."+ splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon, itemViewHolder.icon);
                //picasso.load(icon).into(itemViewHolder.icon);
                viewHolder.views[i].setClickable(true);
                viewHolder.views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apks.get(0).md5sum);
                        i.putExtra("download_from", "recommended_apps");
                        context.startActivity(i);
                    }
                });
                i++;
            }

        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
            viewHolder.tv.setText(header);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);

        view = (RecyclerView) rootView.findViewById(R.id.list);
        string = new ArrayList<Displayable>(20);

        final RecyclerAdapter mAdapter = new RecyclerAdapter(getActivity(), string);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
        StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(view, stickyRecyclerHeadersDecoration);


        touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View viewHeader, int i, long l) {

                Intent intent = new Intent(getActivity(), MoreActivity.class);

                intent.putExtra("widgetid", ((Row)((RecyclerAdapter)view.getAdapter()).list.get(i)).widgetid);
                intent.putExtra("widgetrefid", ((Row)((RecyclerAdapter)view.getAdapter()).list.get(i)).widgetrefid);
                intent.putExtra("widgetname", ((Row)((RecyclerAdapter)view.getAdapter()).list.get(i)).header);
                startActivity(intent);

                //Toast.makeText(Aptoide.getContext(), "" + ((Row)((RecyclerAdapter)view.getAdapter()).list.get(i)).widgetid + " " + l, Toast.LENGTH_LONG).show();

            }
        });
        view.addOnItemTouchListener(touchListener);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);



        view.setAdapter(mAdapter);
        view.setLayoutManager(linearLayoutManager);
        view.addItemDecoration(stickyRecyclerHeadersDecoration);

        TestRequest request = new TestRequest("home");

        requestListener = new RequestListener<Response>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(Response response) {


                ArrayList<Displayable> map = new ArrayList<Displayable>();

                List<Response.GetStore.Widgets.Widget> list = response.responses.getStore.datasets.widgets.data.list;
                HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();


                for(Response.GetStore.Widgets.Widget widget : list) {

                    if(widget.type.equals("apps_list")) {
                        Response.ListApps.Category category = dataset.get(widget.data.ref_id);

                        if(category !=null && category.data!=null) {
                            ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(dataset.get(widget.data.ref_id).data.list);

                            while (!inElements.isEmpty()) {
                                Row row = new Row(getActivity());
                                row.widgetid = widget.widgetid;
                                row.header = widget.name;
                                row.widgetrefid = widget.data.ref_id;
                                for (int j = 0; j < 3 && !inElements.isEmpty(); j++) {
                                    row.addItem(inElements.remove(0));
                                }
                                map.add(row);
                            }
                        }

                    } else {
                        Row row = new Row(getActivity());
                        row.setEnabled(false);
                        map.add(row);
                        ((RecyclerAdapter)view.getAdapter()).getPlaceholders().put(widget.type, map.size());
                    }

                }

                //string.clear();
                string.addAll(map);

                Log.d("AptoideDebug", string.toString());
                int offset = ((RecyclerAdapter) view.getAdapter()).offset;
                ((RecyclerAdapter) view.getAdapter()).offset =  offset + list.size();
                view.getAdapter().notifyDataSetChanged();
                loading = false;

                GetAdsRequest request = new GetAdsRequest(getActivity());

                request.setLimit(3);
                request.setLocation("homepage");
                request.setKeyword("__NULL__");


//                ListApksInstallsRequest listRelatedApkRequest = new ListApksInstallsRequest();


//                if(AptoideUtils.isLoggedIn(getActivity())) {
//
//                    manager.execute(listRelatedApkRequest, "MoreFriendsInstalls", DurationInMillis.ONE_DAY, new RequestListener<TimelineListAPKsJson>() {
//                        @Override
//                        public void onRequestFailure(SpiceException spiceException) {
//
//                        }
//
//                        @Override
//                        public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
//
//
//                            Row row = new Row(getActivity());
//
//                            for (TimelineListAPKsJson.UserApk apkSuggestion : timelineListAPKsJson.getUsersapks()) {
//
//                                    row.header = "Your friends installed";
//                                    Response.ListApps.Apk apk = new Response.ListApps.Apk();
//
//                                    apk.name = apkSuggestion.getApk().getName();
//                                    apk.icon = apkSuggestion.getApk().getIcon();
//                                    apk.downloads = 0;
//                                    row.addItem(apk);
//
//                            }
//
//                            int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("timeline");
//
//
//                            ((RecyclerAdapter) view.getAdapter()).list.remove(location);
//                            ((RecyclerAdapter) view.getAdapter()).list.add(location, row);
//
//
//                            (view.getAdapter()).notifyDataSetChanged();
//
//                        }
//                    });
//
//                }


                manager.execute(request, new RequestListener<ApkSuggestionJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {

                    }

                    @Override
                    public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {
                        if (apkSuggestionJson != null && apkSuggestionJson.getAds() != null && apkSuggestionJson.getAds().size() > 0) {
                            Row row = new Row(getActivity());

                            for (ApkSuggestionJson.Ads apkSuggestion : apkSuggestionJson.getAds()) {


                                if (apkSuggestion.getInfo().getAd_type().equals("app:suggested")) {
                                    row.header = "Highlighted";
                                    Response.ListApps.Apk apk = new Response.ListApps.Apk();

                                    apk.name = apkSuggestion.getData().getName();
                                    apk.icon = apkSuggestion.getData().getIcon();

                                    apk.downloads = 0;

                                    row.addItem(apk);
                                }


                            }
                            int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("ads_list");

                            ((RecyclerAdapter) view.getAdapter()).list.add(location, row);


                            (view.getAdapter()).notifyDataSetChanged();
                        }


                    }
                });
            }

        };




//        view.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                visibleItemCount = recyclerView.getChildCount();
//                totalItemCount = recyclerView.getLayoutManager().getItemCount();
//                firstVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//
//                if (loading) {
//                    if (totalItemCount > previousTotal) {
//                        previousTotal = totalItemCount;
//                    }
//                }
//                if (!loading && (totalItemCount - visibleItemCount)
//                        <= (firstVisibleItem + visibleThreshold)) {
//                    // End has been reached
//
//                    Log.i("...", "end called");
//                    TestRequest request = new TestRequest("home");
//                    request.setOffset(((RecyclerAdapter) view.getAdapter()).offset);
//                    loading = true;
//                    manager.execute(request, "home" + totalItemCount, DurationInMillis.ALWAYS_RETURNED,  requestListener);
//
//                    // Do something
//
//                }
//            }
//        });

        manager.execute(request, "home", DurationInMillis.ALWAYS_RETURNED,  requestListener);


        return rootView;
    }

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RowViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerAdapter.HeaderViewHolder> {

        public int offset = 0;

        public Context getContext() {
            return context;
        }

        private final Context context;
        private final List<Displayable> list;
        private final HashMap<String, Integer> placeholders = new HashMap<String, Integer>();

        public RecyclerAdapter(Context context, List<Displayable> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).getViewType();
        }


        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LinearLayout inflate = new LinearLayout(context);
            inflate.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            inflate.setLayoutParams(params);

            return new RowViewHolder(inflate, viewType, context);
        }


        @Override
        public void onBindViewHolder(RowViewHolder holder, int position) {
            list.get(position).bindView(holder);
        }

        @Override
        public long getHeaderId(int position) {
            return list.get(position).getHeaderId();
        }

        @Override
        public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {

            View inflate = LayoutInflater.from(context).inflate(R.layout.home_separator, viewGroup, false);

            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("OnClick", "OnClick");
                }
            });

            return new HeaderViewHolder(inflate);
        }

        @Override
        public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
            list.get(position).onBindHeaderViewHolder(viewHolder);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public HashMap<String, Integer> getPlaceholders() {
            return placeholders;
        }

        public static class RowViewHolder extends RecyclerView.ViewHolder{


            public static class ItemViewHolder {
                public TextView name;
                public TextView category;
                public ImageView icon;
            }

            public LinearLayout getLinearLayout() {
                return layout;
            }

            public View[] getViews() {
                return views;
            }


            final View[] views;
            final LinearLayout layout;

            public RowViewHolder(View itemView, int viewType, Context context) {
                super(itemView);

                views = new View[viewType];

                layout = (LinearLayout) itemView;
                for(int i = 0; i < viewType; i++){
                    View inflate = LayoutInflater.from(context).inflate(R.layout.home_item, layout, false);
                    views[i] = inflate;
                    ItemViewHolder holder = new ItemViewHolder();
                    holder.name = (TextView) inflate.findViewById(R.id.app_name);
                    holder.icon = (ImageView) inflate.findViewById(R.id.app_icon);
                    holder.category = (TextView) inflate.findViewById(R.id.app_category);

                    inflate.setTag(holder);
                    layout.addView(inflate);

                }

                if(viewType>0){
                    for(int i = viewType; i < 3; i++){
                        View inflate = LayoutInflater.from(context).inflate(R.layout.home_item, layout, false);
                        inflate.setVisibility(View.INVISIBLE);
                        layout.addView(inflate);
                    }
                }


            }

        }
        public static class HeaderViewHolder extends RecyclerView.ViewHolder{

            TextView tv;


            public HeaderViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.header);

            }



        }
    }

}
