package cm.aptoide.ptdev.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.TextView;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreFeaturedGraphicActivity;
import cm.aptoide.ptdev.MoreUserBasedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.Adapter;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.DismissRefreshEvent;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.fragments.callbacks.PullToRefreshCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.ListRecomended;

import com.commonsware.cwac.merge.MergeAdapter;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome extends ListFragment implements LoaderManager.LoaderCallbacks<HashMap<String, ArrayList<Home>>>, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Home> editorsChoice = new ArrayList<Home>();
    private ArrayList<HomeItem> recommended = new ArrayList<HomeItem>();
    private Adapter homeBucketAdapterHome;

    private HomeBucketAdapter recomendedAdapter;
    private View v2;
    private TextView moreReTv;

    private MergeAdapter mergeAdapter;
    private int bucketSize;
    private View moreRecommended;
    private PullToRefreshCallback pullToRefreshCallback;

    private View featGraphFooter;

    private LinearLayout sponsoredLinearLayout, sponsoredCustomAdsLinearLayout, sponsoredGoogleAdsLinearLayout;
    private View sponsoredHeader;
    private boolean fromRefresh;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View adultSwitchView;
    private CompoundButton adultContent;

/*    private ArrayList<HomeItem> featuredGraphicItems =  new ArrayList<HomeItem>();;
    private boolean onConfigChange;
    private HomeTopAdapter topAdapter;
    private ArrayList<HomeItem> top = new ArrayList<HomeItem>();
    private SectionAdapter adapter;*/

    @Override
    public void onRefresh() {
        Log.d("pullToRefresh", "onRefreshStarted");

        if(pullToRefreshCallback != null) {
            pullToRefreshCallback.reload();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create the list fragment's content view by calling the super method
        final FrameLayout listFragmentView = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
         mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
    }

    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }

    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(50, null, this);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.default_progress_bar_color, R.color.custom_color, R.color.default_progress_bar_color, R.color.custom_color);


        //getLoaderManager().restartLoader(52, null, featuredGraphicLoader);

