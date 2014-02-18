package cm.aptoide.ptdev;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.util.ArrayList;

/**
 * Created by rmateus on 06-02-2014.
 */
public class MoreTopAppsActivity extends ActionBarActivity implements DownloadInterface {

    private DownloadService downloadService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);

        Fragment fragment = new MoreTopAppsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.more_top_apps));
        bindService(new Intent(this, DownloadManager.class), conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
    }


    public static class MoreTopAppsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>> {

        ArrayList<HomeItem> items = new ArrayList<HomeItem>();

        HomeBucketAdapter adapter;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new HomeBucketAdapter(getActivity(), items);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getLoaderManager().initLoader(0, null, this);

            getListView().setPadding(0, AptoideUtils.getPixels(getActivity(), 10), 0, 0);
            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
        }


        @Override
        public Loader<ArrayList<HomeItem>> onCreateLoader(int i, Bundle bundle) {

            AsyncTaskLoader<ArrayList<HomeItem>> taskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getActivity()) {
                @Override
                public ArrayList<HomeItem> loadInBackground() {
                    return new Database(Aptoide.getDb()).getTopFeatured(Integer.MAX_VALUE);
                }
            };

            taskLoader.forceLoad();

            return taskLoader;

        }

        @Override
        public void onLoadFinished(Loader<ArrayList<HomeItem>> arrayListLoader, ArrayList<HomeItem> homeItems) {
            items.addAll(homeItems);
            setListAdapter(adapter);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<HomeItem>> arrayListLoader) {
            setListAdapter(null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
