package com.aptoide.partners;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.CategoryCallback;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.MenuListAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.fragments.FragmentDownloadManager;
import cm.aptoide.ptdev.fragments.FragmentListApps;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromId;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by tdeus on 3/19/14.
 */
public class StartPartner extends cm.aptoide.ptdev.Start implements CategoryCallback, AdultDialog.Callback {

    long storeid = -200;
    private boolean isRefreshing;
    private StoreActivity.Sort sort;
    private boolean categories;
    private static boolean startPartner = true;
    private Fragment fragmentStore;


//    @Override
//    public void loadTopApps(String url) throws IOException {
//
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//
//        GenericUrl genericUrl = new GenericUrl(url);
//
//        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);
//
//        int code;
//        try{
//            code = request.execute().getStatusCode();
//        }catch (HttpResponseException e){
//            code = e.getStatusCode();
//        }
//
//        if (code != 200) {
//            url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultTopAppsUrl();
//        }
//
//        super.loadTopApps(url);
//    }
//
//    @Override
//    public void loadEditorsChoice(String url, String countryCode) throws IOException {
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        GenericUrl genericUrl = new GenericUrl(url);
//        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);
//
//        int code;
//
//        try{
//            code = request.execute().getStatusCode();
//        }catch (HttpResponseException e){
//            code = e.getStatusCode();
//        }
//
//        if (code != 200) {
//            url = ((AptoideConfigurationPartners)Aptoide.getConfiguration()).getFallbackEditorsChoiceUrl();
//            genericUrl = new GenericUrl(url);
//            request = transport.createRequestFactory().buildHeadRequest(genericUrl);
//            try{
//                code = request.execute().getStatusCode();
//            }catch (HttpResponseException e){
//                code = e.getStatusCode();
//            }
//
//            if(code!=200){
//                url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultEditorsUrl();
//            }
//        }
//
//       super.loadEditorsChoice(url, countryCode);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

          /** Epiland **/
//        if(AptoideConfigurationPartners.getRestrictionlist() != null) {
//            StringTokenizer tokenizer = new StringTokenizer(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getRestrictionlist(), ",");
//
//            startPartner = false;
//            while (tokenizer.hasMoreElements() && !startPartner) {
//                String token = tokenizer.nextToken().trim();
//                if (Build.MODEL.equals(token)) {
//                    startPartner = true;
//                    Log.d("Restriction List", "Device model " + token + " in restriction list, you're allowed to continue");
//                }
//            }
//        }

        /** Lisciani **/
        try {
                startPartner = !getChildrenPlaying();
                Log.d("StartPartner","Value of children_playing " + getChildrenPlaying());
            
        } catch (IOException e) {
            e.printStackTrace();
	    startPartner = false;
        }


        sort = StoreActivity.Sort.values()[PreferenceManager.getDefaultSharedPreferences(this).getInt("order_list", 0)];
        categories = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("orderByCategory", true);

