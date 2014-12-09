package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cm.aptoide.ptdev.adapters.AptoidePagerAdapter;
import cm.aptoide.ptdev.adapters.MenuListAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.DismissRefreshEvent;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.events.SocialTimelineEvent;
import cm.aptoide.ptdev.events.SocialTimelineInitEvent;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.fragments.callbacks.PullToRefreshCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.exceptions.InvalidVersionException;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.pushnotification.PushNotificationReceiver;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.social.WebViewFacebook;
import cm.aptoide.ptdev.social.WebViewTwitter;
import cm.aptoide.ptdev.tutorial.Tutorial;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Base64;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.views.BadgeView;
import cm.aptoide.ptdev.webservices.Api;
import cm.aptoide.ptdev.webservices.OAuth2AuthenticationRequest;
import cm.aptoide.ptdev.webservices.Response;
import cm.aptoide.ptdev.webservices.TimelineCheckRequestSync;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.OAuth;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import cm.aptoide.ptdev.webservices.json.TimelineActivityJson;
import retrofit.http.Body;
import retrofit.http.POST;
import roboguice.util.temp.Ln;

public class Start extends ActionBarActivity implements
        StoresCallback,
        DownloadManagerCallback,
        AddStoreDialog.Callback,
        DownloadInterface,
        MyAppsAddStoreInterface,
        ProgressDialogFragment.OnCancelListener,
        AdultDialog.Callback,
        PullToRefreshCallback,
        GetStartActivityCallback {

    private static final String TAG = "Start";
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();
    private Class settingsClass = Aptoide.getConfiguration().getSettingsActivityClass();

    private static final int Settings_REQ_CODE = 21;
    private static final int WIZARD_REQ_CODE = 50;

    public ParserService service;
    private boolean parserServiceIsBound;
    private ReentrantLock lock = new ReentrantLock();
    private Condition boundCondition = lock.newCondition();
    public ViewPager pager;
    private BadgeView badgeUpdates;
//    private BadgeView badgeNew;
    private HashMap<String, Long> storesIds;
    private int checkServerCacheString;
    private boolean isResumed;
    private boolean matureCheck;

    private boolean rabbitMqConnBound;
    RabbitMqService rabbitMqService;

    private ServiceConnection rabbitMqConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            rabbitMqConnBound = true;

            rabbitMqService = ((RabbitMqService.RabbitMqBinder) binder).getService();
            rabbitMqService.startAmqpService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rabbitMqConnBound = false;


        }
    };
    private String queueName;
    private boolean refresh;
    private String sponsoredCache;
    private boolean timelineRefresh;


    public DownloadService getDownloadService() {
        return downloadService;
    }

    private DownloadService downloadService;

    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();
            BusProvider.getInstance().post(new DownloadServiceConnected());

            if (getIntent().hasExtra("new_updates") && pager != null) {
                pager.setCurrentItem(2);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void setCurrentItem(int page){
        pager.setCurrentItem(page);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            //Log.d("Aptoide-Start", "onServiceConnected");
            parserServiceIsBound = true;

            lock.lock();
            try {
                boundCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            parserServiceIsBound = false;
        }
    };
    private Database database;
    private Context mContext;
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    MenuListAdapter mMenuAdapter;


    private boolean isDisconnect;
    private AccountManager accountManager;



    @Override
    protected void onStop() {
        BusProvider.getInstance().unregister(this);
        super.onStop();

        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parserServiceIsBound) {
            unbindService(conn);
            unbindService(conn2);
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }

        AccountManager.get(mContext).removeOnAccountsUpdatedListener(onAccountsUpdateListener);

        if(isFinishing()) stopService(new Intent(this, RabbitMqService.class));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Log.d("Aptoide-OnClick", "OnClick");
        int i = item.getItemId();


        if (i == R.id.home || i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
//        } else if (i == R.id.menu_settings) {
//            Intent settingsIntent = new Intent(this, settingsClass);
//            startActivityForResult(settingsIntent, 0);
//        } else if (i == R.id.menu_about) {
//            showAbout();

//        } else if ( i == R.id.menu_filter_mature_content){
//
//            if (item.isChecked()) {
//                FlurryAgent.logEvent("Menu_Settings_Clicked_On_Show_Adult_Content");
//                new AdultDialog().show(getSupportFragmentManager(), "adultDialog");
//            } else {
//                matureLock();
//            }

        } else if (i == R.id.menu_SendFeedBack) {
            FlurryAgent.logEvent("Menu_Settings_Clicked_On_Feedback_Button");

            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this, FeedBackActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void onRepoErrorEvent(RepoErrorEvent event) {

        Exception e = event.getE();
        long repoId = event.getRepoId();

        if (e instanceof InvalidVersionException) {
            if (isResumed)
                AptoideDialog.wrongVersionXmlDialog().show(getSupportFragmentManager(), "wrongXmlDialog");
        }

    }

    @Subscribe
    public void onRepoComplete(RepoCompleteEvent event) {
        long repoId = event.getRepoId();
        //Toast.makeText(getApplicationContext(), "Parse " + repoId + " Completed", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
//        menu.findItem(R.id.menu_filter_mature_content).setChecked(!matureCheck);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        final android.app.SearchManager searchManager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    MenuItemCompat.collapseActionView(searchItem);
                    isDisconnect = true;

                    if (Build.VERSION.SDK_INT > 7) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (isDisconnect) {
                                    WebSocketSingleton.getInstance().disconnect();
                                }

                            }
                        }, 10000);
                    }
                }
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDisconnect = false;

                FlurryAgent.logEvent("Clicked_On_Search_Button");

                if (Build.VERSION.SDK_INT > 7) {
                    WebSocketSingleton.getInstance().connect();
                } else {
                    onSearchRequested();
                    MenuItemCompat.collapseActionView(searchItem);
                }
            }
        });


        if (Build.VERSION.SDK_INT > 7) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        return super.onCreateOptionsMenu(menu);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    RequestListener<RepositoryChangeJson> requestListener = new RequestListener<RepositoryChangeJson>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            for (Map.Entry entry : storesIds.entrySet()) {
                database.setFailedRepo((Long) entry.getValue());
                BusProvider.getInstance().post(new RepoErrorEvent(spiceException, (Long) entry.getValue()));
            }
        }

        @Override
        public void onRequestSuccess(final RepositoryChangeJson repositoryChangeJson) {
            try {
                if (repositoryChangeJson == null) {
                    return;
                }
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (RepositoryChangeJson.Listing changes : repositoryChangeJson.listing) {
                            if (Boolean.parseBoolean(changes.getHasupdates())) {
//                                Toast.makeText(Start.this, changes.getRepo() + " has updates.", Toast.LENGTH_SHORT).show();
                                spiceManager.removeDataFromCache(RepositoryChangeJson.class);
                                final Store store = new Store();
                                Cursor c = database.getStore(storesIds.get(changes.getRepo()));
                                if (c.moveToFirst()) {
                                    store.setBaseUrl(c.getString(c.getColumnIndex("url")));
                                    store.setTopTimestamp(c.getLong(c.getColumnIndex("top_timestamp")));
                                    store.setLatestTimestamp(c.getLong(c.getColumnIndex("latest_timestamp")));
                                    store.setDelta(c.getString(c.getColumnIndex("hash")));
                                    store.setId(c.getLong(c.getColumnIndex("id_repo")));

                                    if (c.getString(c.getColumnIndex("username")) != null) {
                                        Login login = new Login();
                                        login.setUsername(c.getString(c.getColumnIndex("username")));
                                        login.setPassword(c.getString(c.getColumnIndex("password")));
                                        store.setLogin(login);
                                    }

                                }

                                c.close();

                                //Log.d("Aptoide-RepositoryChage", "Parsing" + store.getId() + " " + store.getBaseUrl() );
                                service.setShowNotification(!isLoggedin);
                                //service.startParse(database, store, false);
                            }

                        }
                    }
                });
            } catch (RejectedExecutionException ignored) {
            }

        }
    };

    public String getSponsoredCache() {
        return sponsoredCache;
    }

    OnAccountsUpdateListener onAccountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(Account[] accounts) {

            initDrawerHeader();
            BusProvider.getInstance().post(new SocialTimelineInitEvent(true));
            refresh = true;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        mContext = this;
        setContentView(R.layout.activity_main);

        matureCheck = !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);

        pager = (ViewPager) findViewById(R.id.pager);

        pager.setAdapter(getViewPagerAdapter(true));

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);

        badgeUpdates = new BadgeView(mContext, ((LinearLayout) tabStrip.getChildAt(0)).getChildAt(3));