//        refreshSponsoredAdsList();
        refreshSponsoredList();

        refreshRecommendedList();
        if(!isNetworkAvailable(Aptoide.getContext())){
            setListShown(true);
            setEmptyText(getString(R.string.connection_error));
        }

    }

    private void refreshSponsoredList() {

        SpiceManager manager = ((GetStartActivityCallback)getActivity()).getSpiceManager();

        GetAdsRequest request = new GetAdsRequest(getActivity());

        request.setLimit(recomendedAdapter.getBucketSize());
        request.setLocation("homepage");
        request.setKeyword("__NULL__");
//        Log.d("FragmentHome", "refreshSponsoredList");


        manager.execute(request, ((GetStartActivityCallback)getActivity()).getSponsoredCache() + recomendedAdapter.getBucketSize(), DurationInMillis.ONE_HOUR,new RequestListener<ApkSuggestionJson>() {
            int nativeAd, googlePlayAd, bannerAd;

            @Override
            public void onRequestFailure(SpiceException spiceException) {
//                Log.d("FragmentHome", "onRequestFailure");
                sponsoredLinearLayout.removeAllViews();
                sponsoredCustomAdsLinearLayout.removeAllViews();
                sponsoredGoogleAdsLinearLayout.removeAllViews();
                mergeAdapter.setActive(sponsoredHeader, false);
                mergeAdapter.setActive(sponsoredLinearLayout, false);
                mergeAdapter.setActive(sponsoredCustomAdsLinearLayout, false);
                mergeAdapter.setActive(sponsoredGoogleAdsLinearLayout, false);
            }

            @Override
            public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {
//                Log.d("FragmentHome", "onRequestSuccess");

                if(apkSuggestionJson!=null && apkSuggestionJson.getAds()!=null && apkSuggestionJson.getAds().size()>0) {
//                    Log.d("FragmentHome", "there are ads");

                    try {
                        sponsoredLinearLayout.removeAllViews();
                        sponsoredCustomAdsLinearLayout.removeAllViews();
                        sponsoredGoogleAdsLinearLayout.removeAllViews();

                        for (Object apkSuggestionObject : apkSuggestionJson.getAds()) {

                            final ApkSuggestionJson.Ads apkSuggestion = (ApkSuggestionJson.Ads) apkSuggestionObject;
//                            Log.d("FragmentHome", "ad type " + apkSuggestion.getInfo().getAd_type());

                            if(apkSuggestion.getInfo().getAd_type().equals("app:suggested")){
                                nativeAd++;
//                                Log.d("FragmentHome", "onRequestSuccess; app:suggested");
                                View v = LayoutInflater.from(getActivity()).inflate(R.layout.row_app_home, sponsoredLinearLayout, false);
                                v.findViewById(R.id.ic_action).setVisibility(View.INVISIBLE);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                v.setLayoutParams(params);
                                ImageView icon = (ImageView) v.findViewById(R.id.app_icon);
                                TextView name = (TextView) v.findViewById(R.id.app_name);

                                TextView category = (TextView) v.findViewById(R.id.app_category);
                                ImageLoader.getInstance().displayImage(apkSuggestion.getData().getIcon(), icon);
                                category.setText(R.string.sponsored);
                                name.setText(apkSuggestion.getData().getName());

                                v.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_Sponsored_App");
                                        Intent i = new Intent(getActivity(), appViewClass);
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

                                        startActivity(i);
                                    }
                                });
                                FrameLayout layout = new FrameLayout(getActivity());

                                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                                layout.addView(v);
                                sponsoredLinearLayout.addView(layout);

                                mergeAdapter.setActive(sponsoredHeader, true);
                                mergeAdapter.setActive(sponsoredLinearLayout, true);

                            }else if(apkSuggestion.getInfo().getAd_type().equals("url:googleplay")){
//                                Log.d("FragmentHome", "onRequestSuccess; url:googleplay");
                                View gplayView = LayoutInflater.from(getActivity()).inflate(R.layout.row_app_ad_banner, sponsoredGoogleAdsLinearLayout, false);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                gplayView.setLayoutParams(params);
                                ImageView banner = (ImageView) gplayView.findViewById(R.id.app_ad_banner);
                                ImageLoader.getInstance().displayImage(apkSuggestion.getData().getImage(), banner);


                                gplayView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= 10)
                                            FlurryAgent.logEvent("Home_Page_Clicked_On_Sponsored_Google_Play_Link");
                                        try {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(apkSuggestion.getData().getUrl()));
                                            List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(i, 0);
                                            String activityToOpen = "";
                                            for (ResolveInfo resolveInfo : resolveInfos) {
                                                if (resolveInfo.activityInfo.packageName.equals("com.android.vending")) {
                                                    activityToOpen = resolveInfo.activityInfo.name;
                                                }
                                            }
                                            i.setClassName("com.android.vending", activityToOpen);
                                            startActivity(i);
                                        } catch (ActivityNotFoundException e) {
                                            e.printStackTrace();

                                            Intent i = new Intent(getActivity(), Aptoide.getConfiguration().getSearchActivityClass());
                                            String param = apkSuggestion.getData().getUrl().split("=")[1];
                                            i.putExtra(android.app.SearchManager.QUERY, param);
                                            startActivity(i);
                                        }
                                    }
                                });

                                FrameLayout layout = new FrameLayout(getActivity());

                                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                                layout.addView(gplayView);
                                sponsoredGoogleAdsLinearLayout.setOrientation(LinearLayout.VERTICAL);
                                sponsoredGoogleAdsLinearLayout.addView(layout);

                                mergeAdapter.setActive(sponsoredHeader, true);
                                mergeAdapter.setActive(sponsoredGoogleAdsLinearLayout, true);

                            }else if(apkSuggestion.getInfo().getAd_type().equals("url:banner")){
//                                Log.d("FragmentHome", "onRequestSuccess; url:banner");
                                View customView = LayoutInflater.from(getActivity()).inflate(R.layout.row_app_ad_banner, sponsoredCustomAdsLinearLayout, false);

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                customView.setLayoutParams(params);
                                ImageView banner = (ImageView) customView.findViewById(R.id.app_ad_banner);
                                ImageLoader.getInstance().displayImage(apkSuggestion.getData().getImage(), banner);

                                customView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_Sponsored_Banner_Link");
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkSuggestion.getData().getUrl()));
                                        startActivity(intent);
                                    }
                                });

                                FrameLayout layout = new FrameLayout(getActivity());

                                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                                layout.addView(customView);
                                sponsoredCustomAdsLinearLayout.setOrientation(LinearLayout.VERTICAL);
                                sponsoredCustomAdsLinearLayout.addView(layout);




                                mergeAdapter.setActive(sponsoredHeader, true);
                                mergeAdapter.setActive(sponsoredCustomAdsLinearLayout, true);
                            }

                        }

                        //Fill remaining space
                        for(int i = nativeAd ; i < recomendedAdapter.getBucketSize(); i++){
                            FrameLayout frameLayout = new FrameLayout(getActivity());
                            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                            sponsoredLinearLayout.addView(frameLayout);
                        }

                        //Center 1 Ad
