package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreTopAppsActivity;
import cm.aptoide.ptdev.MoreUserBasedActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.HomeTopAdapter;
import cm.aptoide.ptdev.adapters.SectionAdapter;
import cm.aptoide.ptdev.adapters.Sectionizer;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome2 extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>> {


    private SectionAdapter<HomeItem> adapter;
    private ArrayList<HomeItem> editorsChoice = new ArrayList<HomeItem>();
    private ArrayList<HomeItem> top = new ArrayList<HomeItem>();


    private ArrayList<HomeItem> recommended = new ArrayList<HomeItem>();


    private HomeTopAdapter homeBucketAdapter;
    private HomeTopAdapter topAdapter;
    private HomeTopAdapter recomendedAdapter;
    private View v2;
    private TextView moreReTv;
    private ArrayList<HomeItem> featuredGraphicItems =  new ArrayList<HomeItem>();;
    private MergeAdapter mergeAdapter;





    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(50, null, this);
        getLoaderManager().restartLoader(51, null, loader);
        getLoaderManager().restartLoader(52, null, featuredGraphicLoader);

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
        }else if(event.getRepoId() == -1){
            refreshTopList();
        }
    }



    private void refreshEditorsList() {
       editorsChoice.clear();
       featuredGraphicItems.clear();
       if(adapter != null) adapter.notifyDataSetChanged();
       getLoaderManager().restartLoader(50, null, this);
       getLoaderManager().restartLoader(52, null, featuredGraphicLoader);

    }

    private void refreshTopList() {
        top.clear();
        if(adapter != null) adapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(51, null, loader);
    }

    private void refreshRecommendedList() {
        //loadRecommended(v2);
    }

    TopFeaturedLoader loader = new TopFeaturedLoader();
    FeaturedGraphicLoader featuredGraphicLoader = new FeaturedGraphicLoader();

    BaseAdapter featurededchoice;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    ArrayList<String> objects = new ArrayList<String>();



    ViewPager pager;





    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mergeAdapter = new MergeAdapter();

        homeBucketAdapter = new HomeTopAdapter(getActivity(), editorsChoice, 3);
        topAdapter = new HomeTopAdapter(getActivity(), top, 3);
        recomendedAdapter = new HomeTopAdapter(getActivity(), recommended, 3);
//        View editorsView = View.inflate(getActivity(), R.layout.separator_home_header, null);
//        ((TextView) editorsView.findViewById(R.id.separator_label)).setText(getString(R.string.editors_choice));
//        editorsView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("http://m.aptoide.com/");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//            }
//        });
        //adapter.addView(editorsView);
        //editorsChoiceBucketSize = homeBucketAdapter.getBucketSize();

        //adapter.addAdapter(featurededchoice);




        mergeAdapter.addAdapter(homeBucketAdapter);
        View v = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView) v.findViewById(R.id.separator_label)).setText(getString(R.string.top_apps));
//        v.setClickable(true);

        View moreTop = View.inflate(getActivity(), R.layout.separator_home_footer, null);
        TextView moreTopTv = (TextView) moreTop.findViewById(R.id.more);
        moreTopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MoreTopAppsActivity.class);
                startActivity(i);
            }
        });


        mergeAdapter.addAdapter(topAdapter);


        v2 = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView) v2.findViewById(R.id.separator_label)).setText(getString(R.string.recommended_for_you));
//        v2.setClickable(true);

        View moreRecommended = View.inflate(getActivity(), R.layout.separator_home_footer, null);
        moreReTv = (TextView) moreRecommended.findViewById(R.id.more);
        moreReTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MoreUserBasedActivity.class);
                startActivity(i);
            }
        });


        v2.setVisibility(View.GONE);
        moreReTv.setVisibility(View.GONE);
        mergeAdapter.addAdapter(recomendedAdapter);

        //setListAdapter(adapter);

        //loadRecommended(v2);

        adapter = new SectionAdapter<HomeItem>(getActivity(), mergeAdapter, R.layout.separator_home_header, R.id.separator_label, new Sectionizer<HomeItem>() {
            @Override
            public String getSectionTitleForItem(HomeItem instance) {
                return instance.getCategory();
            }
        });

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
    }

