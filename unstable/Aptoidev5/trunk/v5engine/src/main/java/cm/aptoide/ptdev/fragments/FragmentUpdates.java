package cm.aptoide.ptdev.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledApkEvent;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.adapters.InstalledAdapter;
import cm.aptoide.ptdev.adapters.UpdatesAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
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
                updatesAdapter.swapCursor(data);
                if (counter > 0) counter--;
                if (getListView().getAdapter() == null && counter == 0)
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
                installedAdapter.swapCursor(data);
                if (counter > 0) counter--;
                if (getListView().getAdapter() == null && counter == 0)
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
                updatesAdapter.swapCursor(data);
                counter--;
                if (getListView().getAdapter() == null && counter == 0)
                    setListAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

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
                installedAdapter.swapCursor(data);
                counter --;
                if (getListView().getAdapter() == null && counter <= 0)
                    setListAdapter(adapter);

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });


    }


}