//                        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
//                        if (googlePlayAd == 1) {
//                            gplayView.setLayoutParams(params1);
//                            sponsoredGoogleAdsLinearLayout.addView(gplayView);
//                        }
//                        if (bannerAd == 1) {
//                            customView.setLayoutParams(params1);
//                            sponsoredCustomAdsLinearLayout.addView(customView);
//                        }



                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


            }
        });


    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart() {
        BusProvider.getInstance().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event) {
        Log.d("Aptoide-Home", "OnRefresh");

        if (event.getRepoId() == -2) {
            refreshEditorsList();
        } else if (event.getRepoId() == -1) {
            refreshTopList();
        }

        mSwipeRefreshLayout.setRefreshing(false);
        //if (mPullToRefreshLayout!=null) mPullToRefreshLayout.setRefreshComplete();

    }



    private void refreshEditorsList() {
        editorsChoice.clear();
        fromRefresh = true;
        if(getLoaderManager().getLoader(50)!=null){
            getLoaderManager().destroyLoader(50);
        }

       //featuredGraphicItems.clear();
        //homeBucketAdapter = new TestActivity.Adapter(getActivity(), editorsChoice, 3);
        getLoaderManager().restartLoader(50, null, this);
       //getLoaderManager().restartLoader(52, null, featuredGraphicLoader);

    }

    private void refreshTopList() {
        refreshEditorsList();
        //top.clear();
        //if(adapter != null) adapter.notifyDataSetChanged();
        //getLoaderManager().restartLoader(51, null, loader);
    }

    private void refreshRecommendedList() {
        loadRecommended(v2);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    protected float getScreenWidthInDip(Activity context) {
        WindowManager wm = context.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth_in_pixel = dm.widthPixels;

        return screenWidth_in_pixel / dm.density;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //mergeAdapter = new MergeAdapter();

        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
        getListView().setItemsCanFocus(true);




//        HomeBucketAdapter homeBucketAdapter2 = new HomeBucketAdapter(getActivity(), top);
//        View v = View.inflate(getActivity(), R.layout.separator_home_header, null);
//        ((TextView)v.findViewById(R.id.separator_label)).setText(getString(R.string.top_apps));
//        v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("http://m.aptoide.com/more/topapps");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//            }
//        });
//        adapter.addView(v);
//        adapter.addAdapter(homeBucketAdapter2);
//
//        HomeBucketAdapter homeBucketAdapter3 = new HomeBucketAdapter(getSherlockActivity(), Arrays.asList(new HomeItem[]{new HomeItem(), new HomeItem(), new HomeItem(), new HomeItem()}));
//        adapter.addView(View.inflate(getSherlockActivity(), R.layout.separator_home_header, null));
//        adapter.addAdapter(homeBucketAdapter3);

//getListView().setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_anim));

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        ImageLoader.getInstance().resume();
                        break;
                    default:
                        ImageLoader.getInstance().pause();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }

        });


        ViewGroup viewGroup = (ViewGroup) view;



    }

    private void loadRecommended(final View v2) {
        final AccountManager accountManager = AccountManager.get(getActivity());

        if(accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length>0){
            final SpiceManager manager = ((GetStartActivityCallback) getActivity()).getSpiceManager();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ListUserbasedApkRequest request = new ListUserbasedApkRequest(getActivity());
                    Account account = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
                    String token = null;
                    try {
                        token = AccountManager.get(getActivity()).blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,false);
                        request.setLimit(recomendedAdapter.getBucketSize()*2);
                        //request.setToken(token);
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    final String finalToken = token + recomendedAdapter.getBucketSize()*2;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(finalToken !=null){
                            manager.execute(request, finalToken, DurationInMillis.ONE_DAY, new RequestListener<ListRecomended>() {
                                @Override
                                public void onRequestFailure(SpiceException e) {

                                }

                                @Override
                                public void onRequestSuccess(ListRecomended listRecomended) {
                                    if (listRecomended != null && listRecomended.getRepository() != null && listRecomended.getRepository().size() > 0) {
                                        v2.setVisibility(View.VISIBLE);
                                        moreReTv.setVisibility(View.VISIBLE);
                                        mergeAdapter.setActive(v2, true);
                                        mergeAdapter.setActive(moreRecommended, true);
                                        recommended.clear();
                                        final boolean matureCheck = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);
                                        for (ListRecomended.Repository repository : listRecomended.getRepository()) {

                                            String repoName = repository.getName();
                                            String iconPath = repository.getIconspath();
                                            for (ListRecomended.Repository.Package aPackage : repository.getPackage()) {

                                                String icon;

                                                if (aPackage.getIcon_hd() != null) {
                                                    icon = aPackage.getIcon_hd();
                                                } else {
                                                    icon = aPackage.getIcon();
                                                }
                                                HomeItem item = new HomeItem(aPackage.getName(), aPackage.getCatg2(), iconPath + icon, 0, String.valueOf(aPackage.getDwn()), aPackage.getRat().floatValue(), aPackage.getCatg2());
                                                item.setRecommended(true);
                                                item.setRepoName(repoName);
                                                item.setMd5(aPackage.getMd5h());

                                                if (matureCheck) {
                                                    if (!aPackage.getAge().equals("Mature")) {
                                                        recommended.add(item);
                                                    }
                                                } else {
                                                    recommended.add(item);
                                                }

                                            }

                                        }
                                    }
                                    recomendedAdapter.notifyDataSetChanged();
                                }
                            });
                            }
                        }
                    });
                }
            }).start();

        }else{
            recommended.clear();
            mergeAdapter.setActive(v2, false);
            mergeAdapter.setActive(moreRecommended, false);
            mergeAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("Aptoide-Home", "clicked");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pullToRefreshCallback = null;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        float screenWidth = getScreenWidthInDip(activity);

        bucketSize = (int) (screenWidth / 120);
        homeBucketAdapterHome = new Adapter(getActivity());
        recomendedAdapter = new HomeBucketAdapter(activity, recommended);
        pullToRefreshCallback = (PullToRefreshCallback) activity;

    }

    View v;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        v = getLayoutInflater(null).inflate(R.layout.row_app_home_featured, null);

        v2 = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView) v2.findViewById(R.id.separator_label)).setText(getString(R.string.recommended_for_you));
