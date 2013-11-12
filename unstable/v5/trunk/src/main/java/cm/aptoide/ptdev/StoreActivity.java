package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */

public class StoreActivity extends SherlockFragmentActivity implements StoresCallback {


    private long storeid;
    private int themeordinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store_list);
        storeid = getIntent().getLongExtra("storeid", 0);
        themeordinal = getIntent().getIntExtra("theme", 0);
        if(savedInstanceState==null){
            setFragment();
        }

    }

    private void setFragment() {
        Fragment fragment = new MyListFragment();
        Fragment fragmentHeader = new StoreHeaderFragment();

        Log.d("Aptoide-", "StoreActivity id" + storeid);

        Bundle argsHeader = new Bundle();
        argsHeader.putInt("theme", themeordinal);

        fragmentHeader.setArguments(argsHeader);

        Bundle args = new Bundle();
        args.putLong("storeid", storeid);
        //args.putBoolean("list", list);
        //args.putInt("theme", theme);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().add(R.id.content_layout, fragment, "fragStore").add(R.id.store_header_layout, fragmentHeader, "fragStoreHeader").commit();
    }

    @Override
    public void showAddStoreDialog() {


    }

    @Override
    public void click() {

        Fragment fragment = new MyListFragment();
        Bundle args = new Bundle();

        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();

    }
}
