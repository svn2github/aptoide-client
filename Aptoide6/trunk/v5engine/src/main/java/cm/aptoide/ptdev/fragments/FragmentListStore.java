package cm.aptoide.ptdev.fragments;

import android.app.Activity;
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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.EnumCategories;
import cm.aptoide.ptdev.EnumStoreTheme;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.V6.Rows.AppsRow;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by rmateus on 28-11-2014.
 */
public class FragmentListStore extends Fragment {

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private ArrayList<StoreListItem> items;
    private RecyclerView rRiew;
    private View view;
    private String theme;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theme = getArguments().getString("theme");
    }

    public static class GetStoreRequest extends RetrofitSpiceRequest<Response, GetStoreRequest.Webservice>{

        private String widgetId;
        private String refId;
        private String store;
        private StoreActivity.Sort sort;

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

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
            api.getApi_global_params().mature = String.valueOf(sPref.getBoolean("matureChkBox", false));

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
            listApps.datasets_params = null;
            api.getApi_params().set(getStore);
            listApps.order_by = sort.getSort();
            listApps.order_dir = sort.getDir();


            if(widgetId != null){
                listApps.datasets.add(refId);

                api.getApi_params().set(listApps);

                return getService().postApk2(api);
            }else{
                return getService().postApk(api);
            }


        }

        public void setSort(StoreActivity.Sort sort) {
            this.sort = sort;
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
    SwipeRefreshLayout swipeLayout;
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        items = new ArrayList<StoreListItem>();
        this.view = view;
        rRiew  = (RecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rRiew.setLayoutManager(linearLayoutManager);
        StoreListAdapter adapter = new StoreListAdapter(getActivity(), items);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(DurationInMillis.ALWAYS_EXPIRED);
            }
        });
        swipeLayout.setRefreshing(true);
        adapter.setStorename(getArguments().getString("storename"));

        rRiew.setAdapter(adapter);

        refresh(DurationInMillis.ONE_HOUR);
    }


    public void refresh(long expire){
        view.findViewById(R.id.error).setVisibility(View.GONE);
        view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        view.findViewById(R.id.list).setVisibility(View.GONE);
        GetStoreRequest request = new GetStoreRequest();
        StoreActivity.Sort sort = ((StoreActivity) getActivity()).getSort().getSort();
        request.setSort(sort);

        request.setStore(getArguments().getString("storename"));

        if(getArguments().containsKey("widgetrefid")){
            request.setWidgetId(getArguments().getString("widgetrefid"));
            request.setRefId(getArguments().getString("refid"));
        }

        manager.execute(request, getArguments().getString("storename") + sort.getDir() + sort.getSort() + getArguments().getString("widgetrefid") + getArguments().getString("refid") , expire, listener);
    }

    RequestListener<Response> listener = new RequestListener<Response>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            view.findViewById(R.id.please_wait).setVisibility(View.GONE);
            view.findViewById(R.id.error).setVisibility(View.VISIBLE);


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
                            app.setDownloads(apk.downloads.intValue());
                            app.setCategory_ref_id(widgetrefid);
                            app.setName(apk.name);
                            app.setIcon(apk.icon);
                            app.setMd5sum(apk.md5sum);
                            app.setRepo(apk.store_name);
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


                        WidgetCategory item = new WidgetCategory();
                        item.refid = widget.data.ref_id;
                        item.widgetid = widget.widgetid;
                        item.name = widget.name;
                        item.theme = theme;
                        item.store_id = getArguments().getLong("storeid");
                        map.add(item);


                }

                if (dataset != null ) {

                    if(dataset.get(widgetrefid).data != null) {

                        List<Response.ListApps.Apk> apksList = dataset.get(widgetrefid).data.list;

                        for (Response.ListApps.Apk apk : apksList) {

                            App app = new App();
                            app.setName(apk.name);
                            app.setIcon(apk.icon);
                            app.setMd5sum(apk.md5sum);
                            app.setRepo(apk.store_name);
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
            items.clear();
            items.addAll(map);
            rRiew.getAdapter().notifyDataSetChanged();
            swipeLayout.setRefreshing(false);
            view.findViewById(R.id.please_wait).setVisibility(View.GONE);
            view.findViewById(R.id.list).setVisibility(View.VISIBLE);

        }
    };


    public class WidgetCategory implements StoreListItem{

        public String refid;
        public String widgetid;
        public String name;
        public String theme;
        public long store_id;

        @Override
        public String toString() {
            return refid + " "+ widgetid + " " + name + " " + theme + " " + store_id ;
        }

        @Override
        public int getItemViewType() {
            return 1;
        }


    }

    public class App implements StoreListItem{

        private String category_ref_id;
        private int downloads;

        private String name;
        private float rating;
        private String icon;
        private String versionName;
        private String repo;
        private String md5sum;

        @Override
        public int getItemViewType() {
            return 0;
        }

        public String getCategory_ref_id() {
            return category_ref_id;
        }

        public void setCategory_ref_id(String category_ref_id) {
            this.category_ref_id = category_ref_id;
        }

        public int getDownloads() {
            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
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

        public String getRepo() {
            return repo;
        }

        public void setRepo(String repo) {
            this.repo = repo;
        }

        public String getMd5sum() {
            return md5sum;
        }

        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
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

                    if("group_top".equals(appItem.getCategory_ref_id())){
                        appHolder.versionName.setText(context.getString(R.string.X_download_number, withSuffix(String.valueOf(appItem.getDownloads()))));
                    }else{
                        appHolder.versionName.setText(appItem.getVersionName());
                    }




                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, AppViewActivity.class);
                            i.putExtra("fromRelated", true);
                            i.putExtra("md5sum", appItem.getMd5sum());
                            i.putExtra("repoName", appItem.getRepo());
                            i.putExtra("download_from", "store");
                            context.startActivity(i);
                            FlurryAgent.logEvent("Store_Clicked_On_App");

                        }
                    });

                    break;
                case 1:
                    final WidgetCategory storeListItem = (WidgetCategory) list.get(position);

                    CategoryStoreListViewHolder categoryHolder = (CategoryStoreListViewHolder) holder;
                    categoryHolder.name.setText(storeListItem.name);
                    try{
                        ImageLoader.getInstance().displayImage(EnumCategories.getCategoryIcon(Integer.parseInt(storeListItem.refid.substring(4)), storename), categoryHolder.icon);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String refid = storeListItem.refid;
                            String widgetid = storeListItem.widgetid;


//                            Log.d("FragmentListStore", storeListItem.toString());

                            Fragment fragment;
                            Bundle bundle = new Bundle();

                            if("comments".equals(refid)){
                                bundle.putLong("storeid", storeListItem.store_id);
                                fragment = new LatestCommentsFragment();
                                FlurryAgent.logEvent("Store_Clicked_On_Latest_Comments");

                            } else if("likes".equals(refid)){
                                bundle.putLong("storeid", storeListItem.store_id);
                                fragment = new LatestLikesFragment();
                                FlurryAgent.logEvent("Store_Clicked_On_Latest_Likes");

                            }else {

                                fragment = new FragmentListStore();

                                bundle.putString("widgetrefid", widgetid);
                                bundle.putString("refid", refid);
                                bundle.putString("storename", storename);

                                Map<String, String> flurryParams = new HashMap<String, String>();
                                flurryParams.put("Category", storeListItem.name);
                                FlurryAgent.logEvent("Store_Clicked_On", flurryParams);

                            }

                            fragment.setArguments(bundle);

                            ((ActionBarActivity) context).getSupportFragmentManager().beginTransaction().setBreadCrumbTitle(storeListItem.name).replace(R.id.content_layout, fragment).addToBackStack(storeListItem.name).commit();
                        }
                    });

                    int catid;
                    try{

                        if("group_top".equals(storeListItem.refid)){

                            catid = EnumCategories.TOP_APPS;
//                            Log.d("FragmentListStore", "group_top "+catid);
                        }else if("likes".equals(storeListItem.refid)) {


                            catid = EnumCategories.LATEST_LIKES;
//                            Log.d("FragmentListStore", "likes "+catid);

                        }else if("comments".equals(storeListItem.refid)) {

                            catid = EnumCategories.LATEST_COMMENTS;
//                            Log.d("FragmentListStore", "comments "+catid);

                        }else if("group_latest".equals(storeListItem.refid)) {

                            catid = EnumCategories.LATEST_APPS;
//                            Log.d("FragmentListStore", "group_latest "+catid);

                        }else {

                            catid = Integer.valueOf(storeListItem.refid.replaceAll("[^-?0-9]+", ""));
//                            Log.d("FragmentListStore", "catid "+catid);

                        }
                    }catch (Exception e){
                        catid = 0;
                    }

                    EnumStoreTheme theme;
                    try{
                        String themeString = storeListItem.theme;
                        theme = EnumStoreTheme.valueOf("APTOIDE_STORE_THEME_" + themeString);
                        Log.d("FragmentListStore", "theme "+theme);

                    }catch (Exception e){
                        theme = EnumStoreTheme.APTOIDE_STORE_THEME_ORANGE;
                    }

                    switch (catid) {
                        case EnumCategories.APPLICATIONS:
                            categoryHolder.icon.setImageResource(R.drawable.cat_applications);
                            break;
                        case EnumCategories.GAMES:
                            categoryHolder.icon.setImageResource(R.drawable.cat_games);
                            break;
                        case EnumCategories.TOP_APPS:
                            categoryHolder.icon.setImageResource(R.drawable.cat_top_apps);
                            break;
                        case EnumCategories.LATEST_APPS:
                            categoryHolder.icon.setImageResource(R.drawable.cat_latest);
                            break;
                        case EnumCategories.LATEST_LIKES:
                            categoryHolder.icon.setImageResource(R.drawable.cat_likes);
                            break;
                        case EnumCategories.LATEST_COMMENTS:
                            categoryHolder.icon.setImageResource(R.drawable.cat_comments);
                            break;


                        default:
                            String iconUrl = EnumCategories.getCategoryIcon(catid, storeListItem.name);
                            if (iconUrl != null) {
                                ImageLoader.getInstance().displayImage(iconUrl, categoryHolder.icon);
                            } else {
                                categoryHolder.icon.setImageResource(theme.getStoreCategoryDrawable());
                            }
                            break;
                    }


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
            public final TextView name;

            public AppStoreListViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.app_name);
                appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
                appName = (TextView) itemView.findViewById(R.id.app_name);
                versionName = (TextView) itemView.findViewById(R.id.app_version);
                rating = (RatingBar) itemView.findViewById(R.id.app_rating);
            }


        }

        public static class CategoryStoreListViewHolder extends StoreListViewHolder{
            public final TextView name;
            public final ImageView icon;

            public CategoryStoreListViewHolder(View itemView) {
                super(itemView);

                name = ((TextView) itemView.findViewById(R.id.category_first_level_name));
                icon = ((ImageView) itemView.findViewById(R.id.category_first_level_icon));

            }

        }


        public static class StoreListViewHolder extends RecyclerView.ViewHolder{

            public StoreListViewHolder(View itemView) {
                super(itemView);
            }


        }







    }





}
