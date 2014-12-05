package cm.aptoide.ptdev.fragments;

import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
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
import cm.aptoide.ptdev.MoreFriendsInstallsActivity;
import cm.aptoide.ptdev.MoreHighlightedActivity;
import cm.aptoide.ptdev.MoreUserBasedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.HttpService;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.ListRecomended;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by rmateus on 21-11-2014.
 */
public class FragmentListApps extends Fragment {



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

        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
        Response postApk(@Body Api user) throws Response.TicketException;
    }

    public interface Displayable {
        int getViewType();

        long getHeaderId();

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

            Api.CategoryParam highlightedParam = new Api.CategoryParam("EDITORS_group_hrand");
            highlightedParam.setLimit(5);

            Api.CategoryParam applicationsParam = new Api.CategoryParam("EDITORS_cat_1");
            applicationsParam.setLimit(6);

            Api.CategoryParam gamesParam = new Api.CategoryParam("EDITORS_cat_2");
            gamesParam.setLimit(6);


            listApps.datasets_params.set(highlightedParam);
            listApps.datasets_params.set(applicationsParam);
            listApps.datasets_params.set(gamesParam);

            listApps.limit = 3;
            listApps.datasets = null;

            api.getApi_params().set(listApps);


            Response response;


            response = getService().postApk(api);


            return response;

        }

    }


    public static class TimelineRow extends Row{


        public List<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>(3);


        public TimelineRow(List<TimelineListAPKsJson.UserApk> apks) {
            this.apks = apks;
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
                itemViewHolder.friend.setText(apk.getInfo().getUsername() + " installed this.");

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
                        Intent i = new Intent(viewHolder.itemView.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apk.getApk().getMd5sum());
                        i.putExtra("repoName", apk.getApk().getRepo());
                        i.putExtra("download_from", "recommended_apps");
                        viewHolder.itemView.getContext().startActivity(i);
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
                        intent.putExtra("download_from", "recommended_apps");
                        viewHolder.itemView.getContext().startActivity(intent);
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
                    }
                });
            }

        }

        @Override
        public void onBindHeaderViewHolder(RecyclerAdapter.HeaderViewHolder viewHolder) {
            viewHolder.tv.setText(header);
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
                itemViewHolder.category.setText("Sponsored");

                if (icon.contains("_icon")) {
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_96x96" + "." + splittedUrl[1];
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



    public static class Row implements Displayable{


        public String header;
        public String widgetid;
        public List<Response.ListApps.Apk> apks = new ArrayList<Response.ListApps.Apk>();
        public String widgetrefid;
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
                        Intent i = new Intent(v.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apk.md5sum);
                        i.putExtra("repoName", apk.store_name);
                        i.putExtra("download_from", "recommended_apps");
                        v.getContext().startActivity(i);
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
        final View rootView = inflater.inflate(R.layout.fragment_list_apps, container, false);

        final RecyclerView view = (RecyclerView) rootView.findViewById(R.id.list);
        final ArrayList<Displayable> string = new ArrayList<Displayable>(20);

        final RecyclerAdapter mAdapter = new RecyclerAdapter(getActivity(), string);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
        StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(view, stickyRecyclerHeadersDecoration);


        touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View viewHeader, int i, long l) {

                Intent intent;
                String widgetid = ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetid;
                if(widgetid.equals("timeline")){
                    intent = new Intent(getActivity(), MoreFriendsInstallsActivity.class);
                }else if(widgetid.equals("recommended")){
                    intent = new Intent(getActivity(), MoreUserBasedActivity.class);
                }else if(widgetid.equals("highlighted")){
                    intent = new Intent(getActivity(), MoreHighlightedActivity.class);
                }else {
                    intent = new Intent(getActivity(), MoreActivity.class);
                    intent.putExtra("widgetid", widgetid);
                    intent.putExtra("widgetrefid", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).widgetrefid);
                    intent.putExtra("widgetname", ((Row) ((RecyclerAdapter) view.getAdapter()).list.get(i)).header);
                }

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

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);


        final RequestListener<Response> requestListener = new RequestListener<Response>() {

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


                for (Response.GetStore.Widgets.Widget widget : list) {

                    if (widget.type.equals("apps_list")) {

                        if ("apps_list:EDITORS_group_hrand".equals(widget.widgetid)) {

                            ArrayList<Response.ListApps.Apk> inElements = new ArrayList<Response.ListApps.Apk>(dataset.get(widget.data.ref_id).data.list);

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
                                    for (int j = 0; j < 3 && !inElements.isEmpty(); j++) {
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

                string.clear();
                string.addAll(map);

                Log.d("AptoideDebug", string.toString());
                int offset = ((RecyclerAdapter) view.getAdapter()).offset;
                ((RecyclerAdapter) view.getAdapter()).offset = offset + list.size();
                view.getAdapter().notifyDataSetChanged();
                loading = false;

                GetAdsRequest request = new GetAdsRequest(getActivity());

                request.setLimit(3);
                request.setLocation("homepage");
                request.setKeyword("__NULL__");

                ListApksInstallsRequest listRelatedApkRequest = new ListApksInstallsRequest();

                listRelatedApkRequest.setLimit("4");

                if (AptoideUtils.isLoggedIn(getActivity())) {

                    manager.execute(listRelatedApkRequest, "MoreFriendsInstalls", DurationInMillis.ONE_DAY, new RequestListener<TimelineListAPKsJson>() {
                        @Override
                        public void onRequestFailure(SpiceException spiceException) {

                        }

                        @Override
                        public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {


                            TimelineRow row = new TimelineRow(timelineListAPKsJson.getUsersapks());
                            int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("timeline");
                            row.header = "Your friends installs";
                            row.widgetid = "timeline";

                            ((RecyclerAdapter) view.getAdapter()).list.add(location, row);


                            (view.getAdapter()).notifyDataSetChanged();

                        }
                    });

                    final ListUserbasedApkRequest recommendedRequest = new ListUserbasedApkRequest(getActivity());

                    recommendedRequest.setLimit(3);

                    manager.execute(recommendedRequest, new RequestListener<ListRecomended>() {
                        @Override
                        public void onRequestFailure(SpiceException spiceException) {

                        }

                        @Override
                        public void onRequestSuccess(ListRecomended listRecomended) {
                            Row row = new Row();
                            int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("xml_recommended");

                            for (ListRecomended.Repository apkSuggestion : listRecomended.getRepository()) {

                                for (ListRecomended.Repository.Package apkRecommended : apkSuggestion.getPackage()) {

                                    row.header = "Recommended for you";
                                    Response.ListApps.Apk apk = new Response.ListApps.Apk();
                                    row.widgetid = "recommended";

                                    apk.name = apkRecommended.getName();
                                    apk.icon = apkSuggestion.getIconspath() + apkRecommended.getIcon_hd();
                                    apk.downloads = apkRecommended.getDwn();
                                    apk.md5sum = apkRecommended.getMd5h();

                                    row.addItem(apk);
                                }
                            }

                            ((RecyclerAdapter) view.getAdapter()).list.add(location, row);


                            (view.getAdapter()).notifyDataSetChanged();
                        }
                    });


                }

                rootView.findViewById(R.id.please_wait).setVisibility(View.GONE);
                rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                manager.execute(request, ((GetStartActivityCallback)getActivity()).getSponsoredCache() + 3, DurationInMillis.ALWAYS_RETURNED,  new RequestListener<ApkSuggestionJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {

                    }

                    @Override
                    public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {
                        if (apkSuggestionJson != null && apkSuggestionJson.getAds() != null && apkSuggestionJson.getAds().size() > 0) {

                            AdRow row = new AdRow();

                            row.header = "Highlighted";
                            row.widgetid = "highlighted";

                            row.ads.addAll(apkSuggestionJson.getAds());

                            int location = ((RecyclerAdapter) view.getAdapter()).getPlaceholders().get("ads_list");

                            ((RecyclerAdapter) view.getAdapter()).list.add(location, row);

                            (view.getAdapter()).notifyDataSetChanged();
                        }


                    }
                });

                swipeLayout.setRefreshing(false);


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
        final TestRequest request = new TestRequest();


        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                manager.execute(request, "home", DurationInMillis.ALWAYS_EXPIRED, requestListener);
            }
        });


        manager.execute(request, "home", DurationInMillis.ONE_WEEK, requestListener);

        rootView.findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.list).setVisibility(View.GONE);

        return rootView;
    }

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerAdapter.HeaderViewHolder> {

        public int offset = 0;




        private final List<Displayable> list;
        private final HashMap<String, Integer> placeholders = new HashMap<String, Integer>();

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
            if(viewType>3000) {

                LinearLayout inflate = new LinearLayout(context);
                inflate.setOrientation(LinearLayout.HORIZONTAL);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                inflate.setLayoutParams(params);

                return new HomeCategoryViewHolder(inflate, viewType % 3000, context);


            } else if( viewType > 2000){

                View view = LayoutInflater.from(context).inflate(R.layout.row_app_home_featured, parent, false);


                return new FeaturedViewHolder(view, viewType % 2000, context);


            }else {

                LinearLayout inflate = new LinearLayout(context);
                inflate.setOrientation(LinearLayout.HORIZONTAL);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                inflate.setLayoutParams(params);


                if (viewType > 1000) {
                    return new TimelineRowViewHolder(inflate, viewType, context);
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
            return list.get(position).getHeaderId();
        }

        @Override
        public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {

            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_separator, viewGroup, false);

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
                    for(int i = viewType; i < 3; i++){
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

                views = new View[3];
                Context context = itemView.getContext();
                layout = (LinearLayout) itemView;
                for(int i = 0; i < 3; i++){
                    View inflate = LayoutInflater.from(context).inflate(R.layout.timeline_item, layout, false);
                    views[i] = inflate;
                    ItemViewHolder holder = new ItemViewHolder();
                    holder.name = (TextView) inflate.findViewById(R.id.app_name);
                    holder.icon = (ImageView) inflate.findViewById(R.id.app_icon);
                    holder.friend = (TextView) inflate.findViewById(R.id.app_friend);

                    inflate.setTag(holder);
                    layout.addView(inflate);

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
