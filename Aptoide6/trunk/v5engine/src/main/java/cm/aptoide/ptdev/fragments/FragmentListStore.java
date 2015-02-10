package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.EnumCategories;
import cm.aptoide.ptdev.EnumStoreTheme;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AdultHiddenDialog;
import cm.aptoide.ptdev.dialogs.PasswordDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import retrofit.http.Body;
import retrofit.http.POST;

import static cm.aptoide.ptdev.utils.AptoideUtils.getSharedPreferences;
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
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        manager.shouldStop();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void refresh(RepoCompleteEvent event){
        refresh(DurationInMillis.ALWAYS_EXPIRED);
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
        private String password;
        private String username;

        public Number getOffset() {
            return offset;
        }

        public void setOffset(Number offset) {
            this.offset = offset;
        }

        private Number offset = 0;

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

        public void setUsername(String username){
            this.username = username;
        }

        public void setPassword(String password){
            this.password = password;
        }

        @Override
        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();

            api.getApi_global_params().setLang(AptoideUtils.getMyCountry(Aptoide.getContext()));
            api.getApi_global_params().setStore_name(store);

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
            api.getApi_global_params().mature = String.valueOf(!sPref.getBoolean("matureChkBox", true));

            Api.GetStore getStore = new Api.GetStore();

            Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
            widgetParams.setContext("store");
            widgetParams.setWidgetid(widgetId);
            //widgetParams.offset = offset;
            //widgetParams.limit = 3;

            if (username != null) {
                api.getApi_global_params().store_user = username;
                api.getApi_global_params().store_pass_sha1 = password;
            }

            getStore.getDatasets_params().set(widgetParams);

            //getStore.addDataset(categoriesParams.getDatasetName());
            getStore.addDataset(widgetParams.getDatasetName());

            Api.ListApps listApps = new Api.ListApps();
            listApps.datasets_params = null;
            api.getApi_params().set(getStore);
            listApps.order_by = sort.getSort();
            listApps.order_dir = sort.getDir();
            listApps.offset = offset;


            if(widgetId != null){
                listApps.datasets.add(refId);
                api.getApi_params().set(listApps);
                if(offset.intValue() > 0){
                    return getService().postApk3(api);
                }else{
                    return getService().postApk2(api);
                }
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

            @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/listApps/")
            Response postApk3(@Body Api user) throws Response.TicketException;
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
        StoreListAdapter adapter = new StoreListAdapter(getActivity(), items, getFragmentManager(), getArguments());
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

    public void setLoading(View view){
        view.findViewById(R.id.no_network_connection).setVisibility(View.GONE);

        view.findViewById(R.id.error).setVisibility(View.GONE);
        view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        view.findViewById(R.id.swipe_container).setVisibility(View.GONE);
    }

    private void setError(final View view, final SpiceManager manager, final RequestListener requestListener, final SpiceRequest request){
        view.findViewById(R.id.error).setVisibility(View.VISIBLE);
        view.findViewById(R.id.swipe_container).setVisibility(View.GONE);
        view.findViewById(R.id.no_network_connection).setVisibility(View.GONE);
        view.findViewById(R.id.empty).setVisibility(View.GONE);
        view.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(view);
                manager.execute(request, requestListener);

            }
        });
    }
    GetStoreRequest request;

    public void refresh(long expire){
        setLoading(view);
        request = new GetStoreRequest();
        StoreActivity.Sort sort = ((CategoryCallback) getActivity()).getSort().getSort();
        request.setSort(sort);
        items.clear();
        rRiew.getAdapter().notifyDataSetChanged();
        Login login = ((CategoryCallback) getActivity()).getLogin();

        if( login != null){
            request.setUsername(login.getUsername());
            request.setPassword(login.getPassword());
        }

        request.setStore(getArguments().getString("storename"));

        if(getArguments().containsKey("widgetrefid")){
            request.setWidgetId(getArguments().getString("widgetrefid"));
            request.setRefId(getArguments().getString("refid"));
        }

        manager.execute(request, 0 + getArguments().getString("storename") + sort.getDir() + sort.getSort() + getArguments().getString("widgetrefid") + getArguments().getString("refid") + getSharedPreferences().getBoolean("matureChkBox", true) , expire, listener);

    }

    private int previousTotal = 0;
    private int offset;

    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int total;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 20:
                String username = data.getStringExtra("username");
                String password = null;
                try {
                    password = AptoideUtils.Algorithms.computeSHA1sum(data.getStringExtra("password"));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                List<Fragment> fragments = getFragmentManager().getFragments();
                Bundle bundle = new Bundle();

                bundle.putString("username", username);

                bundle.putString("password", password);
                Login login = new Login();
                login.setUsername(username);
                login.setPassword(password);
                ((CategoryCallback)getActivity()).setLogin(login);


                new Database(Aptoide.getDb()).updateStoreLogin(getArguments().getString("storename"), username, password);

                refresh(DurationInMillis.ALWAYS_EXPIRED);
                break;
        }
    }

    RequestListener<Response> listener = new RequestListener<Response>() {


        @Override
        public void onRequestFailure(SpiceException spiceException) {



            if(items.isEmpty()) {

                if (spiceException instanceof NoNetworkException) {

                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.list).setVisibility(View.GONE);
                    view.findViewById(R.id.no_network_connection).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.retry_no_network).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.execute(request, listener);
                            view.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.list).setVisibility(View.GONE);
                            view.findViewById(R.id.no_network_connection).setVisibility(View.GONE);
                        }
                    });

                } else {

                    setError(view, manager, listener, request);
                }

            }else{

                if(loading){
                    items.remove(items.size() - 1);
                }

                rRiew.getAdapter().notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
                loading = false;

            }
        }


        int adsPosition = 0;
        boolean adsLoaded ;

        @Override
        public void onRequestSuccess(final Response response) {
            final ArrayList<StoreListItem> map = new ArrayList<StoreListItem>();

            try {
                if (response.responses.getStore.errors!=null && !response.responses.getStore.errors.isEmpty() && response.responses.getStore.errors.get(0).code.equals("STORE-3") || response.responses.getStore.errors.get(0).code.equals("STORE-5")) {
                    DialogFragment fragment = new PasswordDialog();
                    fragment.setTargetFragment(FragmentListStore.this, 20);
                    fragment.show(getFragmentManager(), "passwordDialog");
                    return;
                }
            }catch (Exception e){

            }


            try {

                List<Response.GetStore.Widgets.Widget> list = new ArrayList<>();

                try {
                    list = response.responses.getStore.datasets.widgets.data.list;
                }catch (NullPointerException e){

                }


                HashMap<String, Response.ListApps.Category> dataset = null;
                if (response.responses.listApps != null) {
                    dataset = response.responses.listApps.datasets.getDataset();
                }




                int hidden = 0;
                String widgetrefid = getArguments().getString("refid");
                if (list.isEmpty()) {

                    if (dataset != null) {


                        if (dataset.get(widgetrefid).data != null) {
                            Response.ListApps.Category category = dataset.get(widgetrefid);

                            if(category !=null && category.data!=null) {
                                offset = category.data.next;
                                total = category.data.total;
                            }

                            hidden += dataset.get(widgetrefid).data.hidden;
                            List<Response.ListApps.Apk> apksList = dataset.get(widgetrefid).data.list;

                            for (Response.ListApps.Apk apk : apksList) {
                                App app = new App();
                                app.setDownloads(apk.downloads.intValue());
                                app.setCategory_ref_id(widgetrefid);
                                app.setName(apk.name);
                                app.setIcon(apk.icon);
                                app.setMd5sum(apk.md5sum);
                                app.setRepo(apk.store_name);
                                if (apk.rating != null) {
                                    app.setRating(apk.rating.floatValue());
                                }
                                app.setVersionName(apk.vername);
                                map.add(app);
                            }

                        } else {
                            manager.removeDataFromCache(Response.class);
                        }
                    }

                } else {



                    for (Response.GetStore.Widgets.Widget widget : list) {


                        WidgetCategory item = new WidgetCategory();
                        item.refid = widget.data.ref_id;
                        item.widgetid = widget.widgetid;
                        item.icon = widget.data.icon;
                        item.name = widget.name;
                        item.theme = theme;
                        item.apps_count = widget.data.apps_count;
                        item.store_id = getArguments().getLong("storeid");
                        map.add(item);


                    }

                    adsPosition = map.size();

                    if (dataset != null) {

                        if (dataset.get(widgetrefid).data != null) {

                            Response.ListApps.Category category = dataset.get(widgetrefid);

                            if(category !=null && category.data!=null) {
                                offset = category.data.next;
                                total = category.data.total;
                            }

                            List<Response.ListApps.Apk> apksList = dataset.get(widgetrefid).data.list;
                            hidden += dataset.get(widgetrefid).data.hidden;

                            for (Response.ListApps.Apk apk : apksList) {

                                App app = new App();
                                app.setName(apk.name);
                                app.setIcon(apk.icon);
                                app.setMd5sum(apk.md5sum);
                                app.setRepo(apk.store_name);
                                if (apk.rating != null) {
                                    app.setRating(apk.rating.floatValue());
                                }
                                app.setVersionName(apk.vername);
                                map.add(app);

                            }

                        }
                    }

                }

                //string.clear();

                if(!adsLoaded && getArguments().getString("widgetrefid") != null) {
                    GetAdsRequest adrequest = new GetAdsRequest(Aptoide.getContext());

                    adrequest.setLimit(1);
                    adrequest.setLocation("homepage");
                    adrequest.setKeyword("__NULL__");
                    adrequest.setCategories(getArguments().getString("widgetrefid"));
                    manager.execute(adrequest, new RequestListener<ApkSuggestionJson>() {

                        @Override
                        public void onRequestFailure(SpiceException spiceException) {

                        }

                        @Override
                        public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {


                            for (ApkSuggestionJson.Ads ad : apkSuggestionJson.getAds()) {

                                AdApp app = new AdApp();
                                app.setName(ad.getData().getName());
                                app.setIcon(ad.getData().getIcon());
                                app.setMd5sum(ad.getData().getMd5sum());
                                app.setRepo(ad.getData().getRepo());
                                if (ad.getData().getStars() != null) {
                                    app.setRating(ad.getData().getStars().floatValue());
                                }
                                app.setVersionName(ad.getData().getVername());
                                app.setAd(ad);
                                items.add(adsPosition,app);
                                rRiew.getAdapter().notifyItemRangeInserted(adsPosition, 1);

                            }


                        }
                    });
                }


                rRiew.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {

                        Log.d("AptoideAdapter", "scrolling" + " " + offset + " " + total);

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
                            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold) && offset < total) {
                                // End has been reached
                                loading = true;

                                items.add(new StoreListItem() {
                                    @Override
                                    public int getItemViewType() {
                                        return 123456789;
                                    }
                                });


                                rRiew.getAdapter().notifyItemInserted(rRiew.getAdapter().getItemCount());

                                request = new GetStoreRequest();
                                StoreActivity.Sort sort = ((CategoryCallback) getActivity()).getSort().getSort();
                                request.setSort(sort);

                                Login login = ((CategoryCallback) getActivity()).getLogin();

                                if(login!=null){

                                    request.setUsername(login.getUsername());
                                    request.setPassword(login.getPassword());
                                }

                                request.setOffset(offset);
                                request.setStore(getArguments().getString("storename"));

                                if(getArguments().containsKey("widgetrefid")){
                                    request.setWidgetId(getArguments().getString("widgetrefid"));
                                    request.setRefId(getArguments().getString("refid"));
                                }

                                manager.execute(request, offset + getArguments().getString("storename") + sort.getDir() + sort.getSort() + getArguments().getString("widgetrefid") + getArguments().getString("refid") + getSharedPreferences().getBoolean("matureChkBox", true) , DurationInMillis.ONE_DAY, listener);
                                // Do something

                            }
                        }

                    }
                });

                if(loading && !items.isEmpty()){
                    items.remove(items.size() - 1);
                }

                items.addAll(map);
                rRiew.getAdapter().notifyDataSetChanged();
                swipeLayout.setRefreshing(false);


                view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                view.findViewById(R.id.swipe_container).setVisibility(View.VISIBLE);

                if(hidden > 0 && getSharedPreferences().getBoolean("showadulthidden", true) && getFragmentManager().findFragmentByTag("hiddenadult")==null){
                    new AdultHiddenDialog().show(getFragmentManager(), "hiddenadult");
                }

                loading = false;

            }catch (Exception e){
                e.printStackTrace();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String s = mapper.writeValueAsString(response);
                    Crashlytics.logException(new Throwable(s, e));
                } catch (JsonProcessingException e1) {
                    e1.printStackTrace();
                }

                setError(view, manager, listener, request);

            }

        }
    };



    public class WidgetCategory implements StoreListItem{

        public String icon;
        public String refid;
        public String widgetid;
        public String name;
        public String theme;
        public int apps_count;
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

    public class AdApp extends App{

        private ApkSuggestionJson.Ads ad;

        @Override
        public int getItemViewType() {
            return 2;
        }


        public ApkSuggestionJson.Ads getAd() {
            return ad;
        }

        public void setAd(ApkSuggestionJson.Ads ad) {
            this.ad = ad;
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
        private final Bundle parentBundle;


        private String storename;

        public StoreListAdapter(Context context, List<StoreListItem> list, FragmentManager fragmentManager, Bundle bundle) {
            this.list = list;
            this.context = context;
            this.fragmentManager = fragmentManager;
            this.parentBundle = bundle;

        }

        private Context context;
        private final FragmentManager fragmentManager;

        @Override
        public StoreListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if(viewType==123456789) {
                return new StoreListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_bar, parent, false)) {};
            }

            switch (viewType){
                case 0:
                case 2:
                    return new AppStoreListViewHolder(LayoutInflater.from(context).inflate(R.layout.row_app_standard, parent, false));
                case 1:
                    return new CategoryStoreListViewHolder(LayoutInflater.from(context).inflate(R.layout.row_item_category_first_level_list, parent, false));
            }

            return null;
        }

        @Override
        public void onBindViewHolder(StoreListViewHolder holder, final int position) {

            String icon = null;

            switch (getItemViewType(position)) {

                case 2:
                    final AdApp adItem = (AdApp) list.get(position);
                    AppStoreListViewHolder adAppHolder = (AppStoreListViewHolder) holder;
                    adAppHolder.name.setText(adItem.getName());

                    adAppHolder.appName.setText(Html.fromHtml(adItem.getName()).toString());
                    adAppHolder.rating.setRating(adItem.getRating());

                    icon = adItem.getIcon();
                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
                    }
                    ImageLoader.getInstance().displayImage(icon,adAppHolder.appIcon);


                    adAppHolder.versionName.setText(adItem.getVersionName());

                    final ApkSuggestionJson.Ads apkSuggestion = adItem.getAd();

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(v.getContext(), Aptoide.getConfiguration().getAppViewActivityClass());
                            long id = apkSuggestion.getData().getId().longValue();
                            i.putExtra("id", id);
                            i.putExtra("packageName", apkSuggestion.getData().getPackageName());
                            i.putExtra("repoName", apkSuggestion.getData().getRepo());
                            i.putExtra("fromSponsored", true);
                            i.putExtra("location", "homepage");
                            i.putExtra("keyword", "__NULL__");
                            i.putExtra("cpc", apkSuggestion.getInfo().getCpc_url());
                            i.putExtra("cpi", apkSuggestion.getInfo().getCpi_url());
                            i.putExtra("whereFrom", "sponsored");
                            i.putExtra("download_from", "sponsored");
                            FlurryAgent.logEvent("Home_Page_Clicked_On_Sponsored_App");

                            if(apkSuggestion.getPartner() != null){
                                Bundle bundle = new Bundle();

                                bundle.putString("partnerType", apkSuggestion.getPartner().getPartnerInfo().getName());
                                bundle.putString("partnerClickUrl", apkSuggestion.getPartner().getPartnerData().getClick_url());

                                i.putExtra("partnerExtra", bundle);
                            }

                            v.getContext().startActivity(i);
                            FlurryAgent.logEvent("Store_Clicked_On_App");

                        }
                    });
                    break;

                case 0:

                    final App appItem = (App) list.get(position);
                    StoreActivity.Sort sort = ((CategoryCallback) context).getSort().getSort();
                    AppStoreListViewHolder appHolder = (AppStoreListViewHolder) holder;
                    appHolder.name.setText(appItem.getName());

                    appHolder.appName.setText(Html.fromHtml(appItem.getName()).toString());


                    if(sort.equals(StoreActivity.Sort.DOWNLOADS)){
                        appHolder.rating.setVisibility(View.GONE);
                        appHolder.downloads.setVisibility(View.VISIBLE);
                        appHolder.downloads.setText(context.getString(R.string.X_download_number,AptoideUtils.withSuffix(String.valueOf(appItem.getDownloads()))));

                    }else{
                        appHolder.rating.setVisibility(View.VISIBLE);
                        appHolder.rating.setRating(appItem.getRating());
                        appHolder.downloads.setVisibility(View.GONE);

                    }



                    icon = appItem.getIcon();
                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
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
                            Intent i = new Intent(context, Aptoide.getConfiguration().getAppViewActivityClass());
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

                    if(storeListItem.apps_count>0){
                        ((CategoryStoreListViewHolder) holder).apps_count.setText( storeListItem.apps_count + "");
                    }else{
                        ((CategoryStoreListViewHolder) holder).apps_count.setText( "");
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

                            if(parentBundle.containsKey("username")){
                                bundle.putString("username", parentBundle.getString("username"));
                                bundle.putString("password", parentBundle.getString("password"));
                            }

                            fragment.setArguments(bundle);

                            fragmentManager.beginTransaction().setBreadCrumbTitle(storeListItem.name).replace(R.id.content_layout, fragment).addToBackStack(storeListItem.name).commit();
                        }
                    });

                    if(storeListItem.icon != null){

                        if (storeListItem.icon.contains("_cat_icon")) {
                            String[] splittedUrl = storeListItem.icon.split("\\.(?=[^\\.]+$)");
                            icon = splittedUrl[0] + "_" + Aptoide.iconSize + "." + splittedUrl[1];
                        }

                        ImageLoader.getInstance().displayImage(icon, categoryHolder.icon);

                    }else {

                        int catid;
                        try {

                            if ("group_top".equals(storeListItem.refid)) {
                                catid = EnumCategories.TOP_APPS;
                            } else if ("likes".equals(storeListItem.refid)) {
                                catid = EnumCategories.LATEST_LIKES;
                            } else if ("comments".equals(storeListItem.refid)) {
                                catid = EnumCategories.LATEST_COMMENTS;
                            } else if ("group_latest".equals(storeListItem.refid)) {
                                catid = EnumCategories.LATEST_APPS;
                            } else if ("ucat_3239".equals(storeListItem.refid)) {
                                catid = EnumCategories.APTOIDE_PUBLISHERS;
                            } else {
                                catid = Integer.valueOf(storeListItem.refid.replaceAll("[^-?0-9]+", ""));
                            }
                        } catch (Exception e) {
                            catid = 0;
                        }

                        EnumStoreTheme theme;
                        try {
                            String themeString = storeListItem.theme;
                            theme = EnumStoreTheme.valueOf("APTOIDE_STORE_THEME_" + themeString);
                            Log.d("FragmentListStore", "theme " + theme);

                        } catch (Exception e) {
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
                            case EnumCategories.APTOIDE_PUBLISHERS:
                                categoryHolder.icon.setImageResource(R.drawable.cat_publishers);
                                break;

                            default:
                                String iconUrl = EnumCategories.getCategoryIcon(catid, storename);
                                if (iconUrl != null) {
                                    ImageLoader.getInstance().displayImage(iconUrl, categoryHolder.icon);
                                } else {
                                    categoryHolder.icon.setImageResource(theme.getStoreCategoryDrawable());
                                }
                                break;
                        }
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

            public final TextView downloads;

            public AppStoreListViewHolder(View itemView) {
                super(itemView);
                ;
                name = (TextView) itemView.findViewById(R.id.app_name);
                appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
                appName = (TextView) itemView.findViewById(R.id.app_name);
                versionName = (TextView) itemView.findViewById(R.id.app_version);
                downloads = (TextView) itemView.findViewById(R.id.downloads);
                rating = (RatingBar) itemView.findViewById(R.id.app_rating);
            }


        }

        public static class CategoryStoreListViewHolder extends StoreListViewHolder{
            public final TextView name;
            public final TextView apps_count;
            public final ImageView icon;

            public CategoryStoreListViewHolder(View itemView) {
                super(itemView);

                name = ((TextView) itemView.findViewById(R.id.category_first_level_name));
                icon = ((ImageView) itemView.findViewById(R.id.category_first_level_icon));
                apps_count = ((TextView) itemView.findViewById(R.id.category_first_level_number));


            }

        }


        public static class StoreListViewHolder extends RecyclerView.ViewHolder{

            public StoreListViewHolder(View itemView) {
                super(itemView);
            }


        }

    }





}
