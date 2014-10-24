package cm.aptoidetv.pt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.AutoUpdate;
import cm.aptoide.ptdev.DownloadServiceConnected;
import cm.aptoide.ptdev.Geolocation;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.WebSocketSingleton;
import cm.aptoide.ptdev.adapters.AptoidePagerAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.DismissRefreshEvent;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.fragments.callbacks.PullToRefreshCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.views.BadgeView;
import cm.aptoide.ptdev.webservices.OAuth2AuthenticationRequest;
import cm.aptoide.ptdev.webservices.RepositoryChangeRequest;
import cm.aptoide.ptdev.webservices.json.OAuth;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import roboguice.util.temp.Ln;

public class StartTV extends ActionBarActivity implements PullToRefreshCallback, GetStartActivityCallback {

    private Class appViewClass = AptoideTV.getConfiguration().getAppViewActivityClass();
    private Class settingsClass = AptoideTV.getConfiguration().getSettingsActivityClass();

    private static final int Settings_REQ_CODE = 21;
    private static final int WIZARD_REQ_CODE = 50;
    static Toast toast;
    private ArrayList<Server> server;
    public ParserService service;
    private boolean parserServiceIsBound;
    private ReentrantLock lock = new ReentrantLock();
    private Condition boundCondition = lock.newCondition();
    public ViewPager pager;
    private BadgeView badge;
    private RepositoryChangeRequest request;
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
            rabbitMqService = ((RabbitMqService.RabbitMqBinder)binder).getService();
            rabbitMqService.startAmqpService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rabbitMqConnBound = false;

        }
    };
    private String queueName;
    private boolean refresh;

    public DownloadService getDownloadService() {
        return downloadService;
    }

    private DownloadService downloadService;

    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();
            BusProvider.getInstance().post(new DownloadServiceConnected());

//            if (getIntent().hasExtra("new_updates") && pager != null) {
//                pager.setCurrentItem(2);
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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



    private boolean isDisconnect;
    private AccountManager accountManager;

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
        spiceManager.shouldStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parserServiceIsBound) {
            unbindService(conn);
            unbindService(conn2);
        }

        if(executorService!=null){
            executorService.shutdownNow();
        }

        if(isFinishing()) stopService(new Intent(this, RabbitMqService.class));


    }


    ExecutorService executorService = Executors.newFixedThreadPool(2);
    RequestListener<RepositoryChangeJson> requestListener = new RequestListener<RepositoryChangeJson>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            for(Map.Entry entry : storesIds.entrySet()){
                database.setFailedRepo((Long) entry.getValue());
                BusProvider.getInstance().post(new RepoErrorEvent(spiceException, (Long) entry.getValue()));
            }
        }

        @Override
        public void onRequestSuccess(final RepositoryChangeJson repositoryChangeJson) {
            try{
                if(repositoryChangeJson==null){
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
                                service.startParse(database, store, false);
                            }

                        }
                    }
                });
            }catch (RejectedExecutionException e){

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AptoideTV.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);


        mContext = this;
        setContentView(R.layout.activity_main);

        matureCheck = !PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext()).getBoolean("matureChkBox", true);

//        pager = (ViewPager) findViewById(cm.aptoide.ptdev.R.id.pager);
//
//        pager.setAdapter(getViewPagerAdapter());
//
//        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(cm.aptoide.ptdev.R.id.tabs);
//        tabStrip.setViewPager(pager);
//
//        badge = new BadgeView(mContext, ((LinearLayout)tabStrip.getChildAt(0)).getChildAt(2));


        Intent i = new Intent(this, ParserService.class);
        final SQLiteDatabase db = ((AptoideTV) getApplication()).getDb();
        database = new Database(db);

        bindService(i, conn, BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);


