package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.services.ParserService;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.otto.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */

public class StoreActivity extends SherlockFragmentActivity {


    private long storeid;
    private int themeordinal;
    private String storeName;
    private String storeAvatarUrl;
    private ParserService service;
    private boolean serviceIsBound;
    private boolean isRefreshing;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ParserService.MainServiceBinder) binder).getService();
            isRefreshing = service.repoIsParsing(storeid);

            Toast.makeText(getApplicationContext(), "Is repo parsing? " + service.repoIsParsing(storeid), Toast.LENGTH_LONG).show();
            FragmentStoreListCategories fragmentStoreListCategories = (FragmentStoreListCategories) getSupportFragmentManager().findFragmentByTag("fragStore");
            if (fragmentStoreListCategories != null) fragmentStoreListCategories.setRefreshing(isRefreshing);
            serviceIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceIsBound = false;
        }
    };

    public boolean isRefreshing() {
        return isRefreshing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this, ParserService.class);
        bindService(i, conn, BIND_AUTO_CREATE);
        setContentView(R.layout.page_store_list);
        storeName = getIntent().getStringExtra("storename");
        storeid = getIntent().getLongExtra("storeid", 0);
        themeordinal = getIntent().getIntExtra("theme", 0);
        storeAvatarUrl = getIntent().getStringExtra("storeavatarurl");
        if (savedInstanceState == null) {
            setFragment();
        }

    }

    private void setFragment() {
        Fragment fragment = new FragmentStoreListCategories();
        Fragment fragmentHeader = new FragmentStoreHeader();

        Log.d("Aptoide-", "StoreActivity id" + storeid);


        Bundle args = new Bundle();
        args.putLong("storeid", storeid);

        fragment.setArguments(args);
        fragmentHeader.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").replace(R.id.store_header_layout, fragmentHeader, "fragStoreHeader").commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceIsBound) unbindService(conn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getSupportMenuInflater().inflate(R.menu.menu_categories, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.abs__home) {
            finish();
        } else if( i == R.id.refresh_store){
            refreshList();
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event) {
        if (event.getRepoId() == storeid) {
            refreshList();
        }
    }

    private void refreshList() {
        isRefreshing = service.repoIsParsing(storeid);

        FragmentStore fragStore = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
        fragStore.onRefresh();
        fragStore.setRefreshing(isRefreshing);
    }
}
