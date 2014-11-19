package com.aptoide.partners;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.MenuListAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.FragmentDownloadManager;
import cm.aptoide.ptdev.fragments.FragmentSocialTimeline;
import cm.aptoide.ptdev.fragments.FragmentStore;
import cm.aptoide.ptdev.fragments.FragmentStores;
import cm.aptoide.ptdev.fragments.FragmentUpdates;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created by tdeus on 3/19/14.
 */
public class StartPartner extends cm.aptoide.ptdev.Start implements CategoryCallback {

    long storeid = -200;
    private boolean isRefreshing;
    private StoreActivity.Sort sort;
    private boolean categories;
    private static boolean startPartner = true;


    @Override
    public void loadTopApps(String url) throws IOException {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();

        GenericUrl genericUrl = new GenericUrl(url);

        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);

        int code;
        try{
            code = request.execute().getStatusCode();
        }catch (HttpResponseException e){
            code = e.getStatusCode();
        }

        if (code != 200) {
            url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultTopAppsUrl();
        }

        super.loadTopApps(url);
    }

    @Override
    public void loadEditorsChoice(String url, String countryCode) throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);

        int code;

        try{
            code = request.execute().getStatusCode();
        }catch (HttpResponseException e){
            code = e.getStatusCode();
        }

        if (code != 200) {
            url = ((AptoideConfigurationPartners)Aptoide.getConfiguration()).getFallbackEditorsChoiceUrl();
            genericUrl = new GenericUrl(url);
            request = transport.createRequestFactory().buildHeadRequest(genericUrl);
            try{
                code = request.execute().getStatusCode();
            }catch (HttpResponseException e){
                code = e.getStatusCode();
            }

            if(code!=200){
                url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultEditorsUrl();
            }
        }

       super.loadEditorsChoice(url, countryCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(AptoideConfigurationPartners.getRestrictionlist() != null) {
            StringTokenizer tokenizer = new StringTokenizer(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getRestrictionlist(), ",");

            startPartner = false;
            while (tokenizer.hasMoreElements() && !startPartner) {
                String token = tokenizer.nextToken().trim();
                if (Build.MODEL.equals(token)) {
                    startPartner = true;
                    Log.d("Restriction List", "Device model " + token + " in restriction list, you're allowed to continue");
                }
            }
        }

        sort = StoreActivity.Sort.values()[PreferenceManager.getDefaultSharedPreferences(this).getInt("order_list", 0)];
        categories = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("orderByCategory", true);

        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!startPartner) {
            Toast toast = Toast.makeText(this, getString(cm.aptoide.ptdev.R.string.device_not_allowed), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        if(fragmentStore!=null && pager.getCurrentItem()==1){
            if(fragmentStore.getChildFragmentManager().getBackStackEntryCount()>0){
                fragmentStore.getChildFragmentManager().popBackStack();
            }else{
                super.onBackPressed();
            }
        }else{
            super.onBackPressed();
        }


    }



    @Override
    public void executeWizard() {
        try {
            if (Aptoide.isUpdate()) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());

                final Database database = new Database(Aptoide.getDb());
                final Store store = new Store();

                String storeName = Aptoide.getConfiguration().getDefaultStore();
                String repoUrl = "http://"+storeName+".store.aptoide.com/";

                AptoideConfigurationPartners config = (AptoideConfigurationPartners) Aptoide.getConfiguration();
                store.setId(storeid);
                store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                store.setDelta(null);
                store.setView(config.getStoreView());
                store.setTheme(config.getStoreTheme());
                store.setAvatar(config.getStoreAvatar());
                store.setItems(config.getStoreItems());
                database.insertStore(store);

                if(!PreferenceManager.getDefaultSharedPreferences(this).contains("version") && ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getCreateShortcut()){
                    new ManagerPreferences(this).createLauncherShortcut(this, R.drawable.ic_launcher);
                }

                sharedPreferences.edit().putBoolean("firstrun", false).commit();

                PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();



            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getShowSplash()) {
            new SplashDialogFragment().show(getSupportFragmentManager(), "splashDialog");
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.partners_menu_main, menu);
//        boolean value = ((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getMatureContentSwitch();
//        menu.findItem(R.id.menu_filter_mature_content).setVisible(value);
        return true;
    }


    @Override
    public PagerAdapter getViewPagerAdapter(boolean timeline){
        boolean showTimeline = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getShowTimeline();
        boolean multistores = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getMultistores();
        return new PartnersPagerAdapter(getSupportFragmentManager(), this, showTimeline, multistores);

    }

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event) {
        if (event.getRepoId() == storeid) {
            refreshList();
        }
    }

    @Subscribe
    public void onStoreError(RepoErrorEvent event) {
//        Toast.makeText(this, "OnStoreError", Toast.LENGTH_LONG).show();
        Log.d("OnStoreError","Error parsing store. Refreshing...");
        if (event.getRepoId() == storeid) {

            if(fragmentStore!=null){
                ((FragmentStore)fragmentStore).onError();
                ((FragmentStore)fragmentStore).onRefreshCalled();
                ((FragmentStore)fragmentStore).setRefreshing(service.repoIsParsing(storeid));
            }

        }
    }

    public void refreshList() {
        if(service!=null){
            isRefreshing = service.repoIsParsing(storeid);
            if(fragmentStore!=null){
                ((FragmentStore)fragmentStore).onRefreshCalled();
                ((FragmentStore)fragmentStore).setListShown(false);
                ((FragmentStore)fragmentStore).setRefreshing(isRefreshing);
            }
        }
    }

    @Override
    public boolean isRefreshing() {


        if(service!=null){
            isRefreshing = service.repoIsParsing(storeid);

            Log.d("Aptoide-", "Is refreshing?" + isRefreshing);

        }

        return isRefreshing;
    }

    @Override
    public StoreActivity.SortObject getSort() {

        return new StoreActivity.SortObject(sort, !categories);
    }


    @Override
    public List<Object> getDrawerList() {

        List<Object> mItems = new ArrayList<Object>();
        int[] attrs = new int[] {
                cm.aptoide.ptdev.R.attr.icMyAccountDrawable /* index 0 */,
                cm.aptoide.ptdev.R.attr.icRollbackDrawable /* index 1 */,
                cm.aptoide.ptdev.R.attr.icScheduledDrawable /* index 2 */,
                cm.aptoide.ptdev.R.attr.icExcludedUpdatesDrawable /* index 3 */,
                cm.aptoide.ptdev.R.attr.icSettingsDrawable /* index 4 */
        };

        TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);

        int myAccountRes = typedArray.getResourceId(0, cm.aptoide.ptdev.R.drawable.ic_action_accounts_dark);
        mItems.add(new MenuListAdapter.Item(getString(cm.aptoide.ptdev.R.string.my_account), myAccountRes, 0));

        int rollbackRes = typedArray.getResourceId(1, cm.aptoide.ptdev.R.drawable.ic_action_time_dark);
        mItems.add(new MenuListAdapter.Item(getString(cm.aptoide.ptdev.R.string.rollback), rollbackRes, 1));

        int scheduleRes = typedArray.getResourceId(2, cm.aptoide.ptdev.R.drawable.ic_schedule);
        mItems.add(new MenuListAdapter.Item(getString(cm.aptoide.ptdev.R.string.setting_schdwntitle), scheduleRes, 2));

        int excludedUpdatesRes = typedArray.getResourceId(3, cm.aptoide.ptdev.R.drawable.ic_action_cancel_dark);
        mItems.add(new MenuListAdapter.Item(getString(cm.aptoide.ptdev.R.string.excluded_updates), excludedUpdatesRes, 3));

        int settingsRes = typedArray.getResourceId(4, cm.aptoide.ptdev.R.drawable.ic_action_settings_dark);
        mItems.add(new MenuListAdapter.Item(getString(cm.aptoide.ptdev.R.string.settings), settingsRes, 7));

        typedArray.recycle();

        return mItems;
    }

    public void setSort(StoreActivity.Sort sort){
        this.sort = sort;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("order_list", sort.ordinal()).commit();
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
    private Fragment fragmentStore;

    public void toggleCategories() {
        categories = !categories;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("orderByCategory", categories).putInt("order_list", sort.ordinal()).commit();
    }

    public class PartnersPagerAdapter extends FragmentStatePagerAdapter {
        private String[] TITLES;
        private boolean showTimeline;
        private boolean multistore;

        public PartnersPagerAdapter(FragmentManager fm, Context context, boolean showTimeline, boolean multistore) {
            super(fm);
            this.showTimeline = showTimeline;
            this.multistore = multistore;

            if(showTimeline){
                TITLES = new String[]{context.getString(cm.aptoide.ptdev.R.string.home), context.getString(cm.aptoide.ptdev.R.string.store), context.getString(cm.aptoide.ptdev.R.string.updates_tab), context.getString(cm.aptoide.ptdev.R.string.social_timeline), context.getString(cm.aptoide.ptdev.R.string.download_manager)};
            }else {
                TITLES = new String[]{context.getString(cm.aptoide.ptdev.R.string.home), context.getString(cm.aptoide.ptdev.R.string.store), context.getString(cm.aptoide.ptdev.R.string.updates_tab), context.getString(cm.aptoide.ptdev.R.string.download_manager)};
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final Object fragment = super.instantiateItem(container, position);
            try {
                final Field saveFragmentStateField = android.support.v4.app.Fragment.class.getDeclaredField("mSavedFragmentState");
                saveFragmentStateField.setAccessible(true);
                final Bundle savedFragmentState = (Bundle) saveFragmentStateField.get(fragment);
                if (savedFragmentState != null) {
                    savedFragmentState.setClassLoader(android.support.v4.app.Fragment.class.getClassLoader());
                }
            } catch (Exception e) {
                Log.w("CustomFragmentStatePagerAdapter", "Could not get mSavedFragmentState field: " + e);
            }
            return fragment;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            if(showTimeline) {

                switch (position) {
                    case 0:
                        return new FragmentHomePartners();
                    case 1:
                        if(multistore){
                            return new FragmentStores();
                        }
                        fragmentStore = new com.aptoide.partners.Fragment();
                        return fragmentStore;
                    case 2:
                        return new FragmentUpdates();
                    case 3:
                        return new FragmentSocialTimeline();
                    case 4:
                        return new FragmentDownloadManager();
                }

            }else{

                switch (position) {
                    case 0:
                        return new FragmentHomePartners();
                    case 1:
                        if(multistore){
                            return new FragmentStores();
                        }
                        fragmentStore = new com.aptoide.partners.Fragment();
                        return fragmentStore;
                    case 2:
                        return new FragmentUpdates();
                    case 3:
                        return new FragmentDownloadManager();
                }
            }

            return null;
        }


    }

//    @Override
//    public void matureLock() {
//        super.matureLock();
//        invalidateAptoideMenu();
//    }
//
//    @Override
//    public void matureUnlock() {
//        super.matureUnlock();
//        invalidateAptoideMenu();
//    }
//
//    private void invalidateAptoideMenu() {
//        if(!ActivityCompat.invalidateOptionsMenu(this)) {
//            supportInvalidateOptionsMenu();
//        }
//    }

    public void updateNewFeature(SharedPreferences sPref) {
        if(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getShowTimeline()){
            super.updateNewFeature(sPref);
        }
    }

    @Override
    public void updateTimelinePostsBadge(SharedPreferences sharedPreferences) {
        if(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getShowTimeline()){
            super.updateTimelinePostsBadge(sharedPreferences);
        }
    }
}
