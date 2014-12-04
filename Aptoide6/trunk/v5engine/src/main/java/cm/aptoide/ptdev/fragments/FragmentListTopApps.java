package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import cm.aptoide.ptdev.webservices.HttpService;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.RestAdapter;
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
        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps,listStores/")
        Response postApk(@Body Api user);
    }

    public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {

        private final String context;

        public TestRequest(String context) {
            super(Response.class, TestService.class);
            this.context = context;
        }

        @Override

        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();

            api.getApi_global_params().setLang("en");
            api.getApi_global_params().setStore_name("apps");

            Api.GetStore getStore = new Api.GetStore();
            Api.GetStore.CategoriesParams categoriesParams = new Api.GetStore.CategoriesParams();

            categoriesParams.setParent_ref_id("cat_1");

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

                switch (error.getKind()){
                    case NETWORK:
                    case CONVERSION:
                    case UNEXPECTED:
                    case HTTP:
                        if(error.getResponse().getStatus() / 100 == 4){
                            new RestAdapter.Builder().build().create(TestService.class);
                        }
                        break;
                }
                throw error;

            }

            return response;

        }

    }

    public interface Displayable {
        int getViewType();

        long getHeaderId();



        void bindView(RecyclerView.ViewHolder viewHolder);

        void onBindHeaderViewHolder(FragmentListTopApps.RecyclerAdapter.HeaderViewHolder viewHolder);
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
        public void bindView(RecyclerView.ViewHolder vh) {

            RecyclerAdapter.RowViewHolder viewHolder = (RecyclerAdapter.RowViewHolder) vh;

                if(!apks.isEmpty()) {
                    viewHolder.name.setText(apks.get(0).name);
                    String icon = apks.get(0).icon;
                    viewHolder.version.setText(apks.get(0).downloads.intValue() + " Downloads");

                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_96x96" + "." + splittedUrl[1];
                    }

                    ImageLoader.getInstance().displayImage(icon, viewHolder.icon);
                    viewHolder.itemView.setClickable(true);
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, AppViewActivity.class);
                            i.putExtra("fromRelated", true);
                            i.putExtra("md5sum", apks.get(0).md5sum);
                            i.putExtra("repoName", apks.get(0).store_name);
                            i.putExtra("download_from", "recommended_apps");
                            context.startActivity(i);
                        }
                    });



                }

                //picasso.load(icon).into(itemViewHolder.icon);



        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
            viewHolder.tv.setText(header);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);

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

        final TestRequest request = new TestRequest("top");
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        requestListener = new RequestListener<Response>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.error).setVisibility(View.VISIBLE);
            }

            @Override
            public void onRequestSuccess(Response response) {


                ArrayList<Displayable> map = new ArrayList<Displayable>();

                List<Response.GetStore.Widgets.Widget> list = response.responses.getStore.datasets.widgets.data.list;
                HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();
                HashMap<String, Response.ListStores.StoreGroup> storesdataset = response.responses.listStores.datasets.getDataset();



                for(Response.GetStore.Widgets.Widget widget : list) {

                    if(widget.type.equals("apps_list")) {

                        Response.ListApps.Category category = dataset.get(widget.data.ref_id);

                        if (category != null && category.data != null) {
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
                    }else if(widget.type.equals("stores_list")){
                        ArrayList<Response.ListStores.Store> inElements = new ArrayList<Response.ListStores.Store>(storesdataset.get(widget.data.ref_id).data.list);

                        while (!inElements.isEmpty()) {
                            StoreRow row = new StoreRow(getActivity());
                            Response.ListStores.Store store = inElements.remove(0);
                            row.header = widget.name;
                            row.appscount = store.apps_count.intValue();
                            row.avatar = store.avatar;
                            row.name = store.name;
                            map.add(row);
                        }

                    } else {
                        Row row = new Row(getActivity());
                        row.setEnabled(false);
                        map.add(row);
                        ((RecyclerAdapter)view.getAdapter()).getPlaceholders().put(widget.type, map.size());
                    }

                }

                string.clear();
                string.addAll(map);

                Log.d("AptoideDebug", string.toString());

                view.getAdapter().notifyDataSetChanged();

                swipeLayout.setRefreshing(false);

                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
            }

        };



        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                manager.execute(request, "top" , DurationInMillis.ALWAYS_EXPIRED,  requestListener);
            }
        });

        rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.GONE);
        manager.execute(request, "top" , DurationInMillis.ALWAYS_RETURNED,  requestListener);

        return rootView;
    }

    public static class StoreRow extends Row{

        public String name;
        public String avatar;
        public int downloads;
        public int appscount;


        public StoreRow(Context context) {
            super(context);
        }

        @Override
        public int getViewType() {
            return 1000;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder vh) {
            RecyclerAdapter.StoreRowViewHolder viewHolder = (RecyclerAdapter.StoreRowViewHolder) vh;

            viewHolder.name.setText(name);
            ImageLoader.getInstance().displayImage(avatar, viewHolder.icon);

        }
    }

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerAdapter.HeaderViewHolder> {


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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View inflate;
            if(viewType<1000){
                inflate = LayoutInflater.from(context).inflate(R.layout.top_item, parent, false);
                return new RowViewHolder(inflate, viewType, context);

            }else{
                inflate = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
                return new StoreRowViewHolder(inflate, viewType, context);
            }


        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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

        public static class StoreRowViewHolder extends RecyclerView.ViewHolder{


            public static class ItemViewHolder {


            }


            public TextView name;
            public TextView store_info;

            public ImageView icon;

            public StoreRowViewHolder(View itemView, int viewType, Context context) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.store_name);
                icon = (ImageView) itemView.findViewById(R.id.store_avatar);
                store_info = (TextView) itemView.findViewById(R.id.store_info);

            }

        }

        public static class RowViewHolder extends RecyclerView.ViewHolder{


            public static class ItemViewHolder {


            }


            public TextView name;
            public TextView version;
            public RatingBar app_rating;
            public ImageView icon;

            public RowViewHolder(View itemView, int viewType, Context context) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.app_name);
                icon = (ImageView) itemView.findViewById(R.id.app_icon);
                version = (TextView) itemView.findViewById(R.id.app_version);
                app_rating = (RatingBar) itemView.findViewById(R.id.app_rating);

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
