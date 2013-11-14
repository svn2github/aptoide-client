package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.model.json.RepositoryInfoJson;
import cm.aptoide.ptdev.parser.callbacks.CompleteCallback;
import cm.aptoide.ptdev.parser.callbacks.ErrorCallback;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

import java.util.ArrayList;

public class MainActivity extends SherlockFragmentActivity implements StoresCallback, DownloadManagerCallback, AddStoreDialog.Callback {
    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    private ArrayList<Server> server;
    private ParserService service;

    private boolean parserServiceIsBound;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (ParserService) ((ParserService.MainServiceBinder)binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
            service.setCallbacks(errorCallback, parserCompleteCallback);
            parserServiceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            parserServiceIsBound = false;
        }
    };

    Database database;
    private AsyncTask task;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);


    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    ErrorCallback errorCallback = new ErrorCallback() {
        @Override
        public void onError(Exception e, long repoId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Parse error", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Parse " + repoId +  " Completed", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        AptoidePagerAdapter adapter = new AptoidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);



        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);


        getSupportActionBar().setTitle("");

        //testWebservicesCalls();
        Intent i = new Intent(this, ParserService.class);
        SQLiteDatabase db = ((Aptoide)getApplication()).getDb();
        database = new Database(db);

        if(savedInstanceState==null){
            startService(i);
        }

        bindService(i, conn, BIND_AUTO_CREATE);

        //spiceManager.execute( new GetRepositoryInfoRequest(AptoideUtils.RepoUtils.split(s)), "json", DurationInMillis.ONE_MINUTE, new WeatherRequestListener());







    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(parserServiceIsBound) unbindService(conn);
    }

    @Override
    public void showAddStoreDialog() {

        DialogFragment newFragment = AptoideDialog.addStoreDialog();
        newFragment.show(getSupportFragmentManager(), "addStoreDialog");

    }



    @Override
    public void click() {

        getSupportLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(MainActivity.this) {
                    @Override
                    public Cursor loadInBackground() {
                        return database.getServers();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Toast.makeText(MainActivity.this, String.valueOf(data.getCount()), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

    }

    @Override
    public void addStore(final String s) {

        final String url = AptoideUtils.checkStoreUrl(s);

        String repoName = AptoideUtils.RepoUtils.split(url);

        Log.d("Aptoide-", repoName);

        spiceManager.execute(new GetRepositoryInfoRequest(repoName), url.hashCode(), DurationInMillis.ONE_HOUR, new RequestListener<RepositoryInfoJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Toast.makeText(MainActivity.this, "Unable to add store", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRequestSuccess(RepositoryInfoJson repositoryInfoJson) {

                Store store = new Store();

                store.setBaseUrl(url);
                store.setName(repositoryInfoJson.getListing().getName());
                store.setDownloads(repositoryInfoJson.getListing().getDownloads());
                store.setAvatar(repositoryInfoJson.getListing().getAvatar());
                store.setDescription(repositoryInfoJson.getListing().getDescription());
                store.setTheme(repositoryInfoJson.getListing().getTheme());
                store.setView(repositoryInfoJson.getListing().getView());
                store.setItems(repositoryInfoJson.getListing().getItems());

                service.get(database, store);
            }
        });


    }

}
