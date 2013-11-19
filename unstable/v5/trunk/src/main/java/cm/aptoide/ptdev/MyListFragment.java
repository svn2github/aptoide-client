package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockListFragment;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class MyListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {



    private ArrayList<Category> categories = new ArrayList<Category>();
    private Database database;
    private CategoryAdapter arrayAdapter;
    private long storeid;
    private long parentid;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new Database(Aptoide.getDb());
        arrayAdapter = new CategoryAdapter(getSherlockActivity());
        setListAdapter(arrayAdapter);
        storeid = getArguments().getLong("storeid");
        parentid = getArguments().getLong("parentid");
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("Aptoide-", "StoreFragment id" + storeid);

    }



    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = new Bundle();
        bundle.putLong("storeid", storeid);
        bundle.putLong("parentid", parentid);

        getLoaderManager().restartLoader(20, bundle, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        int type = l.getAdapter().getItemViewType(position);

        switch (type){
            case 0:
                startActivity(new Intent(getSherlockActivity(), AppViewActivity.class));
                break;
            case 1:
                Fragment fragment = new MyListFragment();
                Bundle args = new Bundle();

                args.putLong("storeid", storeid);
                args.putLong("parentid", id);

                fragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new SimpleCursorLoader(getSherlockActivity()) {
            @Override
            public Cursor loadInBackground() {
                return database.getCategories(args.getLong("storeid"), args.getLong("parentid"));
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        arrayAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        arrayAdapter.swapCursor(null);
    }
}
