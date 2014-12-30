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
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import java.util.concurrent.Executors;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.fragments.FragmentListStore;
import cm.aptoide.ptdev.fragments.FragmentStoreHeader;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */

public class StoreActivity extends ActionBarActivity implements CategoryCallback, AdultDialog.Callback {


    private SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

    @Override
    public void matureUnlock() {
        //Log.d("Mature","Unlocked");

        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", false).commit();
        FlurryAgent.logEvent("Unlocked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));

    }

    public void matureLock() {
        //Log.d("Mature","locked");

        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", true).commit();
        FlurryAgent.logEvent("Locked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));

    }

    private long storeid;
    private ParserService service;
    private boolean serviceIsBound;
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

    public enum Sort{
        NAMEAZ("alpha", "asc"),NAMEZA("alpha", "desc"), DOWNLOADS("downloads", "desc"), DATE("latest", "desc"), PRICE("alpha", "asc"), RATING("rating", "desc");
        private final String sort;
        private final String dir;

        Sort(String sort, String dir){

            this.sort = sort;
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }

        public String getSort() {
            return sort;
        }
    }
    public boolean categories;
    public Sort sort;




    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ParserService.MainServiceBinder) binder).getService();
            //isRefreshing = service.repoIsParsing(storeid);

//            final FragmentStore fragmentStoreListCategories = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
  //          if(isRefreshing){
 //               if (fragmentStoreListCategories != null) fragmentStoreListCategories.setRefreshing(isRefreshing);
  //          }

            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceIsBound = false;
        }
    };

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

        Fragment fragment = new FragmentListStore();


        if(!getIntent().getBooleanExtra("list", true)){
            //fragment = new FragmentStoreListCategories();
        }else{
            //fragment = new FragmentStoreGridCategories();
        }


        final Database db = new Database(Aptoide.getDb());

        Cursor c = db.getStore(storeid);

        c.moveToFirst();
        String name = c.getString(c.getColumnIndex("name"));
        String theme = c.getString(c.getColumnIndex("theme"));

        c.close();

        TestServerRequest request = new TestServerRequest();
        CheckStoreListener checkStoreListener = new CheckStoreListener(null);

        request.setStore_name(name);


        manager.execute(request,"getStore" + name,DurationInMillis.ONE_HOUR * 6, checkStoreListener);

        Fragment fragmentHeader = new FragmentStoreHeader();

        //Log.d("Aptoide-", "StoreActivity id" + storeid);

        Bundle args = new Bundle();
        args.putString("storename", name);
        args.putString("theme", theme);
        args.putLong("storeid", storeid);

        if(getIntent().hasExtra("username")){

            args.putString("username", getIntent().getStringExtra("username"));
            args.putString("password", getIntent().getStringExtra("password"));

        }
        //args.putString("widgetrefid", "cat_1");

        fragment.setArguments(args);
        fragmentHeader.setArguments(args);

        getSupportFragmentManager().beginTransaction().add(R.id.content_layout, fragment, "fragStore").commit();

        if(storeid!=-1){
            getSupportFragmentManager().beginTransaction().add(R.id.store_header_layout, fragmentHeader, "fragStoreHeader").commit();
        }else{
            categories = true;
        }

    }

    public static final class CheckStoreListener implements RequestListener<Response.GetStore> {


        private final Login login;

        public CheckStoreListener(Login login) {
            this.login = login;

        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(Response.GetStore response) {

            final Store store = new Store();
            Response.GetStore.StoreMetaData data = response.datasets.meta.data;
            store.setId(data.id.longValue());
            store.setName(response.datasets.meta.data.name);
            store.setDownloads(response.datasets.meta.data.downloads.intValue() + "");


            String sizeString = IconSizes.generateSizeStringAvatar(Aptoide.getContext());


            String avatar = data.avatar;

            if(avatar!=null) {
                String[] splittedUrl = avatar.split("\\.(?=[^\\.]+$)");
                avatar = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
            }


            store.setAvatar(avatar);
            store.setDescription(data.description);
            store.setTheme(data.theme);
            store.setView(data.view);
            store.setBaseUrl(data.name);

            Database database = new Database(Aptoide.getDb());

            try {
                database.insertStore(store);
                database.updateStore(store);

                BusProvider.getInstance().post(new RepoAddedEvent());
            }catch (Exception e){
                e.printStackTrace();
            }


        }



    }


    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
        manager.start(this);
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");

    }

    public interface TestServerWebservice{
        @POST("/ws2.aptoide.com/api/6/getStore")
        Response.GetStore checkServer(@Body Api.GetStore body);
    }

    public static class TestServerRequest extends RetrofitSpiceRequest<Response.GetStore, TestServerWebservice> {


        private String store_name;
        public TestServerRequest() {
            super(Response.GetStore.class, TestServerWebservice.class);
        }

        public void setStore_name(String store_name){

            this.store_name = store_name;
        }

        @Override
        public Response.GetStore loadDataFromNetwork() throws Exception {

            Api.GetStore api = new Api.GetStore();
            api.addDataset("meta");
            api.datasets_params = null;
            api.store_name = store_name;

            return getService().checkServer(api);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.shouldStop();
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
                menu.findItem(R.id.nameAZ).setChecked(true);
                break;
            case RATING:
                menu.findItem(R.id.rating).setChecked(true);
                break;
        }

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



    public SortObject getSort(){
        return new SortObject(sort, !categories);
    }


    private void refreshList() {
        FragmentListStore fragmentById = (FragmentListStore) getSupportFragmentManager().findFragmentById(R.id.content_layout);
        fragmentById.refresh(DurationInMillis.ONE_HOUR);
    }

    public void onRefreshStarted() {
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

    public void installApp(long id){
        downloadService.startDownloadFromAppId(id);
    }
}
