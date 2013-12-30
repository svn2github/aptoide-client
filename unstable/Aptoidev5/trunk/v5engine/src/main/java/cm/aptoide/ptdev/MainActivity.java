package cm.aptoide.ptdev;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.adapters.AptoidePagerAdapter;
import cm.aptoide.ptdev.adapters.MenuListAdapter;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.exceptions.ParseStoppedException;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.social.WebViewFacebook;
import cm.aptoide.ptdev.social.WebViewTwitter;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.squareup.otto.Subscribe;
import roboguice.util.temp.Ln;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends ActionBarActivity implements StoresCallback, DownloadManagerCallback, AddStoreDialog.Callback {

    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    private ArrayList<Server> server;
    private ParserService service;
    private boolean parserServiceIsBound;
    private ReentrantLock lock = new ReentrantLock();
    private Condition boundCondition = lock.newCondition();
    private ViewPager pager;

    public DownloadService getDownloadService() {
        return downloadService;
    }

    private DownloadService downloadService;

    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder)binder).getService();
            BusProvider.getInstance().post(new DownloadServiceConnected());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

        private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
            parserServiceIsBound = true;

            lock.lock();
            try{
                boundCondition.signalAll();
            }finally {
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
    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    MenuListAdapter mMenuAdapter;

    private CharSequence mDrawerTitle;

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
        if (parserServiceIsBound){
            unbindService(conn);
            unbindService(conn2);
        }
        unregisterReceiver(newRepoReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Aptoide-OnClick", "OnClick");
        int i = item.getItemId();

        if (i == R.id.home || i == android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }else if (i == R.id.menu_settings) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivityForResult(settingsIntent, 0);
        }else if (i == R.id.menu_about) {
            showAbout();
        }else if (i == R.id.menu_search) {
            onSearchRequested();
            Log.d("Aptoide-OnClick", "OnSearchRequested");

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onRepoErrorEvent(RepoErrorEvent event){

        Exception e = event.getE();
        long repoId = event.getRepoId();

        if(e instanceof ParseStoppedException){
            Toast.makeText(getApplicationContext(), "Parse stopped on " + repoId + " with " + e, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "Parse error on " + repoId + " with " + e, Toast.LENGTH_LONG).show();
        }

    }

    @Subscribe
    public void onRepoComplete(RepoCompleteEvent event){
        long repoId = event.getRepoId();
        Toast.makeText(getApplicationContext(), "Parse " + repoId + " Completed", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_main);

        pager = (ViewPager) findViewById(R.id.pager);

        AptoidePagerAdapter adapter = new AptoidePagerAdapter(getSupportFragmentManager(), mContext);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);

        Intent i = new Intent(this, ParserService.class);
        final SQLiteDatabase db = ((Aptoide) getApplication()).getDb();
        database = new Database(db);

        bindService(i, conn, BIND_AUTO_CREATE);
        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);
        registerReceiver(newRepoReceiver, new IntentFilter("pt.caixamagica.aptoide.NEWREPO"));


        if (savedInstanceState == null) {

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitForServiceToBeBound();
                        service.parseEditorsChoice(database, "http://apps.store.aptoide.com/editors.xml");
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

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    AptoideUtils.syncInstalledApps(mContext, db);
                }
            });



        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mMenuAdapter = new MenuListAdapter(mContext);

        //Login Header
//        TextView login_email, login_store;
//        accountManager=AccountManager.get(this);
//        if(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length>0){
//            View header = LayoutInflater.from(mContext).inflate(R.layout.header_logged_in, null);
//            mDrawerList.addHeaderView(header, null, false);
//
//            login_email = (TextView) header.findViewById(R.id.login_email);
//            login_email.setText(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0].name);
//
//            login_store = (TextView) header.findViewById(R.id.login_store);
//            login_store.setText("");
//        }

        TextView login_email, login_store;
        accountManager=AccountManager.get(this);

        if(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length>0){
            View header = LayoutInflater.from(mContext).inflate(R.layout.header_logged_in, null);

            login_email = (TextView) header.findViewById(R.id.login_email);
            login_email.setText(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0].name);

            login_store = (TextView) header.findViewById(R.id.login_store);
            login_store.setText("");
            mDrawerList.addHeaderView(header, null, false);
        }

        mDrawerList.setAdapter(mMenuAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                // TODO Auto-generated method stub
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                // Set the title on the action when drawer open
                getSupportActionBar().setTitle(mDrawerTitle);
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


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
        switch (requestCode){
            case 20:

                Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_LONG).show();
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
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                service.startParse(database, store, true);
            }
        });

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {


        // Locate Position
        switch (position){
            case 0:
//                Log.d("MenuDrawer-position", "pos: "+position);
                Intent loginIntent = new Intent(mContext, LoginActivity.class);
                startActivity(loginIntent);
                break;
            case 1:
//                Log.d("MenuDrawer-position", "pos: "+position);
                Intent rollbackIntent = new Intent(mContext, RollbackActivity.class);
                startActivity(rollbackIntent);
                break;
            case 2:
//                Log.d("MenuDrawer-position", "pos: "+position);
                Intent scheduledIntent = new Intent(mContext, ScheduledDownloadsActivity.class);
                startActivity(scheduledIntent);
                break;
            case 3:
//                Log.d("MenuDrawer-position", "pos: "+position);
                Intent excludedIntent = new Intent(mContext, ExcludedUpdatesActivity.class);
                startActivity(excludedIntent);
                break;
            case 5:
//                Log.d("MenuDrawer-position", "pos: "+position);
                showFacebook();
                break;
            case 6:
//                Log.d("MenuDrawer-position", "pos: "+position);
                showTwitter();
                break;
            default: break;
        }

        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
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

    private void showTwitter(){
        if (AptoideUtils.isAppInstalled(mContext, "com.twitter.android")) {
            String url = "http://www.twitter.com/aptoide";
            Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(twitterIntent);
        } else {
            Intent intent = new Intent(mContext, WebViewTwitter.class);
            startActivity(intent);
        }
    }

    private void showAbout(){
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


    private BroadcastReceiver newRepoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("newrepo")) {
                ArrayList<String> repos = intent.getExtras().getStringArrayList("newrepo");
                for (final String uri2 : repos) {
//                    if (Database.Instance().getServer(RepoUtils.formatRepoUri(uri2)) != null) {
//                        Toast.makeText(MainActivity.this, getString(R.string.store_already_added), Toast.LENGTH_LONG).show();
//                    } else {
                          showAddStoreDialog();
//                    }
                }
            }

        }
    };
}
