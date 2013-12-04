package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoErrorEvent;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.exceptions.ParseStoppedException;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends SherlockFragmentActivity implements StoresCallback, DownloadManagerCallback, AddStoreDialog.Callback {
    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    private ArrayList<Server> server;
    private ParserService service;
    private boolean parserServiceIsBound;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
            parserServiceIsBound = true;
            database.deleteFeatured(2);
            database.deleteFeatured(1);
            service.parseEditorsChoice(database, "http://apps.store.aptoide.com/editors.xml");
            service.parseTopApps(database, "http://apps.store.aptoide.com/top.xml");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            parserServiceIsBound = false;
        }
    };
    private Database database;
    private Context mContext;
    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);
    private String currentRequest;


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
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Aptoide-OnClick", "OnClick");
        int i = item.getItemId();
        if (i == R.id.menu_search) {
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
        getSupportMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mContext = this;
        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        AptoidePagerAdapter adapter = new AptoidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);

        Intent i = new Intent(this, ParserService.class);
        final SQLiteDatabase db = ((Aptoide) getApplication()).getDb();
        database = new Database(db);

        bindService(i, conn, BIND_AUTO_CREATE);


        if (savedInstanceState == null) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    AptoideUtils.syncInstalledApps(mContext, db);
                }
            });
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
        SherlockDialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");
    }

    @Override
    public void startParse(Store store) {
        service.startParse(database, store);
    }

    @Override
    public void reloadStores(Set<Long> checkedItems) {

    }



}
