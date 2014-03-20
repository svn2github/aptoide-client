package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBreadCrumbs;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.FragmentStore;
import cm.aptoide.ptdev.fragments.FragmentStoreGridCategories;
import cm.aptoide.ptdev.fragments.FragmentStoreHeader;
import cm.aptoide.ptdev.fragments.FragmentStoreListCategories;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.ParserService;
import com.squareup.otto.Subscribe;

import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */

public class StoreActivity extends ActionBarActivity implements CategoryCallback {




    private long storeid;
    private ParserService service;
    private boolean serviceIsBound;
    private boolean isRefreshing;
    private DownloadService downloadService;
    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder)binder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private EnumStoreTheme storeTheme;

    public EnumStoreTheme getStoreTheme() {
        return storeTheme;
    }

    public enum Sort{ 	NAME, DOWNLOADS, DATE, PRICE, RATING}
    public boolean categories;
    public Sort sort;



    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ParserService.MainServiceBinder) binder).getService();
            isRefreshing = service.repoIsParsing(storeid);

            final FragmentStore fragmentStoreListCategories = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
            if(isRefreshing){
                if (fragmentStoreListCategories != null) fragmentStoreListCategories.setRefreshing(isRefreshing);
            }

            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceIsBound = false;
        }
    };

    public boolean isRefreshing() {
        return isRefreshing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this, ParserService.class);
        setContentView(R.layout.page_store_list);

        sort = Sort.values()[PreferenceManager.getDefaultSharedPreferences(this).getInt("order_list", 0)];
        //storeName = getIntent().getStringExtra("storename");
        storeid = getIntent().getLongExtra("storeid", 0);
        categories = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("orderByCategory", true);
        //themeordinal = getIntent().getIntExtra("theme", 0);
        //storeAvatarUrl = getIntent().getStringExtra("storeavatarurl");
        FragmentBreadCrumbs breadCrumbs = (FragmentBreadCrumbs) findViewById(R.id.breadcrumbs);

        breadCrumbs.setActivity(this);
        breadCrumbs.setTitle("Home", null);

        if (savedInstanceState == null) {
            setFragment();
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        bindService(i, conn, BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);

    }

    private void setFragment() {

        Fragment fragment;


        if(!getIntent().getBooleanExtra("list", true)){
            fragment = new FragmentStoreListCategories();
        }else{
            fragment = new FragmentStoreGridCategories();
        }


        Fragment fragmentHeader = new FragmentStoreHeader();

        Log.d("Aptoide-", "StoreActivity id" + storeid);


        Bundle args = new Bundle();
        args.putLong("storeid", storeid);

        fragment.setArguments(args);
        fragmentHeader.setArguments(args);

        getSupportFragmentManager().beginTransaction().add(R.id.content_layout, fragment, "fragStore").commit();

        if(storeid!=-1){
            getSupportFragmentManager().beginTransaction().add(R.id.store_header_layout, fragmentHeader, "fragStoreHeader").commit();
        }else{
            categories = true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceIsBound) unbindService(conn);
        if(downloadService!=null)unbindService(conn2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_categories, menu);

        switch(sort){
            case NAME:
                menu.findItem(R.id.name).setChecked(true);
                break;
            case DOWNLOADS:
                menu.findItem(R.id.download).setChecked(true);
                break;
            case DATE:
                menu.findItem(R.id.date).setChecked(true);
                break;
            case PRICE:
                menu.findItem(R.id.price).setChecked(true);
                break;
            case RATING:
                menu.findItem(R.id.rating).setChecked(true);
                break;
        }

        if(!categories){
            menu.findItem(R.id.show_all).setChecked(true);
        }

        menu.findItem(R.id.show_all).setVisible(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("mergeStores", false));

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        } else if( i == R.id.refresh_store){
            refreshList();
        }
        else if( i == R.id.name){
            sort = Sort.NAME;
            setSort(item);
        } else if( i == R.id.date){
            sort = Sort.DATE;
            setSort(item);
        }else if( i == R.id.download){
            sort = Sort.DOWNLOADS;
            setSort(item);
        }else if( i == R.id.rating){
            sort = Sort.RATING;
            setSort(item);
        }else if( i == R.id.price){
            sort = Sort.PRICE;
            setSort(item);

        }else if( i == R.id.show_all){

            categories = !categories;
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            setSort(item);

        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("orderByCategory", categories).putInt("order_list", sort.ordinal()).commit();

        return true;
    }

    private void setSort(MenuItem item) {
        supportInvalidateOptionsMenu();
        refreshList();
    }

    public static class SortObject {

        public SortObject(Sort sort, boolean noCategories) {
            this.sort = sort;
            this.noCategories = noCategories;
        }

        public Sort getSort() {
            return sort;
        }

        public boolean isNoCategories() {
            return noCategories;
        }

        Sort sort;
        boolean noCategories;
    }

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event) {
        if (event.getRepoId() == storeid) {
            refreshList();
        }
    }

    @Subscribe
    public void onStoreError(RepoErrorEvent event) {
        if (event.getRepoId() == storeid) {
            FragmentStore fragStoreHeader = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStoreHeader");
            fragStoreHeader.onError();
            FragmentStore fragStore = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
            fragStore.onError();
            fragStore.onRefresh();
            fragStore.setRefreshing(service.repoIsParsing(storeid));
        }
    }

    public SortObject getSort(){
        return new SortObject(sort, !categories);
    }




    private void refreshList() {
        if(service!=null){
            isRefreshing = service.repoIsParsing(storeid);
            FragmentStore fragStore = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
            fragStore.onRefresh();
            fragStore.setListShown(false);
            fragStore.setRefreshing(isRefreshing);
        }
    }

    public void onRefreshStarted() {


        if(!isRefreshing){


            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    final Database db = new Database(Aptoide.getDb());
                    final Store store = new Store();

                    Cursor c = db.getStore(storeid);

                    if(c.moveToFirst()){
                        store.setBaseUrl(c.getString(c.getColumnIndex("url")));
                        store.setTopTimestamp(c.getLong(c.getColumnIndex("top_timestamp")));
                        store.setLatestTimestamp(c.getLong(c.getColumnIndex("latest_timestamp")));
                        store.setDelta(c.getString(c.getColumnIndex("hash")));
                        store.setId(c.getLong(c.getColumnIndex("id_repo")));
                        if(c.getString(c.getColumnIndex("username"))!=null){
                            Login login = new Login();
                            login.setUsername(c.getString(c.getColumnIndex("username")));
                            login.setPassword(c.getString(c.getColumnIndex("password")));
                            store.setLogin(login);
                        }

                    }
                    c.close();
                    service.startParse(db, store, false);
                }
            });

        }

    }

    public void installApp(long id){
        downloadService.startDownloadFromAppId(id);
    }
}
