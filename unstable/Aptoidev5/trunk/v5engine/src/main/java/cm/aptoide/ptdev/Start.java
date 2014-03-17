package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.*;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.adapters.AptoidePagerAdapter;
import cm.aptoide.ptdev.adapters.MenuListAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.exceptions.InvalidVersionException;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.social.WebViewFacebook;
import cm.aptoide.ptdev.social.WebViewTwitter;
import cm.aptoide.ptdev.tutorial.Tutorial;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.views.BadgeView;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.RepositoryChangeRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;
import org.apache.http.message.BasicNameValuePair;
import roboguice.util.temp.Ln;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Start extends ActionBarActivity implements
        StoresCallback,
        DownloadManagerCallback,
        AddStoreDialog.Callback,
        DownloadInterface,
        MyAppsAddStoreInterface,
        ProgressDialogFragment.OnCancelListener,
        AdultDialog.Callback {

    private static final String TAG = Start.class.getName();
    private static final int WIZARD_REQ_CODE = 50;
    static Toast toast;
    private ArrayList<Server> server;
    private ParserService service;
    private boolean parserServiceIsBound;
    private ReentrantLock lock = new ReentrantLock();
    private Condition boundCondition = lock.newCondition();
    private ViewPager pager;
    private BadgeView badge;
    private RepositoryChangeRequest request;
    private HashMap<String, Long> storesIds;
    private int checkServerCacheString;
    private boolean isResumed;
    private boolean matureCheck;

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

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            Log.d("Aptoide-Start", "onServiceConnected");
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
        super.onStop();
        BusProvider.getInstance().unregister(this);
        spiceManager.shouldStop();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Aptoide-OnClick", "OnClick");
        int i = item.getItemId();

        if (i == R.id.home || i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        } else if (i == R.id.menu_settings) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivityForResult(settingsIntent, 0);
        } else if (i == R.id.menu_about) {
            showAbout();
        } else if (i == R.id.menu_search) {
            onSearchRequested();
            Log.d("Aptoide-OnClick", "OnSearchRequested");

        } else if ( i == R.id.menu_filter_mature_content){

            if (item.isChecked()) {
                new AdultDialog().show(getSupportFragmentManager(), "adultDialog");
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("matureChkBox", !item.isChecked()).commit();
                item.setChecked(true);
                matureCheck = false;
                BusProvider.getInstance().post(new RepoCompleteEvent(-1));
                BusProvider.getInstance().post(new RepoCompleteEvent(-2));
            }

        }

        return super.onOptionsItemSelected(item);
    }




    @Subscribe
    public void onRepoErrorEvent(RepoErrorEvent event) {

        Exception e = event.getE();
        long repoId = event.getRepoId();

        if (e instanceof InvalidVersionException) {

            if(isResumed)AptoideDialog.wrongVersionXmlDialog().show(getSupportFragmentManager(), "wrongXmlDialog");
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

        menu.findItem(R.id.menu_filter_mature_content).setChecked(!matureCheck);

        return super.onCreateOptionsMenu(menu);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    RequestListener<RepositoryChangeJson> requestListener = new RequestListener<RepositoryChangeJson>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {

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

                                Log.d("Aptoide-RepositoryChage", "Parsing" + store.getId() + " " + store.getBaseUrl() );

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
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);



        mContext = this;
        setContentView(R.layout.activity_main);

        matureCheck = !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);


        pager = (ViewPager) findViewById(R.id.pager);

        AptoidePagerAdapter adapter = new AptoidePagerAdapter(getSupportFragmentManager(), mContext);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);

        badge = new BadgeView(mContext, ((LinearLayout)tabStrip.getChildAt(0)).getChildAt(2));


        Intent i = new Intent(this, ParserService.class);
        final SQLiteDatabase db = ((Aptoide) getApplication()).getDb();
        database = new Database(db);

        bindService(i, conn, BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);


        if (savedInstanceState == null) {

            File sdcard_file = new File(Environment.getExternalStorageDirectory().getPath());
            if (!sdcard_file.exists() || !sdcard_file.canWrite()) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                final AlertDialog noSDDialog = dialogBuilder.create();
                noSDDialog.setTitle(getText(R.string.remote_in_noSD_title));
                noSDDialog.setIcon(android.R.drawable.ic_dialog_alert);
                noSDDialog.setMessage(getText(R.string.remote_in_noSD));
                noSDDialog.setCancelable(false);
                noSDDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                noSDDialog.show();

            } else {
                StatFs stat = new StatFs(sdcard_file.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                long total = (blockSize * totalBlocks) / 1024 / 1024;
                long avail = (blockSize * availableBlocks) / 1024 / 1024;
                Log.d("Aptoide", "* * * * * * * * * *");
                Log.d("Aptoide", "Total: " + total + " Mb");
                Log.d("Aptoide", "Available: " + avail + " Mb");

                if (avail < 10) {
                    Log.d("Aptoide", "No space left on SDCARD...");
                    Log.d("Aptoide", "* * * * * * * * * *");

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

                    final AlertDialog noSpaceDialog = dialogBuilder.create();
                    noSpaceDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    noSpaceDialog.setTitle(getText(R.string.remote_in_noSD_title));
                    noSpaceDialog.setMessage(getText(R.string.remote_in_noSDspace));
                    noSpaceDialog.setButton(Dialog.BUTTON_NEUTRAL, getText(android.R.string.ok), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    noSpaceDialog.show();
                }
            }


            if (getIntent().hasExtra("newrepo") && getIntent().getFlags() == 12345) {
                ArrayList<String> repos = getIntent().getExtras().getStringArrayList("newrepo");
                for (final String repoUrl : repos) {

                    if (database.existsServer(AptoideUtils.RepoUtils.formatRepoUri(repoUrl))) {
                        Toast.makeText(this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
                    } else if (!getIntent().getBooleanExtra("nodialog", false)) {
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
                getIntent().removeExtra("newrepo");
            } else if (getIntent().hasExtra("fromDownloadNotification") && pager != null) {
                getIntent().removeExtra("fromDownloadNotification");
                pager.setCurrentItem(3);
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String queueName = sharedPreferences.getString("queueName", null);


            if (queueName != null) {
                Intent serviceIntent = new Intent(this, RabbitMqService.class);
                startService(serviceIntent);
            }


            new AutoUpdate(this).execute();
            executeWizard();


            try {
                InputStream is = getAssets().open("actionsOnBoot.properties");
                Properties properties = new Properties();
                properties.load(is);

                String id = properties.getProperty("downloadId");
                long savedId = sharedPreferences.getLong("downloadId", 0);

                if (Long.valueOf(id) != savedId) {
                    sharedPreferences.edit().putLong("downloadId", Long.valueOf(id)).commit();
                    Intent intent = new Intent(this, AppViewActivity.class);

                    intent.putExtra("fromApkInstaller", true);
                    intent.putExtra("id", Long.valueOf(id));

                    startActivity(intent);
                }

            } catch (IOException e) {
                Log.e("MYTAG", "");
                e.printStackTrace();
            }


            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitForServiceToBeBound();
                        service.parseEditorsChoice(database, "http://apps.store.aptoide.com/editors_more.xml?country=" + AptoideUtils.getMyCountry(Start.this));
                        service.parseTopApps(database, "http://apps.store.aptoide.com/top.xml");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

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


            StringBuffer repos = new StringBuffer();
            StringBuffer hashes = new StringBuffer();
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

            updateBadge(PreferenceManager.getDefaultSharedPreferences(this));

        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mMenuAdapter = new MenuListAdapter(mContext);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };


        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
                    Intent whatsNewTutorial = new Intent(mContext, Tutorial.class);
                    whatsNewTutorial.putExtra("isUpdate", true);
                    startActivityForResult(whatsNewTutorial, WIZARD_REQ_CODE);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateBadge(SharedPreferences sPref){
        badge.setTextSize(12);
        int size = sPref.getInt("updates", 0);
        if(size!=0) {
            badge.setText(String.valueOf(size));
            if(!badge.isShown())badge.show(true);
        }else{
            if(badge.isShown())badge.hide(true);
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
            case 50:
                spiceManager.addListenerIfPending(RepositoryChangeJson.class, checkServerCacheString, requestListener);
                spiceManager.getFromCache(RepositoryChangeJson.class, checkServerCacheString, DurationInMillis.ONE_DAY, requestListener);
                if(resultCode == RESULT_OK && data.getBooleanExtra("addDefaultRepo", false)){
                    Store store = new Store();
                    String repoUrl = "http://apps.store.aptoide.com/";
                    store.setBaseUrl(AptoideUtils.RepoUtils.formatRepoUri(repoUrl));
                    store.setName(AptoideUtils.RepoUtils.split(repoUrl));
                    startParse(store);
                    Log.d("Start-addDefaultRepo", "added default repo "+ repoUrl);
                }
                break;
        }

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
    }


    @Override
    public void showAddStoreDialog() {
        DialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");
    }

    @Override
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


    public void updateAll(List<Long> ids){
        for(long id : ids){
            installApp(id);
        }
    }


    @Override
    public void reloadStores(Set<Long> checkedItems) {


        for (final Long storeid : checkedItems) {


            Runnable runnable = new Runnable() {
                public void run() {
                    final Database db = new Database(Aptoide.getDb());
                    final Store store = new Store();
                    Log.d("Aptoide-Reloader", "Reloading storeid " + storeid);
                    Cursor c = db.getStore(storeid);

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
                    service.startParse(db, store, false);
                }
            };
            executorService.submit(runnable);
        }


    }

    @Override
    public boolean isRefreshing(long id) {
        return service.repoIsParsing(id);
    }

    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
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

        if(fragment!=null){
            fragment.cancelListener.onCancel();
        }

    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public void onOk() {
        matureCheck = true;
        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", false).commit();
        BusProvider.getInstance().post(new RepoCompleteEvent(-2));
        BusProvider.getInstance().post(new RepoCompleteEvent(-1));
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {


            int switchId = (int) id;

            switch (switchId) {
                case 0:
                    Log.d("MenuDrawer-position", "pos: " + position);
                    Intent loginIntent = new Intent(mContext, MyAccountActivity.class);
                    startActivity(loginIntent);
                    break;
                case 1:
                    Log.d("MenuDrawer-position", "pos: " + position);
                    Intent rollbackIntent = new Intent(mContext, RollbackActivity.class);
                    startActivity(rollbackIntent);
                    break;
                case 2:
                    Log.d("MenuDrawer-position", "pos: "+position);
                    Intent scheduledIntent = new Intent(mContext, ScheduledDownloadsActivity.class);
                    startActivity(scheduledIntent);
                    break;
                case 3:
                    Log.d("MenuDrawer-position", "pos: "+position);
                    Intent excludedIntent = new Intent(mContext, ExcludedUpdatesActivity.class);
                    startActivity(excludedIntent);
                    break;
                case 4:
                    Log.d("MenuDrawer-position", "pos: " + position);
                    showFacebook();
                    break;
                case 5:
                    Log.d("MenuDrawer-position", "pos: " + position);
                    showTwitter();
                    break;
                default:
                    break;
            }

            mDrawerLayout.closeDrawer(mDrawerList);

        }

    }

    boolean isLoggedin = false;

    @Override
    protected void onResume() {

        super.onResume();
        isResumed = true;
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        TextView login_email;
        ImageView user_avatar;
        accountManager = AccountManager.get(this);

        if(mDrawerList.getHeaderViewsCount()>0){
            View v = mDrawerList.getChildAt(0);
            mDrawerList.removeHeaderView(v);
        }

        mDrawerList.setAdapter(null);

        //Login Header
        if (accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {
            View header = LayoutInflater.from(mContext).inflate(R.layout.header_logged_in, null);
            isLoggedin = true;

            mDrawerList.addHeaderView(header, null, false);

            login_email = (TextView) header.findViewById(R.id.login_email);
            login_email.setText(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0].name);

            user_avatar = (ImageView) header.findViewById(R.id.user_avatar);
            String avatarUrl = PreferenceManager.getDefaultSharedPreferences(mContext).getString("useravatar",null);

            if (avatarUrl != null) {
                ImageLoader.getInstance().displayImage(avatarUrl, user_avatar);
            }

        }else{
            isLoggedin = false;
        }
        mDrawerList.setAdapter(mMenuAdapter);

    }

    @Override
    protected void onPause() {
        isResumed=false;
        super.onPause();
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
            }
        } else {
            Intent intent = new Intent(mContext, WebViewFacebook.class);
            startActivity(intent);
        }
    }

    private void showTwitter() {
        if (AptoideUtils.isAppInstalled(mContext, "com.twitter.android")) {
            String url = "http://www.twitter.com/aptoide";
            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(twitterIntent);
        } else {
            Intent intent = new Intent(mContext, WebViewTwitter.class);
            startActivity(intent);
        }
    }

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
    }



}
