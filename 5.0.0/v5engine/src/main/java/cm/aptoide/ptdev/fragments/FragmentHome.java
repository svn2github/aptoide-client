package cm.aptoide.ptdev.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.adapters.HomeLayoutAdapter;
import cm.aptoide.ptdev.adapters.PrincipalLayoutAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Collection;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.json.ListRecomended;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<Collection>> {


    private MergeAdapter adapter;
    private ArrayList<Collection> editorsChoice = new ArrayList<Collection>();
    private List<HomeItem> top = new ArrayList<HomeItem>();
    private List<HomeItem> recommended = new ArrayList<HomeItem>();


    private PrincipalLayoutAdapter homeBucketAdapter;
    private HomeBucketAdapter topAdapter;
    private HomeBucketAdapter recomendedAdapter;
    private View v2;
    private TextView moreReTv;


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(50, null, this);
        getLoaderManager().restartLoader(51, null, loader);
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
        if(adapter != null) adapter.notifyDataSetChanged();
       getLoaderManager().restartLoader(50, null, this);
    }

    private void refreshTopList() {
        top.clear();
        if(adapter != null) adapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(51, null, loader);
    }

    private void refreshRecommendedList() {

        loadRecommended(v2);
    }

    TopFeaturedLoader loader = new TopFeaturedLoader();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MergeAdapter();

        homeBucketAdapter = new PrincipalLayoutAdapter(getActivity(), editorsChoice, true);
        topAdapter = new HomeBucketAdapter(getActivity(), top);
        recomendedAdapter = new HomeBucketAdapter(getActivity(), recommended);
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


        adapter.addAdapter(homeBucketAdapter);
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

        adapter.addView(v);
        adapter.addAdapter(topAdapter);
        adapter.addView(moreTop);

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
        adapter.addView(v2);
        adapter.addAdapter(recomendedAdapter);
        adapter.addView(moreRecommended);

        //setListAdapter(adapter);

        //loadRecommended(v2);


        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));


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
                        request.setPackageName(token);
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
                                                HomeItem item = new HomeItem(aPackage.getName(), aPackage.getCatg2(), iconPath + icon, 0, String.valueOf(aPackage.getDwn()), aPackage.getRat().floatValue());
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
            v2.setVisibility(View.GONE);
            moreReTv.setVisibility(View.GONE);
        }
    }


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
                    return new Database(Aptoide.getDb()).getTopFeatured(topAdapter.getBucketSize()*2);
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

    @Override
    public Loader<ArrayList<Collection>> onCreateLoader(final int id, final Bundle args) {



        AsyncTaskLoader<ArrayList<Collection>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<Collection>>(getActivity()) {
            @Override
            public ArrayList<Collection> loadInBackground() {
                return new Database(Aptoide.getDb()).getFeatured(6, homeBucketAdapter.getBucketSize()*2);
            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }
//
    @Override
    public void onLoadFinished(Loader<ArrayList<Collection>> loader, ArrayList<Collection> data) {
        editorsChoice.clear();
        editorsChoice.addAll(data);

        if(getListView().getAdapter()==null){
            if(data.size()>1) setListAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Collection>> loader) {
        if(editorsChoice != null) editorsChoice.clear();
        if(adapter != null) adapter.notifyDataSetChanged();
    }
}
