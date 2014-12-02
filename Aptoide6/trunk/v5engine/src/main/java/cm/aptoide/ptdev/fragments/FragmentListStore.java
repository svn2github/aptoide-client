package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 28-11-2014.
 */
public class FragmentListStore extends Fragment {


    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manager.start(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        manager.shouldStop();
    }




    public static class GetStoreRequest extends RetrofitSpiceRequest<Response, GetStoreRequest.Webservice>{

        private String widgetId;
        private String refId;
        private String store;

        public GetStoreRequest() {
            super(Response.class, Webservice.class);
        }

        public void setWidgetId(String widgetId){

            this.widgetId = widgetId;
        }

        public void setStore(String store){

            this.store = store;
        }

        public void setRefId(String refId){

            this.refId = refId;
        }

        @Override
        public Response loadDataFromNetwork() throws Exception {


            Api api = new Api();

            api.getApi_global_params().setLang("en");
            api.getApi_global_params().setStore_name(store);


            Api.GetStore getStore = new Api.GetStore();

            Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
            widgetParams.setContext("store");
            widgetParams.setWidgetid(widgetId);
            //widgetParams.offset = offset;
            //widgetParams.limit = 3;

            getStore.getDatasets_params().set(widgetParams);

            //getStore.addDataset(categoriesParams.getDatasetName());
            getStore.addDataset(widgetParams.getDatasetName());

            Api.ListApps listApps = new Api.ListApps();

            api.getApi_params().set(getStore);

            if(widgetId != null){
                listApps.datasets.add(refId);
                api.getApi_params().set(listApps);

                return getService().postApk2(api);
            }else{
                return getService().postApk(api);
            }


        }

        public interface Webservice{
            @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore/")
            Response postApk(@Body Api user) throws Response.TicketException;

            @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
            Response postApk2(@Body Api user) throws Response.TicketException;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_list_apps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayList<StoreListItem> items = new ArrayList<StoreListItem>();

        final RecyclerView rRiew  = (RecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rRiew.setLayoutManager(linearLayoutManager);
        StoreListAdapter adapter = new StoreListAdapter(getActivity(), items);

        GetStoreRequest request = new GetStoreRequest();

        request.setStore(getArguments().getString("storename"));

        if(getArguments().containsKey("widgetrefid")){
            request.setWidgetId(getArguments().getString("widgetrefid"));
            request.setRefId(getArguments().getString("refid"));
        }

        adapter.setStorename(getArguments().getString("storename"));

        rRiew.setAdapter(adapter);
        manager.execute(request, getArguments().getString("storename") + getArguments().getString("widgetrefid") + getArguments().getString("refid") , DurationInMillis.ONE_HOUR, new RequestListener<Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(Response response) {
                ArrayList<StoreListItem> map = new ArrayList<StoreListItem>();

                List<Response.GetStore.Widgets.Widget> list = response.responses.getStore.datasets.widgets.data.list;
                HashMap<String, Response.ListApps.Category> dataset = null;
                if (response.responses.listApps != null) {
                    dataset = response.responses.listApps.datasets.getDataset();
                }


                String widgetrefid = getArguments().getString("refid");
                if (list.isEmpty()) {

                    if (dataset != null) {
                        if(dataset.get(widgetrefid).data != null) {

                            List<Response.ListApps.Apk> apksList = dataset.get(widgetrefid).data.list;

                            for (Response.ListApps.Apk apk : apksList) {
                                App app = new App();
                                app.setName(apk.name);
                                app.setIcon(apk.icon);

                                if(apk.rating!=null){
                                    app.setRating(apk.rating.floatValue());
                                }

                                app.setVersionName(apk.vername);
                                map.add(app);
                            }

                        }else{
                            manager.removeDataFromCache(Response.class);
                        }
                    }

                } else {

                    for (Response.GetStore.Widgets.Widget widget : list) {

                        if (widget.type.equals("apps_list")) {
                            //Response.ListApps.Category category = dataset.get(widget.data.ref_id);
                            WidgetCategory item = new WidgetCategory();
                            item.refid = widget.data.ref_id;
                            item.widgetid = widget.widgetid;
                            item.name = widget.name;
                            map.add(item);
                        } else {
                            WidgetCategory item = new WidgetCategory();
                            item.name = widget.name;
                            map.add(item);
                        }

                    }

                    if (dataset != null ) {

                        if(dataset.get(widgetrefid).data != null) {

                            List<Response.ListApps.Apk> apksList = dataset.get(widgetrefid).data.list;

                            for (Response.ListApps.Apk apk : apksList) {

                                App app = new App();
                                app.setName(apk.name);
                                app.setIcon(apk.icon);
                                if(apk.rating!=null){
                                    app.setRating(apk.rating.floatValue());
                                }
                                app.setVersionName(apk.vername);
                                map.add(app);

                            }

                        }
                    }


                }

                //string.clear();
                items.addAll(map);
                rRiew.getAdapter().notifyDataSetChanged();

            }
        });
    }


