package cm.aptoide.ptdev.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.adapters.HomeLayoutAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Collection;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

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
    
    private HomeLayoutAdapter homeBucketAdapter;
    private HomeBucketAdapter topAdapter;

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(50, null, this);
        getLoaderManager().restartLoader(51, null, loader);
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
       adapter.notifyDataSetChanged();
       getLoaderManager().restartLoader(50, null, this);
    }

    private void refreshTopList() {
        top.clear();
        adapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(51, null, loader);
    }

    TopFeaturedLoader loader = new TopFeaturedLoader();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MergeAdapter();

        homeBucketAdapter = new HomeLayoutAdapter(getActivity(), editorsChoice, true);
        topAdapter = new HomeBucketAdapter(getActivity(), top);
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
        adapter.addView(v);
        adapter.addAdapter(topAdapter);

        getListView().setCacheColorHint(0);

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
                if(!data.isEmpty()) setListAdapter(adapter);
            }else{
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {
            top.clear();
            adapter.notifyDataSetChanged();
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
            if(!data.isEmpty()) setListAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Collection>> loader) {
        editorsChoice.clear();
        adapter.notifyDataSetChanged();
    }
}
