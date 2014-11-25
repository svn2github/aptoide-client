package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBreadCrumbs;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
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
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.ParserService;

import com.flurry.android.FlurryAgent;
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
    private FragmentBreadCrumbs breadCrumbs;

    public EnumStoreTheme getStoreTheme() {
        return storeTheme;
    }

    public enum Sort{ 	NAMEAZ,NAMEZA, DOWNLOADS, DATE, PRICE, RATING}
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
        storeTheme = EnumStoreTheme.values()[getIntent().getIntExtra("theme", 0)];
        //storeAvatarUrl = getIntent().getStringExtra("storeavatarurl");
        breadCrumbs = (FragmentBreadCrumbs) findViewById(R.id.breadcrumbs);

        breadCrumbs.setActivity(this);
        breadCrumbs.setTitle(getString(R.string.categories), null);

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

        //Log.d("Aptoide-", "StoreActivity id" + storeid);

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
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");

    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
        FlurryAgent.onEndSession(this);
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
            case NAMEAZ:
                menu.findItem(R.id.nameAZ).setChecked(true);
                break;
            case NAMEZA:
                menu.findItem(R.id.nameZA).setChecked(true);
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

        if(categories && storeid != -1){
            breadCrumbs.setTitle(getString(R.string.categories), null);
        }else{
            menu.findItem(R.id.show_all).setChecked(true);
            breadCrumbs.setTitle(getString(R.string.order_popup_catg2), null);
        }

        menu.findItem(R.id.show_all).setVisible(!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("mergeStores", false));

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home || i == R.id.home) {
            finish();
        } else if( i == R.id.refresh_store){
            refreshList();
            FlurryAgent.logEvent("Store_View_Clicked_On_Refresh_Button");
        }else if( i == R.id.menu_SendFeedBack){
            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
            FlurryAgent.logEvent("Store_View_Clicked_On_Feedback_Button");
        }
        else {
            if (i == R.id.nameAZ) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Name_AZ");
                sort = Sort.NAMEAZ;
                setSort(item);
            } else if (i == R.id.nameZA) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Name_ZA");
                sort = Sort.NAMEZA;
                setSort(item);
            } else if (i == R.id.date) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Date");
                sort = Sort.DATE;
                setSort(item);
            } else if (i == R.id.download) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Downloads");
                sort = Sort.DOWNLOADS;
                setSort(item);
            } else if (i == R.id.rating) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Rating");
                sort = Sort.RATING;
                setSort(item);
            } else if (i == R.id.price) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_Price");
                sort = Sort.PRICE;
                setSort(item);
            } else if (i == R.id.show_all) {
                FlurryAgent.logEvent("Store_View_Sorted_Apps_By_All_Applications");
                categories = !categories;
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                setSort(item);
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean("orderByCategory", categories)
                .putInt("order_list", sort.ordinal()).commit();
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
            fragStore.onRefreshCalled();
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
            fragStore.onRefreshCalled();
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
                    //service.startParse(db, store, false);
                }
            });

        }

    }

    public void installApp(long id){
        downloadService.startDownloadFromAppId(id);
    }
}