    public class WidgetCategory implements StoreListItem{

        public String refid;
        public String widgetid;
        public String name;


        @Override
        public int getItemViewType() {
            return 1;
        }


    }

    public class App implements StoreListItem{

        private String name;
        private float rating;
        private String icon;
        private String versionName;

        @Override
        public int getItemViewType() {
            return 0;
        }


        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }


    public interface StoreListItem{
        int getItemViewType();
    }

    public static class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.StoreListViewHolder>{

        private final List<StoreListItem> list;

        private String storename;

        public StoreListAdapter(Context context, List<StoreListItem> list) {
            this.list = list;
            this.context = context;
        }

        private Context context;

        @Override
        public StoreListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType){
                case 0:
                    return new AppStoreListViewHolder(LayoutInflater.from(context).inflate(R.layout.row_app_standard, parent, false));
                case 1:
                    return new CategoryStoreListViewHolder(LayoutInflater.from(context).inflate(R.layout.row_item_category_first_level_list, parent, false));
            }

            return null;
        }

        @Override
        public void onBindViewHolder(StoreListViewHolder holder, final int position) {


            switch (getItemViewType(position)) {
                case 0:

                    final App appItem = (App) list.get(position);
                    AppStoreListViewHolder appHolder = (AppStoreListViewHolder) holder;
                    appHolder.name.setText(appItem.getName());

                    appHolder.appName.setText(Html.fromHtml(appItem.getName()).toString());
                    appHolder.rating.setRating(appItem.getRating());

                    String icon = appItem.getIcon();
                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + "96x96" + "." + splittedUrl[1];
                    }
                    ImageLoader.getInstance().displayImage(icon,appHolder.appIcon);
                    appHolder.versionName.setText(appItem.getVersionName());

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    break;
                case 1:
                    final WidgetCategory storeListItem = (WidgetCategory) list.get(position);

                    CategoryStoreListViewHolder categoryHolder = (CategoryStoreListViewHolder) holder;

                    categoryHolder.name.setText(storeListItem.name);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String refid = storeListItem.refid;
                            String widgetid = storeListItem.widgetid;

                            FragmentListStore fragment = new FragmentListStore();

                            Bundle bundle = new Bundle();

                            bundle.putString("widgetrefid", widgetid);
                            bundle.putString("refid", refid);
                            bundle.putString("storename", storename);

                            fragment.setArguments(bundle);

                            ((ActionBarActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment).addToBackStack(storeListItem.name).commit();
                        }
                    });

                    break;
            }


        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).getItemViewType();
        }

        public void setStorename(String storename) {
            this.storename = storename;
        }

        public static class AppStoreListViewHolder extends StoreListViewHolder{

            public final ImageView appIcon;
            public final RatingBar rating;
            public final TextView versionName;
            public final TextView appName;
            public final ImageView overFlow;
            public final TextView name;

            public AppStoreListViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.app_name);
                appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
                overFlow = (ImageView) itemView.findViewById(R.id.ic_action);
                appName = (TextView) itemView.findViewById(R.id.app_name);
                versionName = (TextView) itemView.findViewById(R.id.app_version);
                rating = (RatingBar) itemView.findViewById(R.id.app_rating);
            }


        }

        public static class CategoryStoreListViewHolder extends StoreListViewHolder{
            public final TextView name;


            public CategoryStoreListViewHolder(View itemView) {
                super(itemView);

                name = ((TextView) itemView.findViewById(R.id.category_first_level_name));


            }





        }


        public static class StoreListViewHolder extends RecyclerView.ViewHolder{

            public StoreListViewHolder(View itemView) {
                super(itemView);
            }


        }







    }





}
