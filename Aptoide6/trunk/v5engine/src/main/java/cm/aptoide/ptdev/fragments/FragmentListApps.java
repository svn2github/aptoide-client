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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreActivity;
import cm.aptoide.ptdev.MoreFriendsInstallsActivity;
import cm.aptoide.ptdev.MoreHighlightedActivity;
import cm.aptoide.ptdev.MoreUserBasedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StickyRecyclerHeadersDecoration;
import cm.aptoide.ptdev.StickyRecyclerHeadersTouchListener;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.views.RoundedImageView;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.ListRecomended;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;
import retrofit.http.Body;
import retrofit.http.POST;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by rmateus on 21-11-2014.
 */
public class FragmentListApps extends Fragment {



    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private RequestListener<Response> requestListener;
    Map<String, String> flurryParams = new HashMap<String, String>();

    @Override
    public void onStart() {
        super.onStart();
        manager.start(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        if(manager.isStarted()){
            manager.shouldStop();
        }

    }

    public interface TestService{

        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
        Response postApk(@Body Api user) throws Response.TicketException;
    }

    public interface Displayable {
        int getViewType();

        long getHeaderId();

        boolean isMore();

        void bindView(RecyclerView.ViewHolder viewHolder);

        void onBindHeaderViewHolder(FragmentListApps.RecyclerAdapter.HeaderViewHolder viewHolder);
    }


    public static class TestRequest extends RetrofitSpiceRequest<Response, TestService> {


        private int offset;

        public TestRequest() {
            super(Response.class, TestService.class);

        }


        public void setOffset(int offset){

            this.offset = offset;
        }



        @Override
        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();
            final int BUCKET_SIZE = AptoideUtils.getBucketSize();
            api.getApi_global_params().setLang(AptoideUtils.getMyCountry(Aptoide.getContext()));
            api.getApi_global_params().setStore_name("apps");

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
            api.getApi_global_params().mature = String.valueOf(!sPref.getBoolean("matureChkBox", true));


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

            Api.CategoryParam highlightedParam = new Api.CategoryParam("EDITORS_group_hrand");
            highlightedParam.setLimit(5);

            Api.CategoryParam applicationsParam = new Api.CategoryParam("EDITORS_cat_1");
            applicationsParam.setLimit(BUCKET_SIZE * 2);

            Api.CategoryParam gamesParam = new Api.CategoryParam("EDITORS_cat_2");
            gamesParam.setLimit(BUCKET_SIZE * 2);


            listApps.datasets_params.set(highlightedParam);
            listApps.datasets_params.set(applicationsParam);
            listApps.datasets_params.set(gamesParam);

            listApps.limit = BUCKET_SIZE;
            listApps.datasets = null;

            api.getApi_params().set(listApps);


            Response response;


            response = getService().postApk(api);


            return response;

        }

    }


    public static class TimelineRow extends Row{


        public List<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>();


        public TimelineRow(List<TimelineListAPKsJson.UserApk> apks) {
            this.apks = apks;
        }


        public void addItem(TimelineListAPKsJson.UserApk apk) {
            apks.add(apk);
        }