//        v2.setClickable(true);



        mergeAdapter = new MergeAdapter();


        moreRecommended = View.inflate(getActivity(), R.layout.separator_home_footer, null);
        moreReTv = (TextView) moreRecommended.findViewById(R.id.more);
        moreReTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 10)
                    FlurryAgent.logEvent("Home_Page_Clicked_On_More_Recommended_Button");
                Intent i = new Intent(getActivity(), MoreUserBasedActivity.class);
                startActivity(i);
            }
        });
        v2.setVisibility(View.GONE);
        moreReTv.setVisibility(View.GONE);


        mergeAdapter.addView(v, false);
        v.setVisibility(View.GONE);
        featGraphFooter = getActivity().getLayoutInflater().inflate(R.layout.separator_home_footer, null);
        featGraphFooter.setVisibility(View.GONE);
        featGraphFooter.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MoreFeaturedGraphicActivity.class);
                startActivity(i);
            }
        });
        mergeAdapter.addView(featGraphFooter, false);

        sponsoredLinearLayout = new LinearLayout(getActivity());
        sponsoredLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        sponsoredCustomAdsLinearLayout = new LinearLayout(getActivity());
        sponsoredCustomAdsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        sponsoredGoogleAdsLinearLayout = new LinearLayout(getActivity());
        sponsoredGoogleAdsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        sponsoredHeader = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView)sponsoredHeader.findViewById(R.id.separator_label)).setText(R.string.suggested_apps);

        mergeAdapter.addView(sponsoredHeader);
        mergeAdapter.setActive(sponsoredHeader, false);

        mergeAdapter.addView(sponsoredLinearLayout);
        mergeAdapter.setActive(sponsoredLinearLayout, false);

        mergeAdapter.addView(sponsoredCustomAdsLinearLayout);
        mergeAdapter.setActive(sponsoredCustomAdsLinearLayout, false);

        mergeAdapter.addView(sponsoredGoogleAdsLinearLayout);
        mergeAdapter.setActive(sponsoredGoogleAdsLinearLayout, false);

        mergeAdapter.addAdapter(homeBucketAdapterHome);

        mergeAdapter.addView(v2);
        mergeAdapter.addAdapter(recomendedAdapter);
        mergeAdapter.addView(moreRecommended);



        mergeAdapter.setActive(moreRecommended, false);
        mergeAdapter.setActive(v, false);
        mergeAdapter.setActive(v2, false);
        mergeAdapter.setActive(featGraphFooter, false);

        adultSwitchView = View.inflate(getActivity(), R.layout.widget_switch, null);
        adultContent = (CompoundButton) adultSwitchView.findViewById(R.id.adult_content);
        adultContent.setChecked(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true));
        adultContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Switch_Turned_On_Show_Adult_Content");
                    new AdultDialog().show(getFragmentManager(), "adultDialog");
                } else {
                    ((GetStartActivityCallback) getActivity()).matureLock();
                }
            }
        });

        mergeAdapter.addView(adultSwitchView);
        mergeAdapter.setActive(adultSwitchView, false);



    }


    @Override
    public Loader<HashMap<String, ArrayList<Home>>> onCreateLoader(final int id, final Bundle args) {


        if(fromRefresh){
            setListShown(false);
            fromRefresh = false;
        }


        AsyncTaskLoader<HashMap<String, ArrayList<Home>>> asyncTaskLoader = new AsyncTaskLoader<HashMap<String, ArrayList<Home>>>(getActivity()) {
            @Override
            public HashMap<String, ArrayList<Home>> loadInBackground() {
                return new Database(Aptoide.getDb()).getFeatured2(bucketSize);
            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }

    final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).displayer(new FadeInBitmapDisplayer(1000)).build();

//
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();

    @Override
    public void onLoadFinished(Loader<HashMap<String, ArrayList<Home>>> loader, HashMap<String, ArrayList<Home>> data) {

        loader.reset();

        setListShown(true);

        homeBucketAdapterHome.setItems(data.get("editorsChoice"));
        homeBucketAdapterHome.notifyDataSetChanged();



        ImageView ivCentral = (ImageView) v.findViewById(R.id.app_icon_central);
        ImageView ivRow1Left = (ImageView) v.findViewById(R.id.app_icon_row1_left);
        ImageView ivRow1Right = (ImageView) v.findViewById(R.id.app_icon_row1_right);
        ImageView ivRow2Left = (ImageView) v.findViewById(R.id.app_icon_row2_left);
        ImageView ivRow2Right = (ImageView) v.findViewById(R.id.app_icon_row2_right);

        FrameLayout flCentral = (FrameLayout) v.findViewById(R.id.fl_central);
        FrameLayout flRow1Left = (FrameLayout) v.findViewById(R.id.fl_row1_left);
        FrameLayout flRow1Right = (FrameLayout) v.findViewById(R.id.fl_row1_right);
        FrameLayout flRow2Left = (FrameLayout) v.findViewById(R.id.fl_row2_left);
        FrameLayout flRow2Right = (FrameLayout) v.findViewById(R.id.fl_row2_right);

        final ImageView[] res = new ImageView[]{ivCentral, ivRow1Left, ivRow1Right, ivRow2Left, ivRow2Right};
        final FrameLayout[] fls = new FrameLayout[]{flCentral, flRow1Left, flRow1Right, flRow2Left, flRow2Right};


        ArrayList<Home> items = data.get("featuredGraphic");

        int i =  0;
        for (final Home item : items) {
            ImageLoader.getInstance().displayImage(((HomeItem)item).getIcon(), res[i], options);
            fls[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_Featured_Graphic");
                    Intent i = new Intent(getActivity(), appViewClass);
                    long id = ((HomeItem)item).getId();
                    i.putExtra("id", id);
                    i.putExtra("download_from", "feature_graphic");
                    startActivity(i);
                }
            });
            i++;
        }


        if(!items.isEmpty()){
            v.setVisibility(View.VISIBLE);
            featGraphFooter.setVisibility(View.VISIBLE);
            mergeAdapter.setActive(v, true);
            mergeAdapter.setActive(featGraphFooter, true);
        }




        if(getListView().getAdapter()==null){
            if(data.size()>1) setListAdapter(mergeAdapter);
        }else{
            homeBucketAdapterHome.notifyDataSetChanged();
        }

        mergeAdapter.setActive(adultSwitchView, true);

        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Subscribe
    public void onDismissEvent(DismissRefreshEvent event){
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<HashMap<String, ArrayList<Home>>> loader) {

       //if(editorsChoice != null) editorsChoice.clear();
       // if(adapter != null) homeBucketAdapterHome.notifyDataSetChanged();
    }
}