//        Handler andler = new Handler();
//
//        andler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                onSearchRequested();
//            }
//        },10000);

        Fragment fragment = new FragmentHomeForTV();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.layout_home_page, fragment).commit();

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



            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            queueName = sharedPreferences.getString("queueName", null);





            new AutoUpdateTV(this).execute();
            executeWizard();


            try {
                InputStream is = getAssets().open("actionsOnBoot.properties");
                Properties properties = new Properties();
                properties.load(is);


                if(properties.containsKey("downloadId")) {

                    String id = properties.getProperty("downloadId");
                    long savedId = sharedPreferences.getLong("downloadId", 0);

                    if (Long.valueOf(id) != savedId) {
                        sharedPreferences.edit().putLong("downloadId", Long.valueOf(id)).commit();
                        Intent intent = new Intent(this, appViewClass);

                        intent.putExtra("fromApkInstaller", true);
                        intent.putExtra("id", Long.valueOf(id));


                        if(properties.containsKey("cpi_url")){

                            String cpi = properties.getProperty("cpi_url");

                            try {
                                cpi = URLDecoder.decode(cpi, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            intent.putExtra("cpi",  cpi);
                        }

                        startActivityForResult(intent, 50);
                        if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Started_From_Apkfy");

                    }
                }

            } catch (IOException e) {
                Log.e("MYTAG", "");
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }

            Log.d("StartTV", "editors: " + AptoideTV.getConfiguration().getEditorsPath());

            loadEditorsAndTopApps();

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    AptoideUtils.syncInstalledApps(mContext, db);
                }
            });

            Cursor c = database.getServers();

            ArrayList<BasicNameValuePair> storesToCheck = new ArrayList<BasicNameValuePair>();
            storesIds = new HashMap<String, Long>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                storesToCheck.add(new BasicNameValuePair(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("hash"))));
                storesIds.put(c.getString(c.getColumnIndex("name")), c.getLong(c.getColumnIndex("id_repo")));
            }

            c.close();


            StringBuilder repos = new StringBuilder();
            StringBuilder hashes = new StringBuilder();
            Iterator<?> it = storesToCheck.iterator();
            while (it.hasNext()) {
                BasicNameValuePair next = (BasicNameValuePair) it.next();
                repos.append(next.getName());
                hashes.append(next.getValue());

                if (it.hasNext()) {
                    repos.append(",");
                    hashes.append(",");
                }
            }

            request = new RepositoryChangeRequest();
            request.setRepos(repos.toString());
            request.setHashes(hashes.toString());

            checkServerCacheString = (repos.toString() + hashes.toString()).hashCode();
            if (!storesToCheck.isEmpty()) {
                spiceManager.execute(request, checkServerCacheString, DurationInMillis.ONE_HOUR, requestListener);
            }

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            editor.putInt(EnumPreferences.SCREEN_WIDTH.name(), dm.widthPixels);
            editor.putInt(EnumPreferences.SCREEN_HEIGHT.name(), dm.heightPixels);
            editor.commit();


        }



        getSupportActionBar().hide();


    }

    private void getNoSpaceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final AlertDialog noSpaceDialog = dialogBuilder.create();
        noSpaceDialog.setIcon(android.R.drawable.ic_dialog_alert);
        noSpaceDialog.setTitle(getText(cm.aptoide.ptdev.R.string.remote_in_noSD_title));
        String message;
        if(!Build.DEVICE.equals("alien_jolla_bionic")){
            message=""+getText(cm.aptoide.ptdev.R.string.remote_in_noSDspace);
        }else{
            message=""+getText(cm.aptoide.ptdev.R.string.remote_in_noSD_jolla);
        }
        noSpaceDialog.setMessage(message);
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Dont_Have_Enough_Space_On_SDCARD");
        noSpaceDialog.setButton(Dialog.BUTTON_NEUTRAL, getText(android.R.string.ok), new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        noSpaceDialog.show();
    }

    private void loadEditorsAndTopApps() {
        if(executorService != null && !executorService.isShutdown()) {
            executorService.execute( new Runnable() {
                @Override
                public void run() {
                    try {
                        waitForServiceToBeBound();
                        String countryCode = Geolocation.getCountryCode( StartTV.this );
                        String url = AptoideTV.getConfiguration().getEditorsUrl();

                        loadEditorsChoice( url, countryCode );
                        loadTopApps( AptoideTV.getConfiguration().getTopAppsUrl() );
                        //Log.d( "pullToRefresh", "execute(=)" );
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        BusProvider.getInstance().post( new DismissRefreshEvent());
                    }
                }
            } );
        }
    }

    public void loadTopApps(String url) throws IOException {

        //Log.d("Aptoide-Featured", "Loading " + url);
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
            url = ((AptoideConfigurationTV) AptoideTV.getConfiguration()).getDefaultTopAppsUrl();
        }


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
                    Toast.makeText(this, getString(cm.aptoide.ptdev.R.string.store_already_added), Toast.LENGTH_LONG).show();
                } else if (!intent.getBooleanExtra("nodialog", false)) {
                    AptoideDialog.addMyAppStore(repoUrl).show(getSupportFragmentManager(), "addStoreMyApp");
                    pager.setCurrentItem(1);
                } else {

                    Store store = new Store();
                    store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                    store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                    startParse(store);
                    pager.setCurrentItem(1);

                }

            }

        }else if (intent.hasExtra("new_updates") && pager != null) {
            pager.setCurrentItem(2);
        }else if(intent.hasExtra("fromDownloadNotification") && pager != null){
            pager.setCurrentItem(3);
        }


    }

    public void executeWizard() {
        try {
            if (AptoideTV.isUpdate()) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext());

                final Database database = new Database(AptoideTV.getDb());
                final Store store = new Store();

                String storeName = AptoideTV.getConfiguration().getDefaultStore();
                Log.d("StartTV", "default store: " + AptoideTV.getConfiguration().getDefaultStore());
                String repoUrl = "http://"+storeName+".store.aptoide.com/";

                AptoideConfigurationTV config = (AptoideConfigurationTV) AptoideTV.getConfiguration();
                store.setId(-200);
                store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                store.setDelta(null);
                store.setView(config.getStoreView());
                store.setTheme(config.getStoreTheme());
                store.setAvatar(config.getStoreAvatar());
                store.setItems(config.getStoreItems());
                database.insertStore(store);

                if(!PreferenceManager.getDefaultSharedPreferences(this).contains("version") && ((AptoideConfigurationTV) AptoideTV.getConfiguration()).getCreateShortcut()){
                    new ManagerPreferences(this).createLauncherShortcut(this, R.drawable.ic_launcher);
                }

                sharedPreferences.edit().putBoolean("firstrun", false).commit();

                PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();

//                Intent i = new Intent();
//                i.setAction(PushNotificationReceiver.PUSH_NOTIFICATION_Action_FIRST_TIME);
//                this.sendBroadcast(i);

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(((AptoideConfigurationTV)AptoideTV.getConfiguration()).getShowSplash()) {
            new SplashDialogFragment().show(getSupportFragmentManager(), "splashDialog");
        }


    }

    private void updateAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final AccountManager manager = AccountManager.get(this);
        final Account[] accountsByType = manager.getAccountsByType(AptoideTV.getConfiguration().getAccountType());
        if(accountsByType.length > 0 || "APTOIDE".equals(sharedPreferences.getString("loginType", null))){

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();
                    oAuth2AuthenticationRequest.setMode(LoginActivity.Mode.APTOIDE);
                    oAuth2AuthenticationRequest.setUsername(accountsByType[0].name);
                    oAuth2AuthenticationRequest.setPassword(manager.getPassword(accountsByType[0]));

                    try {
                        oAuth2AuthenticationRequest.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());

                        OAuth oAuth = oAuth2AuthenticationRequest.loadDataFromNetwork();

                        String refreshToken = oAuth.getRefreshToken();

                        String actualToken = manager.blockingGetAuthToken(accountsByType[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
                        manager.invalidateAuthToken(AptoideTV.getConfiguration().getAccountType(), actualToken);
                        manager.setAuthToken(accountsByType[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, refreshToken);
                        SecurePreferences.getInstance().edit().putString("access_token", oAuth.getAccess_token()).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AccountManager.get(StartTV.this).removeAccount(accountsByType[0], null, null);
                    }

                }
            }).start();

        }else{
            AccountManager.get(this).removeAccount(accountsByType[0], null, null);
        }

    }


    @Override
    public boolean onSearchRequested() {
        if (Build.VERSION.SDK_INT > 7) {
            WebSocketSingleton.getInstance().connect();
            isDisconnect = false;
            android.app.SearchManager manager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);
            manager.setOnCancelListener(new android.app.SearchManager.OnCancelListener() {
                @Override
                public void onCancel() {

                    isDisconnect = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (isDisconnect) {
                                WebSocketSingleton.getInstance().disconnect();
                            }

                        }
                    }, 10000);
                }
            });

            manager.setOnDismissListener(new android.app.SearchManager.OnDismissListener() {
                @Override
                public void onDismiss() {
                    isDisconnect = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (isDisconnect) {
                                WebSocketSingleton.getInstance().disconnect();
                            }

                        }
                    }, 10000);
                }
            });
        }
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
                if(!PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext()).getBoolean("matureChkBox", true))
                    matureUnlock();
                else
                    maturelock();


                refresh = true;

                BusProvider.getInstance().post(new RepoCompleteEvent(0));
                BusProvider.getInstance().post(new RepoCompleteEvent(-1));
                BusProvider.getInstance().post(new RepoCompleteEvent(-2));
                break;
            case 50:
                spiceManager.addListenerIfPending(RepositoryChangeJson.class, checkServerCacheString, requestListener);
                spiceManager.getFromCache(RepositoryChangeJson.class, checkServerCacheString, DurationInMillis.ONE_DAY, requestListener);
                if(resultCode == RESULT_OK && data.getBooleanExtra("addDefaultRepo", false)){
                    Store store = new Store();
                    String repoUrl = "http://apps.store.aptoide.com/";
                    store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                    store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                    startParse(store);
                    //Log.d("Start-addDefaultRepo", "added default repo "+ repoUrl);
                }
                break;
        }
        //InvalidateAptoideMenu();
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
        spiceManager.start(this);
        BusProvider.getInstance().register(this);
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    public void startParse(final Store store) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    waitForServiceToBeBound();
                    service.setShowNotification(!isLoggedin);
                    service.startParse(database, store, true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
        Toast.makeText(this, getString(cm.aptoide.ptdev.R.string.starting_download), Toast.LENGTH_LONG).show();
    }

    public void installAppFromManager(long id) {
        downloadService.startExistingDownload(id);
    }


    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public String getSponsoredCache() {
        return null;
    }

    public void matureUnlock() {
        //Log.d("Mature","Unlocked");
        matureCheck = true;
        PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext()).edit().putBoolean("matureChkBox", false).commit();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Unlocked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));
        InvalidateAptoideMenu();
    }

    @Override
    public void matureLock() {

    }

    public void maturelock() {
        //Log.d("Mature","locked");
        matureCheck = false;
        PreferenceManager.getDefaultSharedPreferences(AptoideTV.getContext()).edit().putBoolean("matureChkBox", true).commit();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Locked_Mature_Content");
        BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));
        InvalidateAptoideMenu();
    }

    private void InvalidateAptoideMenu() {
        if(!ActivityCompat.invalidateOptionsMenu(this)) {
            supportInvalidateOptionsMenu();
        }
    }

    public PagerAdapter getViewPagerAdapter() {
        return new AptoidePagerAdapter(getSupportFragmentManager(), mContext);
    }

    public void reload() {
        //Log.d( "pullToRefresh", "reload()" );
        loadEditorsAndTopApps();
    }



    boolean isLoggedin = false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("queueName", queueName);
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
        accountManager = AccountManager.get(this);

        //Login Header
        if (accountManager.getAccountsByType(AptoideTV.getConfiguration().getAccountType()).length > 0) {
            isLoggedin = true;
        }else{
            isLoggedin = false;
        }

        if(refresh){
            BusProvider.getInstance().post(new RepoCompleteEvent(0));
            BusProvider.getInstance().post(new RepoCompleteEvent(-1));
            BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        }
    }

    @Override
    protected void onPause() {
        isResumed=false;
        super.onPause();
        //Toast.makeText(this, "OnPause", Toast.LENGTH_LONG).show();

        if(rabbitMqConnBound){
            rabbitMqService.stopAmqpService();
            unbindService(rabbitMqConn);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

}