        @Override
        public int getViewType() {
            return 1001;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder holder) {
            final RecyclerAdapter.TimelineRowViewHolder viewHolder = (RecyclerAdapter.TimelineRowViewHolder) holder;

            int i=0;
            for(final TimelineListAPKsJson.UserApk apk : apks) {
                final RecyclerAdapter.TimelineRowViewHolder.ItemViewHolder itemViewHolder = (RecyclerAdapter.TimelineRowViewHolder.ItemViewHolder) viewHolder.views[i].getTag();
                itemViewHolder.name.setText(apk.getApk().getName());
                String icon = apk.getApk().getIcon_hd();
                if(icon==null){
                    icon = apk.getApk().getIcon();
                }

                itemViewHolder.friend.setText(apk.getInfo().getUsername() + " " + ((RecyclerAdapter.TimelineRowViewHolder) holder).itemView.getContext().getString(R.string.installed_this));
                ImageLoader.getInstance().displayImage(apk.getInfo().getAvatar(), itemViewHolder.avatar);


                if(icon.contains("_icon")){
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] +"_"+ Aptoide.iconSize  + "."+ splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon, itemViewHolder.icon);
                //picasso.load(icon).into(itemViewHolder.icon);
                viewHolder.views[i].setClickable(true);
                viewHolder.views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(viewHolder.itemView.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apk.getApk().getMd5sum());
                        i.putExtra("repoName", apk.getApk().getRepo());
                        i.putExtra("download_from", "timeline");
                        viewHolder.itemView.getContext().startActivity(i);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_Timeline_App");

                    }
                });
                i++;
            }

        }


    }

    public static class FeaturedRow extends Row{

        final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new FadeInBitmapDisplayer(1000)).build();


        public FeaturedRow() {
            setEnabled(false);
        }

        @Override
        public int getViewType() {
            return super.getViewType() + 2000;
        }

        @Override
        public long getHeaderId() {
            return -1;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder holder) {

            final RecyclerAdapter.FeaturedViewHolder viewHolder = (RecyclerAdapter.FeaturedViewHolder) holder;
            for(int i = 0; i < apks.size() ; i++){
                final Response.ListApps.Apk apk = apks.get(i);
                ImageLoader.getInstance().displayImage(apk.graphic, viewHolder.images[i], options);
                viewHolder.frameLayouts[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(viewHolder.itemView.getContext(), AppViewActivity.class);
                        intent.putExtra("fromRelated", true);
                        intent.putExtra("md5sum", apk.md5sum);
                        intent.putExtra("repoName", apk.store_name);
                        intent.putExtra("download_from", "featured");
                        viewHolder.itemView.getContext().startActivity(intent);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_Featured_App");

                    }
                });
            }
        }
    }


    public static class CategoryRow implements Displayable{
        final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).displayer(new FadeInBitmapDisplayer(1000)).build();

        public String widgetid;
        public String header;
        public ArrayList<Response.GetStore.Widgets.Widget.WidgetCategory> list = new ArrayList<>();
        Map<String, String> flurryParams = new HashMap<String, String>();

        @Override
        public int getViewType() {
            return 3000 + list.size();
        }

        public void addItem(Response.GetStore.Widgets.Widget.WidgetCategory apk){
            list.add(apk);
        }

        @Override
        public long getHeaderId() {
            return -1;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder viewHolder) {

            RecyclerAdapter.HomeCategoryViewHolder holder = (RecyclerAdapter.HomeCategoryViewHolder) viewHolder;

            for(int i = 0; i < list.size(); i++){
                final Response.GetStore.Widgets.Widget.WidgetCategory widgetCategory = list.get(i);
                ImageLoader.getInstance().displayImage(widgetCategory.graphic, holder.views[i], options);
                ((FrameLayout)holder.views[i].getParent()).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MoreActivity.class);
                        intent.putExtra("widgetrefid", widgetCategory.ref_id);
                        intent.putExtra("widgetid", "apps_list:" + widgetCategory.ref_id);
                        intent.putExtra("widgetname", header);
                        v.getContext().startActivity(intent);

                        flurryParams.put("WidgetCategory", header);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_More", flurryParams);

                    }
                });
            }

        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
            viewHolder.tv.setText(header);
        }

        @Override
        public boolean isMore() {
            return false;
        }
    }


    public static class AdRow extends Row {


        public List<ApkSuggestionJson.Ads> ads = new ArrayList<ApkSuggestionJson.Ads>();


        public AdRow() {

        }

        @Override
        public int getViewType() {

            Log.d("Aptoide", ads.size() + "");

            return ads.size();
        }

        @Override
        public void bindView(RecyclerView.ViewHolder holder) {

            RecyclerAdapter.RowViewHolder viewHolder = (RecyclerAdapter.RowViewHolder) holder;

            int i = 0;
            for (final ApkSuggestionJson.Ads apkSuggestion : ads) {
                RecyclerAdapter.RowViewHolder.ItemViewHolder itemViewHolder = (RecyclerAdapter.RowViewHolder.ItemViewHolder) viewHolder.views[i].getTag();
                itemViewHolder.name.setText(apkSuggestion.getData().getName());
                String icon = apkSuggestion.getData().getIcon();
                itemViewHolder.category.setText(viewHolder.itemView.getContext().getString(R.string.sponsored));

                if (icon.contains("_icon")) {
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] +"_"+ Aptoide.iconSize + "." + splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon, itemViewHolder.icon);
                //picasso.load(icon).into(itemViewHolder.icon);
                viewHolder.views[i].setClickable(true);
                viewHolder.views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), AppViewActivity.class);
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
                    }
                });
                i++;

            }
        }
    }


    AdultContentRow.AdultInterface adultInterface = new AdultContentRow.AdultInterface() {
        @Override
        public void onAdultChange(boolean isChecked) {
            if (isChecked) {
                FlurryAgent.logEvent("Switch_Turned_On_Adult_Content");
                new AdultDialog().show(getFragmentManager(), "adultDialog");
            } else {
                ((GetStartActivityCallback) getActivity()).matureLock();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void refresh(RepoCompleteEvent event){
        TestRequest request = new TestRequest();
        manager.execute(request, "home", DurationInMillis.ALWAYS_EXPIRED, requestListener);
    }


    public static class AdultContentRow implements Displayable {

        public interface AdultInterface{
            void onAdultChange(boolean isChecked);
        }

        public AdultContentRow(AdultInterface adultInterface) {
            this.adultInterface = adultInterface;
        }

        AdultInterface adultInterface;

        public static final int VIEW_TYPE = 8723487;



        @Override
        public int getViewType() {
            return VIEW_TYPE;
        }

        @Override
        public long getHeaderId() {
            return -1;
        }

        @Override
        public void bindView(RecyclerView.ViewHolder viewHolder) {


            CompoundButton viewById = (CompoundButton) viewHolder.itemView.findViewById(R.id.adult_content);
            viewById.setOnCheckedChangeListener(null);

            viewById.setChecked(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true));

            viewById.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    adultInterface.onAdultChange(isChecked);

                }
            });
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
        }

        @Override
        public boolean isMore() {
            return false;
        }
    }




    public static class Row implements Displayable{


        public String header;
        public String widgetid;
        public List<Response.ListApps.Apk> apks = new ArrayList<Response.ListApps.Apk>();
        public String widgetrefid;
        private boolean more = true;
        //private Picasso picasso;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled = true;

        public Row() {


            //picasso = Picasso.with(context);
            //picasso.setIndicatorsEnabled(true);

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
        public void bindView(RecyclerView.ViewHolder holder) {
            RecyclerAdapter.RowViewHolder viewHolder = (RecyclerAdapter.RowViewHolder) holder;

            Context context = viewHolder.itemView.getContext();
            int i=0;

            for(final Response.ListApps.Apk apk : apks){
                RecyclerAdapter.RowViewHolder.ItemViewHolder itemViewHolder =
                        (RecyclerAdapter.RowViewHolder.ItemViewHolder) viewHolder.views[i].getTag();
                itemViewHolder.name.setText(apk.name);

                String icon = apk.icon;

                int downloads = apk.downloads.intValue();
                itemViewHolder.category.setText(context.getString(R.string.X_download_number, withSuffix(String.valueOf(downloads))));

                if(icon.contains("_icon")){
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_"+ Aptoide.iconSize  + "."+ splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon, itemViewHolder.icon);
                //picasso.load(icon).into(itemViewHolder.icon);
                viewHolder.views[i].setClickable(true);
                viewHolder.views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apk.md5sum);
                        i.putExtra("repoName", apk.store_name);
                        i.putExtra("download_from", "home");
                        v.getContext().startActivity(i);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_App");

                    }
                });
                i++;
            }

        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
            viewHolder.tv.setText(header);
        }

        public void setMore(boolean more) {
            this.more = more;
        }

        public boolean isMore() {
            return more;
        }
    }

    int BUCKET_SIZE = AptoideUtils.getBucketSize();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);

        final RecyclerView view = (RecyclerView) rootView.findViewById(R.id.list);
        final ArrayList<Displayable> string = new ArrayList<>(20);

        final RecyclerAdapter mAdapter = new RecyclerAdapter(getActivity(), string);

        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
        StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(view, stickyRecyclerHeadersDecoration);

        touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View viewHeader, int i, long l) {

                if((((RecyclerAdapter) view.getAdapter()).list.get(i)).isMore()) {

                    Intent intent;
                    String widgetid = ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetid;
                    if (widgetid.equals("timeline")) {
                        intent = new Intent(getActivity(), MoreFriendsInstallsActivity.class);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_More_Timeline_Installs");
                    } else if (widgetid.equals("recommended")) {
                        intent = new Intent(getActivity(), MoreUserBasedActivity.class);
                        intent.putExtra("widgetname", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_More_Recommended");
                    } else if (widgetid.equals("highlighted")) {
                        intent = new Intent(getActivity(), MoreHighlightedActivity.class);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_More_Highlighted");
                    } else {
                        intent = new Intent(getActivity(), MoreActivity.class);
                        intent.putExtra("widgetid", widgetid);
                        intent.putExtra("widgetrefid", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetrefid);
                        intent.putExtra("widgetname", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);
                        flurryParams.put("WidgetCategory", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);
                        FlurryAgent.logEvent("Home_Page_Clicked_On_More", flurryParams);

                    }

                    startActivity(intent);
                }
            }
        });
        view.addOnItemTouchListener(touchListener);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        view.setAdapter(mAdapter);
        view.setLayoutManager(linearLayoutManager);
        view.addItemDecoration(stickyRecyclerHeadersDecoration);

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        final TestRequest request = new TestRequest();

        requestListener = new RequestListener<Response>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {

                if(spiceException instanceof NoNetworkException){

                    rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    rootView.findViewById(R.id.list).setVisibility(View.GONE);
                    rootView.findViewById(R.id.no_network_connection).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.retry_no_network).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.execute(request, "home" + BUCKET_SIZE, DurationInMillis.ONE_WEEK, requestListener);
                            rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.list).setVisibility(View.GONE);
                            rootView.findViewById(R.id.no_network_connection).setVisibility(View.GONE);
                        }
                    });

                } else {

                    rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    rootView.findViewById(R.id.list).setVisibility(View.GONE);
                    rootView.findViewById(R.id.error).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.execute(request, "home" + BUCKET_SIZE, DurationInMillis.ONE_WEEK, requestListener);
                            rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.list).setVisibility(View.GONE);
                            rootView.findViewById(R.id.error).setVisibility(View.GONE);
                        }
                    });
                }



            }

            @Override
            public void onRequestSuccess(Response response) {
                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.error).setVisibility(View.GONE);
                ArrayList<Displayable> map = new ArrayList<>();

                try {
                    List<Response.GetStore.Widgets.Widget> list = response.responses.getStore.datasets.widgets.data.list;
                    HashMap<String, Response.ListApps.Category> dataset = response.responses.listApps.datasets.getDataset();


                    for (Response.GetStore.Widgets.Widget widget : list) {

                        if (widget.type.equals("apps_list")) {

                            if ("apps_list:EDITORS_group_hrand".equals(widget.widgetid)) {

                                ArrayList<Response.ListApps.Apk> inElements = new ArrayList<>(dataset.get(widget.data.ref_id).data.list);

                                Row row = new FeaturedRow();
                                row.widgetid = widget.widgetid;
                                row.header = widget.name;
                                row.widgetrefid = widget.data.ref_id;
                                while (!inElements.isEmpty()) {
                                    row.addItem(inElements.remove(0));
                                }
                                map.add(row);

                            } else {

                                Response.ListApps.Category category = dataset.get(widget.data.ref_id);

                                if (category != null && category.data != null) {
                                    ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(dataset.get(widget.data.ref_id).data.list);

                                    while (!inElements.isEmpty()) {
                                        Row row = new Row();
                                        row.widgetid = widget.widgetid;
                                        row.header = widget.name;
                                        row.widgetrefid = widget.data.ref_id;
                                        for (int j = 0; j < BUCKET_SIZE && !inElements.isEmpty(); j++) {
                                            row.addItem(inElements.remove(0));
                                        }
                                        map.add(row);
                                    }
                                }

                            }

                        } else if ("categs_list".equals(widget.type)) {

                            ArrayList<Response.GetStore.Widgets.Widget.WidgetCategory> inElements = new ArrayList<Response.GetStore.Widgets.Widget.WidgetCategory>(widget.data.categories);

                            while (!inElements.isEmpty()) {
                                CategoryRow row = new CategoryRow();
                                row.header = widget.name;

                                for (int i = 0; i < 2 && !inElements.isEmpty(); i++) {
                                    Response.GetStore.Widgets.Widget.WidgetCategory widgetCategory = inElements.remove(0);

                                    row.addItem(widgetCategory);
                                }
                                map.add(row);
                            }

                        } else {
                            Row row = new Row();
                            row.setEnabled(false);
                            map.add(row);
                            ((RecyclerAdapter) view.getAdapter()).getPlaceholders().put(widget.type, map.size());
                        }

                    }

                    map.add(new AdultContentRow(adultInterface));

                    string.clear();
                    string.addAll(map);

                    Log.d("AptoideDebug", string.toString());
                    int offset = ((RecyclerAdapter) view.getAdapter()).offset;
                    ((RecyclerAdapter) view.getAdapter()).offset = offset + list.size();
                    view.getAdapter().notifyDataSetChanged();
                    //loading = false;

                    GetAdsRequest request = new GetAdsRequest(getActivity());

                    request.setLimit(BUCKET_SIZE);
                    request.setLocation("homepage");
                    request.setKeyword("__NULL__");

                    if (AptoideUtils.isLoggedIn(getActivity())) {

                        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());

                        if (defaultSharedPreferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {

                            ListApksInstallsRequest listRelatedApkRequest = new ListApksInstallsRequest();

                            listRelatedApkRequest.setLimit(String.valueOf(BUCKET_SIZE + 1));

                            manager.execute(listRelatedApkRequest, "MoreFriendsInstalls", DurationInMillis.ALWAYS_EXPIRED, new RequestListener<TimelineListAPKsJson>() {
                                @Override
                                public void onRequestFailure(SpiceException spiceException) {

                                }

                                @Override
                                public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
                                    TimelineRow row = new TimelineRow(new ArrayList<TimelineListAPKsJson.UserApk>());

                                    int i = 0;
                                    for (TimelineListAPKsJson.UserApk userApk : timelineListAPKsJson.getUsersapks()) {
                                        row.addItem(userApk);
                                        i++;

                                        if (i >= BUCKET_SIZE) {
                                            break;
                                        }

                                    }

                                    int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("timeline");
                                    row.header = getString(R.string.friends_installs);
                                    row.widgetid = "timeline";

                                    ((RecyclerAdapter) view.getAdapter()).list.add(location, row);

                                    (view.getAdapter()).notifyDataSetChanged();

                                }
                            });

                        }


                        final ListUserbasedApkRequest recommendedRequest = new ListUserbasedApkRequest(getActivity());

                        recommendedRequest.setLimit(BUCKET_SIZE);

                        manager.execute(recommendedRequest, new RequestListener<ListRecomended>() {
                            @Override
                            public void onRequestFailure(SpiceException spiceException) {

                            }

                            @Override
                            public void onRequestSuccess(ListRecomended listRecomended) {
                                Row row = new Row();
                                int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("xml_recommended");

                                if (listRecomended != null && listRecomended.getRepository() != null) {
                                    for (ListRecomended.Repository apkSuggestion : listRecomended.getRepository()) {

                                        row.header = getString(R.string.recommended_for_you);
                                        Response.ListApps.Apk apk = new Response.ListApps.Apk();
                                        row.widgetid = "recommended";

                                        if (apkSuggestion.getPackage() != null) {
                                            for (ListRecomended.Repository.Package apkRecommended : apkSuggestion.getPackage()) {
                                                apk.name = apkRecommended.getName();
                                                apk.icon = apkSuggestion.getIconspath() + apkRecommended.getIcon_hd();
                                                apk.downloads = apkRecommended.getDwn();
                                                apk.md5sum = apkRecommended.getMd5h();

                                                row.addItem(apk);
                                            }

                                        }
                                    }
                                }

                                if (!row.apks.isEmpty()) {

                                    ((RecyclerAdapter) view.getAdapter()).list.add(location, row);
                                    (view.getAdapter()).notifyDataSetChanged();

                                }

                            }
                        });
                    }
                    manager.execute(request, ((GetStartActivityCallback) getActivity()).getSponsoredCache() + BUCKET_SIZE, DurationInMillis.ALWAYS_RETURNED, new RequestListener<ApkSuggestionJson>() {
                        @Override
                        public void onRequestFailure(SpiceException spiceException) {

                        }

                        @Override
                        public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {
                            if (apkSuggestionJson != null && apkSuggestionJson.getAds() != null && apkSuggestionJson.getAds().size() > 0) {

                                AdRow row = new AdRow();

                                row.header = getString(R.string.highlighted_apps);
                                row.widgetid = "highlighted";

                                row.ads.addAll(apkSuggestionJson.getAds());

                                int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("ads_list");

                                ((RecyclerAdapter) view.getAdapter()).list.add(location, row);

                                (view.getAdapter()).notifyDataSetChanged();
                            }
                        }
                    });

                    swipeLayout.setRefreshing(false);
                }catch (Exception e){
                    rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    rootView.findViewById(R.id.list).setVisibility(View.GONE);
                    rootView.findViewById(R.id.error).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.execute(request, "home" + BUCKET_SIZE, DurationInMillis.ONE_WEEK, requestListener);
                            rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.list).setVisibility(View.GONE);
                            rootView.findViewById(R.id.error).setVisibility(View.GONE);

                        }
                    });

                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        String s = mapper.writeValueAsString(response);
                        Crashlytics.logException(new Throwable(s, e));
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }

                }
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


        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                manager.execute(request, "home" + BUCKET_SIZE, DurationInMillis.ALWAYS_EXPIRED, requestListener);
            }
        });

        manager.execute(request, "home" + BUCKET_SIZE, DurationInMillis.ONE_WEEK, requestListener);

        rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.GONE);

        return rootView;
    }

