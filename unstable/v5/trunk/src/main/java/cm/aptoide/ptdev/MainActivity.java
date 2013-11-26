package cm.aptoide.ptdev;

import android.content.*;
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
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.parser.exceptions.ParseStoppedException;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;

public class MainActivity extends SherlockFragmentActivity implements StoresCallback, DownloadManagerCallback, AddStoreDialog.Callback {
    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    ErrorCallback errorCallback = new ErrorCallback() {
        @Override
        public void onError(final Exception e, final long repoId) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(e instanceof ParseStoppedException){
                        Toast.makeText(getApplicationContext(), "Parse stopped on " + repoId + " with " + e, Toast.LENGTH_LONG).show();

                    }else{
                        Toast.makeText(getApplicationContext(), "Parse error on " + repoId + " with " + e, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }


    };
    CompleteCallback parserCompleteCallback = new CompleteCallback() {
        @Override
        public void onComplete(final long repoId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Parse " + repoId + " Completed", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    private ArrayList<Server> server;
    private ParserService service;
    private boolean parserServiceIsBound;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
            parserServiceIsBound = true;
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
        BusProvider.getInstance().unregister(this);
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parserServiceIsBound){
            unbindService(conn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
        SQLiteDatabase db = ((Aptoide) getApplication()).getDb();
        database = new Database(db);

        if (savedInstanceState == null) {
            Executors.newFixedThreadPool(1).submit(new Runnable() {
                @Override
                public void run() {
                    AptoideUtils.syncInstalledApps();
                }
            });
        }

        bindService(i, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        BusProvider.getInstance().register(this);
        super.onStart();
    }



    @Override
    public void showAddStoreDialog() {
        SherlockDialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");
    }

    @Override
    public void startParse(Store store, Login login) {
        service.startParse(database, store, login);
    }

    @Override
    public void reloadStores(Set<Long> checkedItems) {

    }



}