        super.onCreate(savedInstanceState);


    }

    public boolean getChildrenPlaying() throws IOException {
        Properties prop = new Properties();
        String propFileName = "session.txt";
        File file = new File("/data/lisciani/", propFileName);
        InputStream inputStream = new FileInputStream(file);
//        Log.d("StartPartner","path " + file.getCanonicalPath());

        if (inputStream != null) {
            prop.load(inputStream);
//            Log.d("StartPartner","loading " + propFileName);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        String children_playing = prop.getProperty("running_ma");

        boolean start = "1".equals(children_playing);
        return start;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!startPartner) {
            /** Epiland **/
//            Toast.makeText(this, getString(cm.aptoide.ptdev.R.string.device_not_allowed), Toast.LENGTH_SHORT).show();
//            finish();
            /** Lisciani *
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,(ViewGroup) findViewById(R.id.toast_layout_id));
            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText(getString(cm.aptoide.ptdev.R.string.children_text_alert));
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
            finish();
             */
        }
    }

    @Override
    public void onBackPressed() {

        if(fragmentStore!=null && pager.getCurrentItem() == 2){
            if(fragmentStore.getChildFragmentManager().getBackStackEntryCount()>0){
                fragmentStore.getChildFragmentManager().popBackStack();
            }else{
                super.onBackPressed();
            }
        }else{
            super.onBackPressed();
        }


    }

    public interface GetFirstInstall{
        @POST("/ws2.aptoide.com/api/6/bulkRequest/api_list/getStore,listApps/")
        Response postApk(@Body Api user);
    }

    public static class TestRequest extends RetrofitSpiceRequest<Response, GetFirstInstall> {

        private int offset;
        private String widget;



        public TestRequest() {
            super(Response.class, GetFirstInstall.class);

        }


        public void setOffset(int offset){

            this.offset = offset;
        }

        public void setWidgetId(String widget){

            this.widget = widget;
        }



        @Override
        public Response loadDataFromNetwork() throws Exception {
            Api api = new Api();

            api.getApi_global_params().setLang(AptoideUtils.getMyCountry(Aptoide.getContext()));
            api.getApi_global_params().setStore_name("savou");
            int BUCKET_SIZE = AptoideUtils.getBucketSize();

            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
            api.getApi_global_params().mature = String.valueOf(!sPref.getBoolean("matureChkBox", true));


            Api.GetStore getStore = new Api.GetStore();
            //Api.GetStore.CategoriesParams categoriesParams = new Api.GetStore.CategoriesParams();

            //categoriesParams.setParent_ref_id("cat_2");

            Api.GetStore.WidgetParams widgetParams = new Api.GetStore.WidgetParams();
            widgetParams.setContext("first_install");

            //widgetParams.offset = offset;
            //widgetParams.limit = 3;
            //getStore.getDatasets_params().set(categoriesParams);
            getStore.getDatasets_params().set(widgetParams);

            //getStore.addDataset(categoriesParams.getDatasetName());
            getStore.addDataset(widgetParams.getDatasetName());

            api.getApi_params().set(getStore);


            return getService().postApk(api);

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

                sharedPreferences.edit().putBoolean("firstrun", false).apply();

                sharedPreferences.edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).apply();

                TestRequest request = new TestRequest();

                getSpiceManager().execute(request, "firstInstallRequest", DurationInMillis.ONE_MINUTE, new RequestListener<Response>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {

                    }

                    @Override
                    public void onRequestSuccess(Response response) {


                        try {
                            for (Response.GetStore.Widgets.Widget widget : response.responses.getStore.datasets.widgets.data.list) {

                                if (!response.responses.listApps.datasets.getDataset().get(widget.data.ref_id).data.list.isEmpty()) {
                                    Intent i = new Intent(StartPartner.this, FirstInstallActivity.class);
                                    startActivityForResult(i, 365);

                                    Log.d("Aptoide", "Starting Activity");
                                    break;
                                }

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });


            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getShowSplash()) {
            new SplashDialogFragment().show(getSupportFragmentManager(), "splashDialog");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 365:

                if(resultCode == RESULT_OK){
                    ArrayList<Integer> ids = data.getIntegerArrayListExtra("ids");

                    for (Integer id : ids) {

                        try{
                            GetApkInfoRequestFromId getApkInfoRequestFromId = new GetApkInfoRequestFromId(Aptoide.getContext());
                            getApkInfoRequestFromId.setAppId(String.valueOf(id));
                            getSpiceManager().execute(getApkInfoRequestFromId, new RequestListener<GetApkInfoJson>() {
                                @Override
                                public void onRequestFailure(SpiceException spiceException) {

                                }

                                @Override
                                public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {


                                    try{

                                        Download download = new Download();

                                        int downloadId = getApkInfoJson.getApk().md5sum.hashCode();

                                        download.setId(downloadId);
                                        download.setName(getApkInfoJson.getMeta().getTitle());
                                        download.setVersion(getApkInfoJson.getApk().getVername());
                                        download.setIcon(getApkInfoJson.getApk().getIcon());
                                        download.setPackageName(getApkInfoJson.getApk().getPackage());
                                        download.setMd5(getApkInfoJson.getApk().getMd5sum());

                                        //download.setReferrer(referrer);

                                        getDownloadService().startDownloadFromJson(getApkInfoJson, downloadId , download );

                                    }catch (Exception e){

                                    }


                                }
                            });


                        }catch (RetrofitError error){

                        }


                    }


                }

                break;
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



    public void refreshList() {
        if(service!=null){
            isRefreshing = service.repoIsParsing(storeid);

        }
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

            TITLES = new String[]{context.getString(cm.aptoide.ptdev.R.string.home),getString(R.string.top_tab),  context.getString(cm.aptoide.ptdev.R.string.store), context.getString(cm.aptoide.ptdev.R.string.updates_tab), context.getString(cm.aptoide.ptdev.R.string.download_manager)};

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

            switch (position){
                case 0:
                    return new FragmentListApps();
                case 1:
                    return new FragmentListTopAppsPartners();
                case 2:
                    fragmentStore = new Fragment();
                    return fragmentStore;
                case 3:
                    return new FragmentUpdates2();
                case 4:
                    return new FragmentDownloadManager();
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
