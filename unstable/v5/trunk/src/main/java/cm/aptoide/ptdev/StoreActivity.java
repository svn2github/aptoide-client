package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store_list);
        storeName = getIntent().getStringExtra("storename");
        storeid = getIntent().getLongExtra("storeid", 0);
        themeordinal = getIntent().getIntExtra("theme", 0);
        storeAvatarUrl = getIntent().getStringExtra("storeavatarurl");
        if(savedInstanceState==null){
            setFragment();
        }

    }

    private void setFragment() {
        Fragment fragment = new MyListFragment();
        Fragment fragmentHeader = new FragmentStoreHeader();

        Log.d("Aptoide-", "StoreActivity id" + storeid);


        Bundle args = new Bundle();
        args.putLong("storeid", storeid);
        //args.putBoolean("list", list);
        //args.putInt("theme", theme);
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
