package cm.aptoidetv.pt;


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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.DownloadInterface;
import cm.aptoide.ptdev.adapters.Adapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.Home;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created by rmateus on 06-02-2014.
 */
public class MoreTopAppsActivityTV extends ActionBarActivity implements DownloadInterface {

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
        setContentView(cm.aptoide.ptdev.R.layout.page_store);
        bindService(new Intent(this, DownloadService.class), conn, Context.BIND_AUTO_CREATE);

        Fragment fragment = new MoreTopAppsFragment();
        if(savedInstanceState==null)
            getSupportFragmentManager().beginTransaction().replace(cm.aptoide.ptdev.R.id.fragment_container, fragment).commit();

        getSupportActionBar().hide();


    }

    @Override
    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home || i == cm.aptoide.ptdev.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MoreTopAppsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<Home>> {

        AdapterHomeTV adapterHome;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            adapterHome = new AdapterHomeTV(getActivity());
            setListAdapter(adapterHome);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
            getLoaderManager().initLoader(0, null, this);
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, AptoideUtils.getPixels(getActivity(), 10), 0, 0);
            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
            getListView().setItemsCanFocus(true);

        }

        @Override
        public Loader<ArrayList<Home>> onCreateLoader(int i, Bundle bundle) {

            AsyncTaskLoader<ArrayList<Home>> taskLoader = new AsyncTaskLoader<ArrayList<Home>>(getActivity()) {
                @Override
                public ArrayList<Home> loadInBackground() {
                    return new Database(Aptoide.getDb()).getAllTopFeatured(adapterHome.getBucketSize());
                }
            };

            taskLoader.forceLoad();

            return taskLoader;

        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Home>> arrayListLoader, ArrayList<Home> homeItems) {
            adapterHome.setItems(homeItems);
            adapterHome.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Home>> arrayListLoader) {
            setListAdapter(null);
        }
    }

}
