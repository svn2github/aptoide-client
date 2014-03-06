package cm.aptoide.ptdev.fragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.InstalledAdapter;
import cm.aptoide.ptdev.adapters.UpdatesAdapter;
import cm.aptoide.ptdev.adapters.UpdatesSectionListAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentUpdates extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private InstalledAdapter installedAdapter;
    private UpdatesAdapter updatesAdapter;
    private Database db;
    private RecentlyUpdated recentUpdates;

    private UpdatesSectionListAdapter adapter;
    private int counter;


    @Subscribe
    public void newAppEvent(InstalledApkEvent event) {
        refreshStoresEvent(null);
    }

    @Subscribe
    public void removedAppEvent(UnInstalledApkEvent event) {
        refreshStoresEvent(null);
    }

    @Subscribe
    public void refreshStoresEvent(RepoCompleteEvent event) {
        setListShown(false);
        Log.d("Aptoide-", "OnEvent");
        getLoaderManager().restartLoader(91, null, this);




    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                counter++;
                return db.getUpdates();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sPref.edit();
        int updates = 0;
        if (data.getCount() > 1) {

            updatesAdapter.swapCursor(data);
            for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
                if(data.getInt(data.getColumnIndex("is_update"))==1){
                    updates++;
                }
            }

            editor.putInt("updates", updates);
        } else {
            updatesAdapter.swapCursor(null);
            editor.remove("updates");
        }
        editor.commit();

        ((MainActivity) getActivity()).updateBadge(sPref);
        if (getListView().getAdapter() == null)
            setListAdapter(adapter);

        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.db = new Database(Aptoide.getDb());

        updatesAdapter = new UpdatesAdapter(getActivity());

        adapter = new UpdatesSectionListAdapter(getActivity(),getLayoutInflater(savedInstanceState), updatesAdapter);

        setHasOptionsMenu(true);



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_rollback, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_rollback) {
            Intent i = new Intent(getActivity(), RollbackActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (id > 0) {
            Intent i = new Intent(getActivity(), AppViewActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BusProvider.getInstance().register(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
        getListView().setDivider(null);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        ImageLoader.getInstance().resume();
                        break;
                    default:
                        ImageLoader.getInstance().pause();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        getLoaderManager().initLoader(91, null, this);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int type = getListView().getAdapter().getItemViewType(info.position);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        switch (type){
            case 1:
                inflater.inflate(R.menu.menu_updates_context, menu);
                break;
            case 0:
                inflater.inflate(R.menu.menu_installed_context, menu);
                break;
        }

    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int i = item.getItemId();
        if (i == R.id.menu_ignore_update) {

            db.addToExcludeUpdate(info.id);
            //Toast.makeText(getActivity(), "Ignoring update...", Toast.LENGTH_LONG).show();
            refreshStoresEvent(null);
            return true;
        } else if (i == R.id.menu_discard) {
            UninstallRetainFragment uninstallRetainFragment = new UninstallRetainFragment(info.id);
            getFragmentManager().beginTransaction().add(uninstallRetainFragment, "UnistallTask").commit();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
    }
}
