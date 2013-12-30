package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.*;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.StoreAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.fragments.callbacks.StoresCallback;
import cm.aptoide.ptdev.parser.events.StopParseEvent;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStores extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Callback {

    private StoresCallback callback;

    private StoresCallback dummyCallback = new StoresCallback() {

        @Override
        public void showAddStoreDialog() {}

        @Override
        public void reloadStores(Set<Long> checkedItems) {}

        @Override
        public boolean isRefreshing(long id) {
            return false;
        }

    };



    private StoreAdapter storeAdapter;
    private ArrayList<StoreItem> stores = new ArrayList<StoreItem>();

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

        Log.d("Aptoide-", "OnEvent");

        loader.forceLoad();
    }

    @Override
    public void onDetach() {
        if(storeAdapter!=null){
            storeAdapter.setAdapterView(gridViewMyStores);
        }
        super.onDetach();
        BusProvider.getInstance().unregister(this);

        this.callback = dummyCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storeAdapter = new StoreAdapter(savedInstanceState, getActivity(), stores, this);
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

        view.findViewById(R.id.button_add_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.showAddStoreDialog();
            }
        });



        storeAdapter.setAdapterView(gridViewMyStores);

        storeAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), StoreActivity.class);
                StoreItem store = (StoreItem) parent.getItemAtPosition(position);

                i.putExtra("storeid", id);
                i.putExtra("isrefreshing", callback.isRefreshing(id));
                i.putExtra("list", store.isList());
                startActivity(i);


            }
        });

        loader = getLoaderManager().restartLoader(0, null, this);



    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        storeAdapter.save(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
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
            String theme = data.getString(data.getColumnIndex(Schema.Repo.COLUMN_THEME));
            if(theme!=null){
                theme = theme.toUpperCase();
            }else{
                theme="DEFAULT";
            }
            stores.add(new StoreItem(
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_NAME)),
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_DOWNLOADS)),
                    data.getString(data.getColumnIndex(Schema.Repo.COLUMN_AVATAR)),
                    EnumStoreTheme.get("APTOIDE_STORE_THEME_" + theme),
                    "list".equals(data.getString(data.getColumnIndex(Schema.Repo.COLUMN_VIEW))),
                    data.getLong(data.getColumnIndex(Schema.Repo.COLUMN_ID)))
            );
            Log.d("Aptoide-", "Added store");
        }

        storeAdapter.setAdapterView(gridViewMyStores);
        storeAdapter.notifyDataSetChanged();

        Log.d("Aptoide-", "OnLoadFinish");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void removeStores(Set<Long> checkedItems) {
        setRetainInstance(true);
        new AsyncTask<Set<Long>, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DialogFragment pd = AptoideDialog.pleaseWaitDialog();
                pd.setCancelable(false);
                pd.show(getFragmentManager(), "pleaseWaitDialogRemove");

            }

            @Override
            protected Void doInBackground(Set<Long>... params) {
                try{
                    for(Long aLong: params[0]){
                        BusProvider.getInstance().post(new StopParseEvent(aLong));
                    }
                    new Database(Aptoide.getDb()).removeStores(params[0]);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialogRemove");
                pd.dismiss();
                setRetainInstance(false);
                loader.forceLoad();
                //BusProvider.getInstance().post(new RepoCompleteEvent(0));
            }
        }.execute(checkedItems);

    }

    @Override
    public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_reload) {

            HashSet<Long> longs = new HashSet<Long>();
            for(Long aLong : storeAdapter.getCheckedItems()){
                longs.add(storeAdapter.getItemId(aLong.intValue()));
            }
            mode.finish();
            callback.reloadStores(longs);

            return true;
        } else if (id == R.id.menu_discard) {

            HashSet<Long> longs = new HashSet<Long>();
            for(Long aLong : storeAdapter.getCheckedItems()){
                longs.add(storeAdapter.getItemId(aLong.intValue()));
            }
            mode.finish();
            removeStores(longs);
            return true;
        } else if( id == R.id.menu_select_all){
            storeAdapter.selectAll();
            return true;
        }

        return false;

    }
}