//        badgeNew = new BadgeView(mContext, ((LinearLayout) tabStrip.getChildAt(0)).getChildAt(4));

        Intent i = new Intent(this, ParserService.class);
        final SQLiteDatabase db = Aptoide.getDb();
        database = new Database(db);

        bindService(i, conn, BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);





        if (savedInstanceState == null) {
            sponsoredCache = UUID.randomUUID().toString();

            File sdcard_file = new File(Environment.getExternalStorageDirectory().getPath());
            if (!sdcard_file.exists() || !sdcard_file.canWrite()) {
                getNoSpaceDialog();

            } else {
                StatFs stat = new StatFs(sdcard_file.getPath());
                long blockSize = stat.getBlockSize();
                //long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                //long total = (blockSize * totalBlocks) / 1024 / 1024;
                long avail = (blockSize * availableBlocks) / 1024 / 1024;
                //Log.d("Aptoide", "* * * * * * * * * *");
                //Log.d("Aptoide", "Total: " + total + " Mb");
                //Log.d("Aptoide", "Available: " + avail + " Mb");

                if (avail < 10) {
                    //Log.d("Aptoide", "No space left on SDCARD...");
                    //Log.d("Aptoide", "* * * * * * * * * *");

                    getNoSpaceDialog();
                }
            }


            if (getIntent().hasExtra("newrepo") && getIntent().getFlags() == 12345) {
                ArrayList<String> repos = getIntent().getExtras().getStringArrayList("newrepo");
                for (final String repoUrl : repos) {

                    if (database.existsServer(AptoideUtils.RepoUtils.formatRepoUri(repoUrl))) {
                        Toast.makeText(this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
                    } else if (!getIntent().getBooleanExtra("nodialog", false)) {
                        AptoideDialog.addMyAppStore(repoUrl).show(getSupportFragmentManager(), "addStoreMyApp");
                        pager.setCurrentItem(2);
                    } else {

                        Store store = new Store();

                        store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                        store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                        startParse(store);
                        pager.setCurrentItem(2);
                        FlurryAgent.logEvent("Added_Store_From_My_App_Installation");
                    }

                }
                getIntent().removeExtra("newrepo");
            } else if (getIntent().hasExtra("fromDownloadNotification") && pager != null) {
                getIntent().removeExtra("fromDownloadNotification");
                pager.setCurrentItem(5);
                FlurryAgent.logEvent("Opened_Updates_Notification");
            }else if(getIntent().hasExtra("fromTimeline")){
                timelineRefresh = true;
                pager.setCurrentItem(4);
                getIntent().removeExtra("fromTimeline");
            }
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            queueName = sharedPreferences.getString("queueName", null);


            new AutoUpdate(this).execute();
            executeWizard();
            startPushNotifications();

            {
                try {
                    InputStream is = getAssets().open("actionsOnBoot.properties");
                    Properties properties = new Properties();
                    properties.load(is);
                    Intent intent = null;
                    if (properties.containsKey("downloadId")) {
                        intent = new Intent(this, appViewClass);

                        String id = properties.getProperty("downloadId");
                        long savedId = sharedPreferences.getLong("downloadId", 0);

                        if (Long.valueOf(id) != savedId) {
                            sharedPreferences.edit().putLong("downloadId", Long.valueOf(id)).commit();

                            intent.putExtra("fromApkInstaller", true);
                            intent.putExtra("id", Long.valueOf(id));


                            if (properties.containsKey("cpi_url")) {

                                String cpi = properties.getProperty("cpi_url");

                                try {
                                    cpi = URLDecoder.decode(cpi, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                intent.putExtra("cpi", cpi);
                            }

                            FlurryAgent.logEvent("Started_From_Apkfy");

                        }

                    } else if (properties.containsKey("aptword")) {
                        String param = properties.getProperty("aptword");

                        if (!TextUtils.isEmpty(param)) {

                            param = param.replaceAll("\\*", "_").replaceAll("\\+", "/");

                            String json = new String(Base64.decode(param.getBytes(), 0));

                            Log.d("AptoideAptWord", json);

                            ObjectMapper mapper = new ObjectMapper();

                            ApkSuggestionJson.Ads ad = mapper.readValue(json, ApkSuggestionJson.Ads.class);

                            intent = new Intent(this, appViewClass);
                            long id = ad.getData().getId().longValue();
                            intent.putExtra("id", id);
                            intent.putExtra("packageName", ad.getData().getPackageName());
                            intent.putExtra("repoName", ad.getData().getRepo());
                            intent.putExtra("fromSponsored", true);
                            intent.putExtra("location", "homepage");
                            intent.putExtra("keyword", "__NULL__");
                            intent.putExtra("cpc", ad.getInfo().getCpc_url());
                            intent.putExtra("cpi", ad.getInfo().getCpi_url());
                            intent.putExtra("whereFrom", "sponsored");
                            intent.putExtra("download_from", "sponsored");

                            if (ad.getPartner() != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("partnerType", ad.getPartner().getPartnerInfo().getName());
                                bundle.putString("partnerClickUrl", ad.getPartner().getPartnerData().getClick_url());
                                intent.putExtra("partnerExtra", bundle);
                            }
                        }
                    }

                    startActivityForResult(intent, 50);


                } catch (IOException e) {
                    Log.e("MYTAG", "");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            //loadEditorsAndTopApps();



//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    AptoideUtils.syncInstalledApps(mContext, db);
//                }
//            });
//
//            Cursor c = database.getServers();
//
//            ArrayList<BasicNameValuePair> storesToCheck = new ArrayList<BasicNameValuePair>();
//            storesIds = new HashMap<String, Long>();
//            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//                storesToCheck.add(new BasicNameValuePair(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("hash"))));
//                storesIds.put(c.getString(c.getColumnIndex("name")), c.getLong(c.getColumnIndex("id_repo")));
//            }
//
//            c.close();
//
//
//            StringBuilder repos = new StringBuilder();
//            StringBuilder hashes = new StringBuilder();
//            Iterator<?> it = storesToCheck.iterator();
//            while (it.hasNext()) {
//                BasicNameValuePair next = (BasicNameValuePair) it.next();
//                repos.append(next.getName());
//                hashes.append(next.getValue());
//
//                if (it.hasNext()) {
//                    repos.append(",");
//                    hashes.append(",");
//                }
//            }
//
//            RepositoryChangeRequest request = new RepositoryChangeRequest();
//            request.setRepos(repos.toString());
//            request.setHashes(hashes.toString());
//
//            checkServerCacheString = (repos.toString() + hashes.toString()).hashCode();
//            if (!storesToCheck.isEmpty()) {
//                spiceManager.execute(request, checkServerCacheString, DurationInMillis.ONE_HOUR, requestListener);
//            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            editor.putInt(EnumPreferences.SCREEN_WIDTH.name(), dm.widthPixels);
            editor.putInt(EnumPreferences.SCREEN_HEIGHT.name(), dm.heightPixels);
            editor.commit();

            updateBadge(PreferenceManager.getDefaultSharedPreferences(this));
//            updateNewFeature(PreferenceManager.getDefaultSharedPreferences(this));
//            updateTimelinePostsBadge(sharedPreferences);
        } else {
            sponsoredCache = savedInstanceState.getString("sponsoredCache");
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mMenuAdapter = new MenuListAdapter(mContext, getDrawerList());

        initDrawerHeader();
        AccountManager.get(mContext).addOnAccountsUpdatedListener(onAccountsUpdateListener, new Handler(Looper.getMainLooper()), false);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);



        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                mDrawerLayout.findViewById(R.id.left_drawer).requestFocus();
                syncState();
            }
        };


        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    public void updateTimelinePostsBadge(SharedPreferences sharedPreferences) {
        if(sharedPreferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)){

            executorService.execute(new Runnable() {


                @Override
                public void run() {
                    try {

                        TimelineActivityJson json = TimelineCheckRequestSync.getRequest("new_installs");

                        int total = 0;

                        total += getFriendsTotal(json.getNew_installs().getTotal());

                        final int finalTotal = total;
                        if (total > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    badgeNew.setText(String.valueOf(finalTotal));
//                                    badgeNew.setTextSize(11);
//                                    badgeNew.setBadgeBackgroundColor(Color.RED);
//                                    badgeNew.show();
                                }
                            });

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public int getFriendsTotal(Number number){
        try{
            return number.intValue();
        } catch (Exception ignored) {}

        return 0;

    }


    private void getNoSpaceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final AlertDialog noSpaceDialog = dialogBuilder.create();
        noSpaceDialog.setIcon(android.R.drawable.ic_dialog_alert);
        noSpaceDialog.setTitle(getText(R.string.remote_in_noSD_title));
        String message;
        if (!Build.DEVICE.equals("alien_jolla_bionic")) {
            message = "" + getText(R.string.remote_in_noSDspace);
        } else {
            message = "" + getText(R.string.remote_in_noSDspace);
        }
        noSpaceDialog.setMessage(message);
        FlurryAgent.logEvent("Dont_Have_Enough_Space_On_SDCARD");
        noSpaceDialog.setButton(Dialog.BUTTON_NEUTRAL, getText(android.R.string.ok), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        noSpaceDialog.show();
    }

    private void loadEditorsAndTopApps() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitForServiceToBeBound();
                        String countryCode = Geolocation.getCountryCode(Start.this);
                        String url = Aptoide.getConfiguration().getEditorsUrl();

                        loadEditorsChoice(url, countryCode);
                        loadTopApps(Aptoide.getConfiguration().getTopAppsUrl());
                        //Log.d( "pullToRefresh", "execute(=)" );
                    } catch (Exception e) {
                        e.printStackTrace();
                        BusProvider.getInstance().post(new DismissRefreshEvent());
                    }
                }
            });
        }
    }

    public List<Object> getDrawerList() {

        List<Object> mItems = new ArrayList<Object>();
        int[] attrs = new int[]{
                R.attr.icMyAccountDrawable /* index 0 */,
                R.attr.icRollbackDrawable /* index 1 */,
                R.attr.icScheduledDrawable /* index 2 */,
                R.attr.icExcludedUpdatesDrawable /* index 3 */,
                R.attr.icSettingsDrawable /* index 4 */
        };

        TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);

        int myAccountRes = typedArray.getResourceId(0, R.drawable.ic_action_accounts_dark);
        mItems.add(new MenuListAdapter.Item(getString(R.string.my_account), myAccountRes, 0));

        int rollbackRes = typedArray.getResourceId(1, R.drawable.ic_action_time_dark);
        mItems.add(new MenuListAdapter.Item(getString(R.string.rollback), rollbackRes, 1));

        int scheduleRes = typedArray.getResourceId(2, R.drawable.ic_schedule);
        mItems.add(new MenuListAdapter.Item(getString(R.string.setting_schdwntitle), scheduleRes, 2));

        int excludedUpdatesRes = typedArray.getResourceId(3, R.drawable.ic_action_cancel_dark);
        mItems.add(new MenuListAdapter.Item(getString(R.string.excluded_updates), excludedUpdatesRes, 3));

