package cm.aptoide.ptdev.fragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.InstalledAdapter;
import cm.aptoide.ptdev.adapters.UpdatesAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentUpdates extends ListFragment {

    private InstalledAdapter installedAdapter;
    private UpdatesAdapter updatesAdapter;
    private Database db;
    private RecentlyUpdated recentUpdates;

    private MergeAdapter adapter;
    private int counter;


    @Subscribe
    public void newAppEvent(InstalledApkEvent event){
        refreshStoresEvent(null);
    }

    @Subscribe
    public void removedAppEvent(UnInstalledApkEvent event){
        refreshStoresEvent(null);
    }

    @Subscribe
    public void refreshStoresEvent(RepoCompleteEvent event){

        Log.d("Aptoide-", "OnEvent");
        getLoaderManager().restartLoader(91, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        counter++;
                        return db.getUpdates();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                if(data.getCount()>1){
                    updatesAdapter.swapCursor(data);
                }else{
                    updatesAdapter.swapCursor(null);
                }
                if (getListView().getAdapter() == null)
                    setListAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });


        getLoaderManager().restartLoader(90, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        counter++;
                        return db.getInstalled();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                if(data.getCount()>1){
                    installedAdapter.swapCursor(data);
                }else{
                    installedAdapter.swapCursor(null);
                }
                if (getListView().getAdapter() == null)
                    setListAdapter(adapter);

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MergeAdapter();

        this.db = new Database(Aptoide.getDb());

        updatesAdapter = new UpdatesAdapter(getActivity());
        adapter.addAdapter(updatesAdapter);

        recentUpdates = new RecentlyUpdated(getActivity());
        adapter.addAdapter(recentUpdates);

        installedAdapter = new InstalledAdapter(getActivity());
        adapter.addAdapter(installedAdapter);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_rollback, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_rollback){
            Intent i = new Intent(getActivity(), RollbackActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(id>0){
            Intent i = new Intent(getActivity(), AppViewActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        }

  }

   

    @Override
    public void onResume() {
        super.onResume();
        refreshStoresEvent(null);
        BusProvider.getInstance().register(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        getLoaderManager().initLoader(91, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        counter++;
                        return db.getUpdates();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if(data.getCount()>1){
                    updatesAdapter.swapCursor(data);
                }else{
                    updatesAdapter.swapCursor(null);
                }


                if (getListView().getAdapter() == null){
                    setListAdapter(adapter);
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                updatesAdapter.swapCursor(null);
            }
        });





        getLoaderManager().initLoader(90, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        counter++;
                        return db.getInstalled();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if(data.getCount()>1){
                    installedAdapter.swapCursor(data);
                }else{
                    installedAdapter.swapCursor(null);
                }

                counter --;
                if (getListView().getAdapter() == null)
                    setListAdapter(adapter);

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                installedAdapter.swapCursor(null);
            }
        });


    }


}
