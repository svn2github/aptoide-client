package cm.aptoide.ptdev;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.services.ParserService;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends SherlockFragmentActivity implements StoresCallback, DownloadManagerCallback, AddStoreDialog.Callback {
    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    ErrorCallback errorCallback = new ErrorCallback() {
        @Override
        public void onError(final Exception e, final long repoId) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Parse error on " + repoId + " with " + e, Toast.LENGTH_LONG).show();
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
    private DismissCallback dismissCallback = new DismissCallback() {
        @Override
        public void onDismiss(String message) {
            SherlockDialogFragment pd = (SherlockDialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
            if (pd != null) {
                pd.dismiss();
            }
            if (message != null) {
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
        }
    };
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder) binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
            service.setCallbacks(errorCallback, parserCompleteCallback, dismissCallback);
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


    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parserServiceIsBound) unbindService(conn);
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

        }

        bindService(i, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    public void showAddStoreDialog() {
        DialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");
    }

    StoreGetter storeGetter;

    @Override
    public void addStore(final String inputUrl, Login login) {


        storeGetter = new StoreGetter(spiceManager, database, dismissCallback, service, new StoreGetter.StorePasswordCallback() {
            @Override
            public void on401(final String store) {

                Login login = new Login();
                login.setPassword("mateus");
                login.setUsername("mateus");
                addStore(store, login);

            }
        });
        ProgressDialogFragment pd = (ProgressDialogFragment) AptoideDialog.pleaseWaitDialog();
        pd.setOnCancelListener(listener);

        pd.show(getSupportFragmentManager(), "pleaseWaitDialog");
        storeGetter.get(inputUrl, login);
    }

    @Override
    public void reloadStores(Set<Long> checkedItems) {

    }

    ProgressDialogFragment.OnCancelListener listener = new ProgressDialogFragment.OnCancelListener() {
        @Override
        public void onCancel() {
            Toast.makeText(mContext, "Canceled", Toast.LENGTH_LONG).show();
            spiceManager.cancel(storeGetter.getRequest());
        }
    };



    public interface DismissCallback {
        public void onDismiss(String message);
    }


}