//        int timelineRes = typedArray.getResourceId(4, R.drawable.ic_action_timeline_dark);
//        MenuListAdapter.Item socialTimelineItem = new MenuListAdapter.Item(getString(R.string.social_timeline), timelineRes, 8);
//        if( !Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false) ) {
//            socialTimelineItem.setNew(true);
//        }else{
//            socialTimelineItem.setNew(false);
//        }
//        mItems.add(socialTimelineItem);

        int settingsRes = typedArray.getResourceId(4, R.drawable.ic_action_settings_dark);
        mItems.add(new MenuListAdapter.Item(getString(R.string.settings), settingsRes, 7));

        mItems.add(new MenuListAdapter.Category(getString(R.string.social_networks)));
        mItems.add(new MenuListAdapter.Item(getString(R.string.facebook), R.drawable.ic_action_facebook, 4));
        mItems.add(new MenuListAdapter.Item(getString(R.string.twitter), R.drawable.ic_action_twitter, 5));

        mItems.add(new MenuListAdapter.Category(getString(R.string.other)));
        mItems.add(new MenuListAdapter.Item(getString(R.string.backup_apps), R.drawable.ic_action_backup_custom, 6));


        typedArray.recycle();
        return mItems;
    }

    public void loadTopApps(String url) throws IOException {

        //Log.d("Aptoide-Featured", "Loading " + url);

        service.parseTopApps(database, url);
    }

    public void loadEditorsChoice(String url, String countryCode) throws IOException {

        if (countryCode.length() > 0) {
            url = url + "?country=" + countryCode;
        }

        //Log.d("Aptoide-Featured", "Loading " + url);

        service.parseEditorsChoice(database, url);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("newrepo")) {

            ArrayList<String> repos = intent.getExtras().getStringArrayList("newrepo");
            for (final String repoUrl : repos) {

                if (database.existsServer(AptoideUtils.RepoUtils.formatRepoUri(repoUrl))) {
                    Toast.makeText(this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
                } else if (!intent.getBooleanExtra("nodialog", false)) {
                    AptoideDialog.addMyAppStore(repoUrl).show(getSupportFragmentManager(), "addStoreMyApp");
                    pager.setCurrentItem(2);
                } else {

                    Store store = new Store();
                    store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                    store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                    startParse(store);
                    pager.setCurrentItem(2);

                }
            }
        }else if (intent.hasExtra("new_updates") && pager != null) {
            pager.setCurrentItem(3);
        }else if(intent.hasExtra("fromDownloadNotification") && pager != null){
            pager.setCurrentItem(5);
        }else if(intent.hasExtra("fromTimeline") && pager != null){
            timelineRefresh = true;
            pager.setCurrentItem(4);
        }


    }


    public SocialTimelineEvent produceTimelineEvent(){
        Log.d("ProducingAptoideEvent", "" + timelineRefresh);
        SocialTimelineEvent socialTimelineEvent = new SocialTimelineEvent(timelineRefresh);
        timelineRefresh = false;
        return socialTimelineEvent;
    }



    public void executeWizard() {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (sPref.getBoolean("firstrun", true)) {


            Intent newToAptoideTutorial = new Intent(mContext, Tutorial.class);
            startActivityForResult(newToAptoideTutorial, WIZARD_REQ_CODE);
            sPref.edit().putBoolean("firstrun", false).commit();
            try {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        } else {

            try {

                if (Aptoide.isUpdate()) {

                    int previousVersion = PreferenceManager.getDefaultSharedPreferences(this).getInt("version", 0);

                    if (previousVersion < 444) {
                        Intent whatsNewTutorial = new Intent(mContext, Tutorial.class);
                        whatsNewTutorial.putExtra("isUpdate", true);
                        startActivityForResult(whatsNewTutorial, WIZARD_REQ_CODE);
                    }

                    if (previousVersion > 430 && previousVersion < 438) {
                        updateAccount();
                    }

                    PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();

                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public void startPushNotifications() {
        Intent i = new Intent(this, Aptoide.getConfiguration().getNotificationsReceiver());
        i.setAction(Aptoide.getConfiguration().getAction());
        boolean alarmUp = (PendingIntent.getBroadcast(this, 982764, i, PendingIntent.FLAG_NO_CREATE) != null);


        if(!alarmUp){

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(this, 982764, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, PushNotificationReceiver.PUSH_NOTIFICATION_TIME_INTERVAL, pi);

        }
    }

    private void updateAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final AccountManager manager = AccountManager.get(this);
        final Account[] accountsByType = manager.getAccountsByType(Aptoide.getConfiguration().getAccountType());

        if (accountsByType.length > 0 || "APTOIDE".equals(sharedPreferences.getString("loginType", null))) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();
                    oAuth2AuthenticationRequest.setMode(LoginActivity.Mode.APTOIDE);
                    oAuth2AuthenticationRequest.setUsername(accountsByType[0].name);
                    oAuth2AuthenticationRequest.setPassword(manager.getPassword(accountsByType[0]));

                    try {
                        //oAuth2AuthenticationRequest.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());

                        OAuth oAuth = oAuth2AuthenticationRequest.loadDataFromNetwork();

                        String refreshToken = oAuth.getRefreshToken();

                        String actualToken = manager.blockingGetAuthToken(accountsByType[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
                        manager.invalidateAuthToken(Aptoide.getConfiguration().getAccountType(), actualToken);
                        manager.setAuthToken(accountsByType[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, refreshToken);
                        SecurePreferences.getInstance().edit().putString("access_token", oAuth.getAccess_token()).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AccountManager.get(Start.this).removeAccount(accountsByType[0], null, null);
                    }

                }
            }).start();


        } else if (accountsByType.length > 0) {
            AccountManager.get(this).removeAccount(accountsByType[0], null, null);
        }

    }

    public void updateBadge(SharedPreferences sPref) {
        badgeUpdates.setTextSize(11);
        int size = sPref.getInt("updates", 0);
        if (size != 0) {
            badgeUpdates.setText(String.valueOf(size));
            if (!badgeUpdates.isShown()) badgeUpdates.show(true);
        } else {
            if (badgeUpdates.isShown()) badgeUpdates.hide(true);
        }
    }

    public void updateTimelineBadge() {
//        badgeNew.hide();
    }

    public void updateNewFeature(SharedPreferences sPref) {
//        badgeNew.setText(getString(R.string.new_feature));
//        badgeNew.setTextSize(10);
//        badgeNew.setBadgeMargin(5);
//        badgeNew.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//        badgeNew.setBadgeBackgroundColor(Color.parseColor("#A4C639"));
//        if (!sPref.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {
//            badgeNew.show(false);
//        } else {
//            badgeNew.hide(false);
//        }
    }


    public SocialTimelineInitEvent produceInitEvent(){
        return new SocialTimelineInitEvent(refresh);
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 20:
                Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_LONG).show();
                break;
            case Settings_REQ_CODE:
                if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true))
                    matureUnlock();
                else
                    matureLock();

                refresh = true;

                BusProvider.getInstance().post(new RepoCompleteEvent(0));
                BusProvider.getInstance().post(new RepoCompleteEvent(-1));
                BusProvider.getInstance().post(new RepoCompleteEvent(-2));

                break;
            case 50:
                spiceManager.addListenerIfPending(RepositoryChangeJson.class, checkServerCacheString, requestListener);
                spiceManager.getFromCache(RepositoryChangeJson.class, checkServerCacheString, DurationInMillis.ONE_DAY, requestListener);
                if (resultCode == RESULT_OK && data.getBooleanExtra("addDefaultRepo", false)) {
                    Store store = new Store();
                    String repoUrl = "http://apps.store.aptoide.com/";
                    store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                    store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                    startParse(store);
                    //Log.d("Start-addDefaultRepo", "added default repo "+ repoUrl);
                }
                break;
        }
        //invalidateAptoideMenu();
    }


    protected void waitForServiceToBeBound() throws InterruptedException {
        lock.lock();
        try {
            while (service == null) {
                boundCondition.await();
            }
            Ln.d("Bound ok.");
        } finally {
            lock.unlock();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        if (!spiceManager.isStarted()) {
            spiceManager.start(this);
        }

        Ln.getConfig().setLoggingLevel(Log.ERROR);
        BusProvider.getInstance().register(this);
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }


    @Override
    public void showAddStoreDialog() {
        DialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");
    }

    public final class CheckStoreListener implements RequestListener<Response.GetStore> {


        private final Login login;

        public CheckStoreListener(Login login) {
            this.login = login;

        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
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
    public void startParse(final Store store) {
        TestServerRequest request = new TestServerRequest();
        CheckStoreListener checkStoreListener = new CheckStoreListener(null);


        request.setStore_name(store.getName());

        spiceManager.execute(request, checkStoreListener);

//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    waitForServiceToBeBound();
//                    service.setShowNotification(!isLoggedin);
//                    service.startParse(database, store, true);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }


    public void updateAll(List<Long> ids) {
        Toast.makeText(this, getString(R.string.starting_download), Toast.LENGTH_LONG).show();
        for (long id : ids) {
            downloadService.startDownloadFromAppId(id);
        }
    }

    public void updateAll(long[] ids) {
        Toast.makeText(this, getString(R.string.starting_download), Toast.LENGTH_LONG).show();
        for (long id : ids) {
            downloadService.startDownloadFromAppId(id);
        }
    }


    @Override
    public void reloadStores(Set<Long> checkedItems) {


//        for (final Long storeid : checkedItems) {
//
//            Runnable runnable = new Runnable() {
//                public void run() {
//                    final Database db = new Database(Aptoide.getDb());
//
//                    final Store store = new Store();
//                    //Log.d("Aptoide-Reloader", "Reloading storeid " + storeid);
//                    Cursor c = db.getStore(storeid);
//
//                    if (c.moveToFirst()) {
//                        store.setBaseUrl(c.getString(c.getColumnIndex("url")));
//                        store.setTopTimestamp(c.getLong(c.getColumnIndex("top_timestamp")));
//                        store.setLatestTimestamp(c.getLong(c.getColumnIndex("latest_timestamp")));
//                        store.setDelta(c.getString(c.getColumnIndex("hash")));
//                        store.setId(c.getLong(c.getColumnIndex("id_repo")));
//                        if (c.getString(c.getColumnIndex("username")) != null) {
//                            Login login = new Login();
//                            login.setUsername(c.getString(c.getColumnIndex("username")));
//                            login.setPassword(c.getString(c.getColumnIndex("password")));
//                            store.setLogin(login);
//                        }
//
//                    }
//                    service.setShowNotification(!isLoggedin);
//                    service.startParse(db, store, false);
//                }
//            };
//            executorService.submit(runnable);
//        }


    }

    @Override
    public boolean isRefreshing(long id) {
        return service.repoIsParsing(id);
    }

    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
        Toast.makeText(this, getString(R.string.starting_download), Toast.LENGTH_LONG).show();
    }

    public void installAppFromManager(long id) {
        downloadService.startExistingDownload(id);
    }


    @Override
    public DialogInterface.OnClickListener getOnMyAppAddStoreListener(final String repoUrl) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Store store = new Store();
                store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                startParse(store);
            }
        };
    }

    @Override
    public void onCancel() {

        AddStoreDialog fragment = (AddStoreDialog) getSupportFragmentManager().findFragmentByTag("addStoreDialog");

        if (fragment != null) {
            fragment.cancelListener.onCancel();
        }

    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public void matureUnlock() {
        //Log.d("Mature","Unlocked");
        matureCheck = true;
        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", false).commit();
        FlurryAgent.logEvent("Unlocked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));
    }

    public void matureLock() {
        //Log.d("Mature","locked");
        matureCheck = false;
        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", true).commit();
        FlurryAgent.logEvent("Locked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));
    }

    @Override
    public void timelineCallback() {
        refresh = false;
    }

    public PagerAdapter getViewPagerAdapter(boolean b) {
        return new AptoidePagerAdapter(getSupportFragmentManager(), mContext, b);
    }

    @Override
    public void reload() {
        //Log.d( "pullToRefresh", "reload()" );
        loadEditorsAndTopApps();
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            FlurryAgent.logEvent("Opened_Drawer");

            int switchId = (int) id;

            switch (switchId) {
                case 0:
                    //Log.d("MenuDrawer-position", "pos: " + position);
                    Intent loginIntent = new Intent(mContext, MyAccountActivity.class);
                    startActivity(loginIntent);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_My_Account_Drawer_Button");
                    break;
                case 1:
                    //Log.d("MenuDrawer-position", "pos: " + position);
                    Intent rollbackIntent = new Intent(mContext, RollbackActivity.class);
                    startActivity(rollbackIntent);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Rollback_Drawer_Button");
                    break;
                case 2:
                    //Log.d("MenuDrawer-position", "pos: "+position);
                    Intent scheduledIntent = new Intent(mContext, ScheduledDownloadsActivity.class);
                    startActivity(scheduledIntent);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Scheduled_Downloads_Drawer_Button");
                    break;
                case 3:
                    //Log.d("MenuDrawer-position", "pos: "+position);
                    Intent excludedIntent = new Intent(mContext, ExcludedUpdatesActivity.class);
                    startActivity(excludedIntent);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Excluded_Updates_Drawer_Button");
                    break;
                case 4:
                    //Log.d("MenuDrawer-position", "pos: " + position);
//                    Log.d("MenuDrawer-position", "pos: " + position);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Facebook_Drawer_Button");
                    showFacebook();
                    break;
                case 5:
                    //Log.d("MenuDrawer-position", "pos: " + position);
//                    Log.d("MenuDrawer-position", "pos: " + position);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Twitter_Drawer_Button");
                    showTwitter();
                    break;
                case 6:
                    initBackupApps();
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_BackupApps_Drawer_Button");
                    break;
                case 7:
                    Intent settingsIntent = new Intent(mContext, settingsClass);
                    startActivityForResult(settingsIntent, Settings_REQ_CODE);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Clicked_On_Settings_Drawer_Button");
                    break;
//                case 8:
//                    Intent timelineIntent = new Intent(mContext, TimelineActivity.class);
//                    startActivity(timelineIntent);
//                    FlurryAgent.logEvent("Clicked_On_Timeline_Drawer_Button");
//                    break;
                default:
                    break;
            }

            mDrawerLayout.closeDrawer(mDrawerList);

        }

    }

    private void initBackupApps() {

        try {
            getPackageManager().getPackageInfo("pt.aptoide.backupapps", 0);
            Intent intent = getPackageManager().getLaunchIntentForPackage("pt.aptoide.backupapps");

            if (intent != null) {
                startActivity(intent);
                FlurryAgent.logEvent("Launched_BackupApps");
            }


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Intent i = new Intent(this, AppViewActivity.class);
            i.putExtra("getBackupApps", true);
            startActivity(i);
            FlurryAgent.logEvent("Opened_App_View_To_Download_BackupApps");
        }
    }

    boolean isLoggedin = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("queueName", queueName);
        outState.putString("sponsoredCache", sponsoredCache);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        queueName = savedInstanceState.getString("queueName");
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (queueName != null) {
            bindService(new Intent(this, RabbitMqService.class), rabbitMqConn, Context.BIND_AUTO_CREATE);
        }

        isResumed = true;

        if(refresh){
            BusProvider.getInstance().post(new RepoCompleteEvent(0));
            BusProvider.getInstance().post(new RepoCompleteEvent(-1));
            BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        }

    }

    private void initDrawerHeader() {
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemsCanFocus(true);
        TextView login_email;
        ImageView user_avatar;
        accountManager = AccountManager.get(this);

        //Log.d("Aptoide-DrawerHeader", String.valueOf(mDrawerList.getHeaderViewsCount()));

        if (mDrawerList.getHeaderViewsCount() > 0) {
            View v = (mDrawerList.getAdapter()).getView(0, null, null);
            mDrawerList.removeHeaderView(v);
        }

        mDrawerList.setAdapter(null);

        //Login Header
        if (AptoideUtils.isLoggedIn(this)) {
            isLoggedin = true;

            View header = LayoutInflater.from(mContext).inflate(R.layout.header_logged_in, null);
            mDrawerList.addHeaderView(header, null, false);

            login_email = (TextView) header.findViewById(R.id.login_email);

            String login_email_text = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", null);

            if (login_email_text == null || login_email_text.equals("NOT_SIGNED_UP")) {
                login_email_text = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0].name;
            }

            login_email.setText(login_email_text);

            user_avatar = (ImageView) header.findViewById(R.id.user_avatar);
            String avatarUrl = PreferenceManager.getDefaultSharedPreferences(mContext).getString("useravatar", null);

            if (avatarUrl != null) {
                ImageLoader.getInstance().displayImage(avatarUrl, user_avatar);
            }

        } else {
            isLoggedin = false;
        }
        mDrawerList.setAdapter(mMenuAdapter);

        if(refresh){
            BusProvider.getInstance().post(new RepoCompleteEvent(0));
            BusProvider.getInstance().post(new RepoCompleteEvent(-1));
            BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
        //Toast.makeText(this, "OnPause", Toast.LENGTH_LONG).show();

        if (rabbitMqConnBound) {
            rabbitMqService.stopAmqpService();
            unbindService(rabbitMqConn);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    private void showFacebook() {
        if (AptoideUtils.isAppInstalled(mContext, "com.facebook.katana")) {
            Intent sharingIntent;
            try {
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                sharingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/225295240870860"));
                startActivity(sharingIntent);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (ActivityNotFoundException notFound) {
                Toast.makeText(mContext, getString(R.string.not_found), Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(mContext, WebViewFacebook.class);
            startActivity(intent);
            FlurryAgent.logEvent("Opened_Facebook_Webview");
        }
    }

    private void showTwitter() {
        if (AptoideUtils.isAppInstalled(mContext, "com.twitter.android")) {
            String url = "http://www.twitter.com/aptoide";
            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(twitterIntent);
            FlurryAgent.logEvent("Opened_Twitter_App");
        } else {
            Intent intent = new Intent(mContext, WebViewTwitter.class);
            startActivity(intent);
            FlurryAgent.logEvent("Opened_Twitter_Webview");
        }
    }

/*
    private void showAbout() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_about, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext).setView(view);
        final AlertDialog aboutDialog = alertDialogBuilder.create();
        aboutDialog.setTitle(getString(R.string.about_us));
        aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
        aboutDialog.setCancelable(false);
        aboutDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        aboutDialog.show();
    }*/
}
