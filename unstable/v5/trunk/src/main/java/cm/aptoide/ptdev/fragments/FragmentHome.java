package cm.aptoide.ptdev.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.HomeBucketAdapter;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import com.actionbarsherlock.app.SherlockListFragment;
import com.commonsware.cwac.merge.MergeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome extends SherlockListFragment implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>> {


    private MergeAdapter adapter;
    private List<HomeItem> editorsChoice = new ArrayList<HomeItem>();
    private List<HomeItem> top = new ArrayList<HomeItem>();
    private int editorsChoiceBucketSize;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MergeAdapter();

        HomeBucketAdapter homeBucketAdapter = new HomeBucketAdapter(getSherlockActivity(), editorsChoice);
        adapter.addView(View.inflate(getSherlockActivity(), R.layout.separator_home_header, null));
        editorsChoiceBucketSize = homeBucketAdapter.getBucketSize();
        adapter.addAdapter(homeBucketAdapter);

        HomeBucketAdapter homeBucketAdapter2 = new HomeBucketAdapter(getSherlockActivity(), top);
        View v = View.inflate(getSherlockActivity(), R.layout.separator_home_header, null);
        ((TextView)v.findViewById(R.id.separator_label)).setText("Top Apps");
        adapter.addView(v);
        adapter.addAdapter(homeBucketAdapter2);
//
//        HomeBucketAdapter homeBucketAdapter3 = new HomeBucketAdapter(getSherlockActivity(), Arrays.asList(new HomeItem[]{new HomeItem(), new HomeItem(), new HomeItem(), new HomeItem()}));
//        adapter.addView(View.inflate(getSherlockActivity(), R.layout.separator_home_header, null));
//        adapter.addAdapter(homeBucketAdapter3);



        getLoaderManager().restartLoader(50, null, this);
        getLoaderManager().restartLoader(51, null, this);





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

    @Override
    public Loader<ArrayList<HomeItem>> onCreateLoader(final int id, final Bundle args) {



        AsyncTaskLoader<ArrayList<HomeItem>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getSherlockActivity()) {
            @Override
            public ArrayList<HomeItem> loadInBackground() {

                switch (id){
                    case 50:
                        return new Database(Aptoide.getDb()).getFeatured(2, editorsChoiceBucketSize, editorsChoice);
                    case 51:
                        return new Database(Aptoide.getDb()).getFeatured(1, editorsChoiceBucketSize, top);
                    default:
                        return new Database(Aptoide.getDb()).getFeatured(1, editorsChoiceBucketSize, editorsChoice);
                }


            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<HomeItem>> loader, ArrayList<HomeItem> data) {



        if(getListView().getAdapter()==null){
            setListAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {

    }
}
