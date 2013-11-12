package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStores extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private StoresCallback callback;

    private StoresCallback dummyCallback = new StoresCallback() {

        @Override
        public void showAddStoreDialog() {

        }

        @Override
        public void click() {

        }
    };

    private StoreAdapter storeAdapter;
    private ArrayList<StoreItem> stores;
    private GridView gridViewMyStores;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (StoresCallback) activity;
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void RefreshStoresEvent(RepoAddedEvent event){
        loader.forceLoad();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
        this.callback = dummyCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_my_stores,container, false);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_stores, menu);
    }

    Loader<Cursor> loader;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridViewMyStores = (GridView) view.findViewById(R.id.gridview_my_stores);

        Toast.makeText(getSherlockActivity(), String.valueOf(R.id.button_add_store),Toast.LENGTH_LONG).show();

        view.findViewById(R.id.button_add_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.showAddStoreDialog();
            }
        });

        stores = new ArrayList<StoreItem>();

        loader = getLoaderManager().restartLoader(0, null, this);

        storeAdapter = new StoreAdapter(savedInstanceState, getSherlockActivity(), stores);

        storeAdapter.setAdapterView(gridViewMyStores);

        storeAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getSherlockActivity(), StoreActivity.class);
                Log.d("Aptoide-", "Store_id = " + id);
                i.putExtra("storeid", id);
                EnumStoreTheme storeTheme = ( (StoreItem) parent.getItemAtPosition(position) ).getTheme();
                i.putExtra("theme", storeTheme.ordinal());
                startActivity(i);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeAdapter.save(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SimpleCursorLoader(getSherlockActivity()) {
            @Override
            public Cursor loadInBackground() {
                return new Database(Aptoide.getDb()).getServers();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        stores.clear();

        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
            stores.add(new StoreItem(
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_NAME)),
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_DOWNLOADS)),
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_AVATAR)),
                    EnumStoreTheme.valueOf("APTOIDE_STORE_THEME_" + data.getString(data.getColumnIndex(Schema.Repo.COLUMN_THEME)).toUpperCase(Locale.ENGLISH)),
                    "list".equals(data.getString(data.getColumnIndex(Schema.Repo.COLUMN_VIEW))),
                    data.getLong(data.getColumnIndex(Schema.Repo.COLUMN_ID)))
            );
        }

        storeAdapter.notifyDataSetChanged();


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