//    private void loadRecommended(final View v2) {
//        final AccountManager accountManager = AccountManager.get(getActivity());
//
//        if(accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length>0){
//            final SpiceManager manager = ((Start) getActivity()).getSpiceManager();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final ListUserbasedApkRequest request = new ListUserbasedApkRequest(getActivity());
//                    Account account = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
//                    String token = null;
//                    try {
//                        token = AccountManager.get(getActivity()).blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,false);
//
//                        request.setLimit(recomendedAdapter.getBucketSize()*2);
//                        request.setPackageName(token);
//                    } catch (OperationCanceledException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (AuthenticatorException e) {
//                        e.printStackTrace();
//                    }
//
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    final String finalToken = token + recomendedAdapter.getBucketSize()*2;
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(finalToken !=null){
//                            manager.execute(request, finalToken, DurationInMillis.ONE_DAY, new RequestListener<ListRecomended>() {
//                                @Override
//                                public void onRequestFailure(SpiceException e) {
//
//                                }
//
//                                @Override
//                                public void onRequestSuccess(ListRecomended listRecomended) {
//                                    if (listRecomended != null && listRecomended.getRepository() != null && listRecomended.getRepository().size() > 0) {
//                                        v2.setVisibility(View.VISIBLE);
//                                        moreReTv.setVisibility(View.VISIBLE);
//                                        recommended.clear();
//                                        final boolean matureCheck = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);
//                                        for (ListRecomended.Repository repository : listRecomended.getRepository()) {
//
//                                            String repoName = repository.getName();
//                                            String iconPath = repository.getIconspath();
//                                            for (ListRecomended.Repository.Package aPackage : repository.getPackage()) {
//
//                                                String icon;
//
//                                                if (aPackage.getIcon_hd() != null) {
//                                                    icon = aPackage.getIcon_hd();
//                                                } else {
//                                                    icon = aPackage.getIcon();
//                                                }
//                                                HomeItem item = new HomeItem(aPackage.getName(), aPackage.getCatg2(), iconPath + icon, 0, String.valueOf(aPackage.getDwn()), aPackage.getRat().floatValue(), aPackage.getCatg2());
//                                                item.setRecommended(true);
//                                                item.setRepoName(repoName);
//                                                item.setMd5(aPackage.getMd5h());
//
//                                                if (matureCheck) {
//                                                    if (!aPackage.getAge().equals("Mature")) {
//                                                        recommended.add(item);
//                                                    }
//                                                } else {
//                                                    recommended.add(item);
//                                                }
//
//                                            }
//
//                                        }
//                                    }
//                                    recomendedAdapter.notifyDataSetChanged();
//                                }
//                            });
//                            }
//                        }
//                    });
//                }
//            }).start();
//
//
//
//
//
//
//        }else{
//            recommended.clear();
//            recomendedAdapter.notifyDataSetChanged();
//            v2.setVisibility(View.GONE);
//            moreReTv.setVisibility(View.GONE);
//        }
//    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("Aptoide-Home", "clicked");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public class TopFeaturedLoader implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>>{

        @Override
        public Loader<ArrayList<HomeItem>> onCreateLoader(final int id, final Bundle args) {



            AsyncTaskLoader<ArrayList<HomeItem>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getActivity()) {
                @Override
                public ArrayList<HomeItem> loadInBackground() {
                    return new Database(Aptoide.getDb()).getTopFeatured(3*2);
                }
            };


            asyncTaskLoader.forceLoad();

            return asyncTaskLoader;
        }
        //
        @Override
        public void onLoadFinished(Loader<ArrayList<HomeItem>> loader, ArrayList<HomeItem> data) {
            top.clear();
            top.addAll(data);

            if(getListView().getAdapter()==null){
                if(!data.isEmpty()){
                    setListAdapter(adapter);
                }
            }else{
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {
            if(top!=null) top.clear();
            if(adapter!=null) adapter.notifyDataSetChanged();
        }
    }

    public class FeaturedGraphicLoader implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>>{

        @Override
        public Loader<ArrayList<HomeItem>> onCreateLoader(final int id, final Bundle args) {



            AsyncTaskLoader<ArrayList<HomeItem>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getActivity()) {
                @Override
                public ArrayList<HomeItem> loadInBackground() {
                    return new Database(Aptoide.getDb()).getTopFeatured(3*2);
                }
            };


            asyncTaskLoader.forceLoad();

            return asyncTaskLoader;
        }
        //
        @Override
        public void onLoadFinished(Loader<ArrayList<HomeItem>> loader, ArrayList<HomeItem> data) {
            featuredGraphicItems.clear();
            featuredGraphicItems.addAll(data);

            if(getListView().getAdapter()==null){
                if(!data.isEmpty()){
                    setListAdapter(adapter);
                }
            }else{
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {
            if(top!=null) top.clear();
            if(adapter!=null) adapter.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<ArrayList<HomeItem>> onCreateLoader(final int id, final Bundle args) {


        AsyncTaskLoader<ArrayList<HomeItem>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getActivity()) {
            @Override
            public ArrayList<HomeItem> loadInBackground() {
                return null;
            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }
//
    @Override
    public void onLoadFinished(Loader<ArrayList<HomeItem>> loader, ArrayList<HomeItem> data) {
        editorsChoice.clear();
        editorsChoice.addAll(data);

        if(getListView().getAdapter()==null){
            if(data.size()>1) setListAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {
        if(editorsChoice != null) editorsChoice.clear();
        if(adapter != null) adapter.notifyDataSetChanged();
    }
}
