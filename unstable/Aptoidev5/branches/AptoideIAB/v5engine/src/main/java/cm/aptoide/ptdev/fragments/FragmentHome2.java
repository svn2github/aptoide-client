package cm.aptoide.ptdev.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreFeaturedGraphicActivity;
import cm.aptoide.ptdev.MoreUserBasedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;

import cm.aptoide.ptdev.adapters.Adapter;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.adapters.HomeTopAdapter;
import cm.aptoide.ptdev.adapters.SectionAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.DismissRefreshEvent;
import cm.aptoide.ptdev.fragments.callbacks.PullToRefreshCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.json.ListRecomended;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.AbcDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.AbsListViewDelegate;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome2 extends ListFragment implements LoaderManager.LoaderCallbacks<HashMap<String, ArrayList<Home>>>, OnRefreshListener {


    private SectionAdapter<HomeItem> adapter;
    private ArrayList<Home> editorsChoice = new ArrayList<Home>();
    private ArrayList<HomeItem> top = new ArrayList<HomeItem>();


    private ArrayList<HomeItem> recommended = new ArrayList<HomeItem>();


    private Adapter homeBucketAdapterHome;
    private HomeTopAdapter topAdapter;
    private HomeBucketAdapter recomendedAdapter;
    private View v2;
    private TextView moreReTv;
    private ArrayList<HomeItem> featuredGraphicItems =  new ArrayList<HomeItem>();;
    private MergeAdapter mergeAdapter;
    private int bucketSize;
    private View moreRecommended;
    private PullToRefreshCallback pullToRefreshCallback;
    private PullToRefreshLayout mPullToRefreshLayout;
    private View featGraphFooter;

    @Override
    public void onRefreshStarted( View view ) {
        Log.d( "pullToRefresh", "onRefreshStarted" );

        if(pullToRefreshCallback != null) {
            pullToRefreshCallback.reload();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(50, null, this);
        //getLoaderManager().restartLoader(52, null, featuredGraphicLoader);


        refreshRecommendedList();
        if(!isNetworkAvailable(Aptoide.getContext())){
            setListShown(true);
            setEmptyText(getString(R.string.connection_error));
        }

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

        if (mPullToRefreshLayout!=null) mPullToRefreshLayout.setRefreshComplete();

    }



    private void refreshEditorsList() {
        editorsChoice.clear();

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

    //TopFeaturedLoader loader = new TopFeaturedLoader();
    //FeaturedGraphicLoader featuredGraphicLoader = new FeaturedGraphicLoader();

    BaseAdapter featurededchoice;



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    ArrayList<String> objects = new ArrayList<String>();
    ViewPager pager;

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

        //getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
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

        if ( getActivity() != null ) {
            mPullToRefreshLayout = new PullToRefreshLayout( viewGroup.getContext() );

            ActionBarPullToRefresh.from(getActivity())
                    .insertLayoutInto( viewGroup )
                    .useViewDelegate( ListView.class, new AbsListViewDelegate())
                    .theseChildrenArePullable( getListView().getId())
                    .listener( FragmentHome2.this )
                    .options( Options.create().headerTransformer( new AbcDefaultHeaderTransformer() ).scrollDistance( 0.5f ).build() )
                    .setup( mPullToRefreshLayout );
        }

    }

    private void loadRecommended(final View v2) {
        final AccountManager accountManager = AccountManager.get(getActivity());

        if(accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length>0){
            final SpiceManager manager = ((Start) getActivity()).getSpiceManager();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ListUserbasedApkRequest request = new ListUserbasedApkRequest(getActivity());
                    Account account = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
                    String token = null;
                    try {
                        token = AccountManager.get(getActivity()).blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,false);
                        request.setLimit(recomendedAdapter.getBucketSize()*2);
                        request.setToken(token);
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
            recomendedAdapter.notifyDataSetChanged();

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

        mergeAdapter.addAdapter(homeBucketAdapterHome);

        mergeAdapter.addView(v2);
        mergeAdapter.addAdapter(recomendedAdapter);
        mergeAdapter.addView(moreRecommended);



        mergeAdapter.setActive(moreRecommended, false);
        mergeAdapter.setActive(v, false);
        mergeAdapter.setActive(v2, false);
        mergeAdapter.setActive(featGraphFooter, false);

    }


    @Override
    public Loader<HashMap<String, ArrayList<Home>>> onCreateLoader(final int id, final Bundle args) {

        setListShown(false);
        AsyncTaskLoader<HashMap<String, ArrayList<Home>>> asyncTaskLoader = new AsyncTaskLoader<HashMap<String, ArrayList<Home>>>(getActivity()) {
            @Override
            public HashMap<String, ArrayList<Home>> loadInBackground() {
                return new Database(Aptoide.getDb()).getFeatured2(bucketSize);
            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
                    Intent i = new Intent(getActivity(), appViewClass);
                    long id = ((HomeItem)item).getId();
                    i.putExtra("id", id);
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

        if(mPullToRefreshLayout!=null) mPullToRefreshLayout.setRefreshComplete();


    }

    @Subscribe
    public void onDismissEvent(DismissRefreshEvent event){
        if(mPullToRefreshLayout!=null) mPullToRefreshLayout.setRefreshComplete();
    }

    @Override
    public void onLoaderReset(Loader<HashMap<String, ArrayList<Home>>> loader) {
       //if(editorsChoice != null) editorsChoice.clear();
       // if(adapter != null) homeBucketAdapterHome.notifyDataSetChanged();
    }
}
