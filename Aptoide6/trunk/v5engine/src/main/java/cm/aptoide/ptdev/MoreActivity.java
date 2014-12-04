package cm.aptoide.ptdev;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.HttpService;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 26-11-2014.
 */

public class MoreActivity extends ActionBarActivity {

    public static class Row implements Displayable{

        private final Context context;
        public String header;
        public List<Response.ListApps.Apk> apks = new ArrayList<Response.ListApps.Apk>(3);
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
            for(final Response.ListApps.Apk apk : apks){
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
                        i.putExtra("md5sum", apk.md5sum);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if(savedInstanceState == null){
            Fragment fragment = new MoreFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

    }

    public interface Displayable {
        int getViewType();

        long getHeaderId();



        void bindView(RecyclerAdapter.RowViewHolder viewHolder);

        void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder);
    }

    public static class MoreFragment extends Fragment {


        private RecyclerView recyclerView;
        ArrayList<Displayable> list = new ArrayList<Displayable>(20);
        private RequestListener<Response> requestListener;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);



            return rootView;
        }

        @Override
        public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            final RecyclerAdapter mAdapter = new RecyclerAdapter(getActivity(), list);
            StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
            StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(recyclerView, stickyRecyclerHeadersDecoration);


            touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                @Override
                public void onHeaderClick(View viewHeader, int i, long l) {
                    Toast.makeText(Aptoide.getContext(), "" + ((RecyclerAdapter) recyclerView.getAdapter()).list.get(i).getHeaderId() + " " + l, Toast.LENGTH_LONG).show();
                }
            });

            recyclerView.addOnItemTouchListener(touchListener);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);



            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);

            requestListener = new RequestListener<Response>() {

                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.error).setVisibility(View.VISIBLE);
                }

                @Override
                public void onRequestSuccess(Response response) {


                    ArrayList<Displayable> map = new ArrayList<Displayable>();

                    List<Response.GetStore.Widgets.Widget> appsList = response.responses.getStore.datasets.widgets.data.list;
                    HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();

                    if(appsList.isEmpty()){
                        Response.ListApps.Category category = dataset.get(getArguments().getString("widgetrefid"));

                        if(category !=null && category.data!=null) {
                            ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(category.data.list);

                            while (!inElements.isEmpty()) {
                                Row row = new Row(getActivity());
                                row.header = getArguments().getString("widgetname");
                                for (int j = 0; j < 3 && !inElements.isEmpty(); j++) {
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

                                    while (!inElements.isEmpty()) {
                                        Row row = new Row(getActivity());
                                        row.header = widget.name;
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
                                ((RecyclerAdapter)recyclerView.getAdapter()).getPlaceholders().put(widget.type, map.size());
                            }

                        }

                    }

                    //string.clear();
                    list.addAll(map);

                    //Log.d("AptoideDebug", string.toString());
                    int offset = ((RecyclerAdapter) recyclerView.getAdapter()).offset;
                    ((RecyclerAdapter) recyclerView.getAdapter()).offset =  offset + list.size();
                    recyclerView.getAdapter().notifyDataSetChanged();
                    //loading = false;

                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.list).setVisibility(View.VISIBLE);

                }
                                                                                                                                
            };

            TestRequest request = new TestRequest("");

            request.setWidgetId(getArguments().getString("widgetid"));

            manager.execute(request, getArguments().getString("widgetid"), DurationInMillis.ALWAYS_RETURNED,  requestListener);
            view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
            view.findViewById(R.id.list).setVisibility(View.GONE);


        }

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
            Response postApk(@Body Api user);
        }

        public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {

            private final String context;
            private int offset;
            private String widget;

            public TestRequest(String context) {
                super(Response.class, TestService.class);
                this.context = context;
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

                //getStore.addDataset(categoriesParams.getDatasetName());
                getStore.addDataset(widgetParams.getDatasetName());

                api.getApi_params().set(getStore);


                return getService().postApk(api);

            }

        }


    }

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
