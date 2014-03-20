package com.aptoide.partners;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.AptoidePagerAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.FragmentDownloadManager;
import cm.aptoide.ptdev.fragments.FragmentHome;
import cm.aptoide.ptdev.fragments.FragmentStore;
import cm.aptoide.ptdev.fragments.FragmentUpdates;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;

/**
 * Created by tdeus on 3/19/14.
 */
public class StartPartner extends cm.aptoide.ptdev.Start implements CategoryCallback {

    long storeid = -200;
    private boolean isRefreshing;
    private StoreActivity.Sort sort;
    private boolean categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        if(savedInstanceState==null){
            final Database database = new Database(Aptoide.getDb());
            final Store store = new Store();
            String repoUrl = "http://apps.store.aptoide.com/";
            store.setId(storeid);
            store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
            store.setName(AptoideUtils.RepoUtils.split(repoUrl));
            store.setDelta("empty");
            database.insertStore(store);
        }
        sort = StoreActivity.Sort.values()[PreferenceManager.getDefaultSharedPreferences(this).getInt("order_list", 0)];
        categories = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("orderByCategory", true);
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onBackPressed() {

        if(pager.getCurrentItem()==1){
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
//        Toast.makeText(getApplicationContext(), "SKIP WIZARD", Toast.LENGTH_LONG).show();
    }



    public PagerAdapter getViewPagerAdaFragmentManager(){
        return super.getViewPagerAdapter();
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

            if(fragmentStore!=null){
                ((FragmentStore)fragmentStore).onError();
                ((FragmentStore)fragmentStore).onRefresh();
                ((FragmentStore)fragmentStore).setRefreshing(service.repoIsParsing(storeid));
            }

        }
    }

    public void refreshList() {
        if(service!=null){
            isRefreshing = service.repoIsParsing(storeid);
            if(fragmentStore!=null){
                ((FragmentStore)fragmentStore).onRefresh();
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


    public void setSort(StoreActivity.Sort sort){
        this.sort = sort;
    }

    public void onRefreshStarted() {


        //if(!isRefreshing){


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
    private Fragment fragmentStore;

    public void toggleCategories() {
        categories = !categories;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("orderByCategory", categories).putInt("order_list", sort.ordinal()).commit();
    }

    public class PartnersPagerAdapter extends FragmentStatePagerAdapter {
        private String[] TITLES;


        public PartnersPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            TITLES = new String[] { context.getString(cm.aptoide.ptdev.R.string.home), context.getString(cm.aptoide.ptdev.R.string.stores), context.getString(cm.aptoide.ptdev.R.string.updates_tab), context.getString(cm.aptoide.ptdev.R.string.download_manager)};

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

            switch (position) {
                case 0:
                    return new FragmentHome();
                case 1:
                    fragmentStore = new com.aptoide.partners.Fragment();
                    return fragmentStore;
                case 2:
                    return new FragmentUpdates();
                case 3:
                    return new FragmentDownloadManager();
            }

            return null;
        }


    }

    //}
}