/*    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;*/

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerAdapter.HeaderViewHolder> {

        public int offset = 0;

        public List<Displayable> getList() {
            return list;
        }

        private final List<Displayable> list;


        private final HashMap<String, Integer> placeholders = new HashMap<>();

        public RecyclerAdapter(Context context, List<Displayable> list) {

            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).getViewType();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            int BUCKET_SIZE = AptoideUtils.getBucketSize();

            if(viewType == 8723487 ){
                View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_switch, parent, false);

                ((CompoundButton)inflate.findViewById(R.id.adult_content)).setChecked(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true));

                return new RecyclerView.ViewHolder(inflate) {
                };

            } else if(viewType>=3000) {
                LinearLayout inflate = new LinearLayout(context);
                inflate.setOrientation(LinearLayout.HORIZONTAL);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                inflate.setLayoutParams(params);
                return new HomeCategoryViewHolder(inflate, viewType % 3000, context);
            } else if( viewType >= 2000){
                View view = LayoutInflater.from(context).inflate(R.layout.row_app_home_featured, parent, false);
                return new FeaturedViewHolder(view, viewType % 2000, context);
            }else {

                LinearLayout inflate = new LinearLayout(context);
                inflate.setOrientation(LinearLayout.HORIZONTAL);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                inflate.setLayoutParams(params);

                if (viewType > 1000) {
                    return new TimelineRowViewHolder(inflate, BUCKET_SIZE, context);
                } else {
                    return new RowViewHolder(inflate, viewType, context);
                }
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            list.get(position).bindView(holder);
        }

        @Override
        public long getHeaderId(int position) {

            if(position > list.size() - 1){
                return -1;
            }else{
                return list.get(position).getHeaderId();
            }

        }

        @Override
        public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {

            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_separator, viewGroup, false);

            return new HeaderViewHolder(inflate);
        }

        @Override
        public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
            list.get(position).onBindHeaderViewHolder(viewHolder);

            if(list.get(position).isMore()){
                viewHolder.more.setVisibility(View.VISIBLE);
            }else{
                viewHolder.more.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public HashMap<String, Integer> getPlaceholders() {
            return placeholders;
        }


        public static class HomeCategoryViewHolder extends RecyclerView.ViewHolder{

            private final LinearLayout layout;
            private final ImageView[] views;

            public HomeCategoryViewHolder(View itemView, int itemCount , Context context) {
                super(itemView);

                views = new ImageView[itemCount];

                layout = (LinearLayout) itemView;
                for(int i = 0; i < itemCount; i++){
                    View inflate = LayoutInflater.from(itemView.getContext()).inflate(R.layout.category_home_item, layout, false);
                    views[i] = (ImageView) inflate.findViewById(R.id.image);
                    layout.addView(inflate);
                }
            }
        }

        public static class FeaturedViewHolder extends RecyclerView.ViewHolder {


            public ImageView[] images;
            public FrameLayout[] frameLayouts;


            int frameLayoutRes[] = {R.id.fl_central, R.id.fl_row1_left, R.id.fl_row1_right, R.id.fl_row2_left, R.id.fl_row2_right};
            int imageRes[] = {R.id.app_icon_central, R.id.app_icon_row1_left, R.id.app_icon_row1_right, R.id.app_icon_row2_left, R.id.app_icon_row2_right};


            public FeaturedViewHolder(View itemView, int viewtype, Context context) {
                super(itemView);

                images = new ImageView[viewtype];
                frameLayouts = new FrameLayout[viewtype];

                for(int i = 0; i < viewtype;i++){
                    frameLayouts[i] = (FrameLayout) itemView.findViewById(frameLayoutRes[i]);
                    images[i] = (ImageView) itemView.findViewById(imageRes[i]);
                }
            }
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

            public RowViewHolder(View itemView, int viewType, Context context1) {
                super(itemView);
                final int BUCKET_SIZE = AptoideUtils.getBucketSize();
                views = new View[viewType];
                Context context = itemView.getContext();
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

                if(viewType > 0){
                    for(int i = viewType; i < BUCKET_SIZE; i++){
                        View inflate = LayoutInflater.from(context).inflate(R.layout.home_item, layout, false);
                        inflate.setVisibility(View.INVISIBLE);
                        layout.addView(inflate);
                    }
                }
            }
        }

        public static class TimelineRowViewHolder extends RecyclerView.ViewHolder{
            public static class ItemViewHolder {
                public TextView name;
                public TextView friend;
                public ImageView icon;
                public RoundedImageView avatar;
            }

            public LinearLayout getLinearLayout() {
                return layout;
            }

            public View[] getViews() {
                return views;
            }

            final View[] views;
            final LinearLayout layout;

            public TimelineRowViewHolder(View itemView, int viewType, Context context1) {
                super(itemView);

                views = new View[viewType];
                Context context = itemView.getContext();
                layout = (LinearLayout) itemView;
                for(int i = 0; i < viewType; i++){
                    View inflate = LayoutInflater.from(context).inflate(R.layout.timeline_item, layout, false);
                    views[i] = inflate;
                    ItemViewHolder holder = new ItemViewHolder();
                    holder.name = (TextView) inflate.findViewById(R.id.app_name);
                    holder.icon = (ImageView) inflate.findViewById(R.id.app_icon);
                    holder.friend = (TextView) inflate.findViewById(R.id.app_friend);
                    holder.avatar = (RoundedImageView) inflate.findViewById(R.id.user_avatar);

                    inflate.setTag(holder);
                    layout.addView(inflate);

                }
            }
        }
        public static class HeaderViewHolder extends RecyclerView.ViewHolder{

            private final TextView more;
            TextView tv;


            public HeaderViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.header);
                more = (TextView) itemView.findViewById(R.id.more);
            }
        }
    }
}
