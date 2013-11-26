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
import com.actionbarsherlock.view.MenuItem;
import com.squareup.otto.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */

public class StoreActivity extends SherlockFragmentActivity{


    private long storeid;
    private int themeordinal;
    private String storeName;
    private String storeAvatarUrl;
    private ParserService service;

    public boolean isRefreshing() {
        return isRefreshing;
    }

    private boolean isRefreshing;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ParserService.MainServiceBinder) binder).getService();
            Toast.makeText(getApplicationContext(), "Is repo parsing? " + service.repoIsParsing(storeid), Toast.LENGTH_LONG).show();
            isRefreshing = service.repoIsParsing(storeid);
            FragmentStoreListCategories fragmentStoreListCategories = (FragmentStoreListCategories) getSupportFragmentManager().findFragmentByTag("fragStore");
            if (fragmentStoreListCategories != null) fragmentStoreListCategories.setRefreshing(isRefreshing);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
        if(savedInstanceState==null){
            setFragment();
        }


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

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event){
        if(event.getRepoId()==storeid){
            FragmentStore fragStore = (FragmentStore) getSupportFragmentManager().findFragmentByTag("fragStore");
            fragStore.onRefresh();
            fragStore.setRefreshing(false);
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
        getSupportFragmentManager().beginTransaction().add(R.id.content_layout, fragment, "fragStore").add(R.id.store_header_layout, fragmentHeader, "fragStoreHeader").commit();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.abs__home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
