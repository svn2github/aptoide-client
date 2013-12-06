package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStoreListCategories extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, FragmentStore {

    private Database database;
    private CategoryAdapter arrayAdapter;

    private PullToRefreshLayout mPullToRefreshLayout;
    private long parentId;
    private long storeId;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) view;
        setEmptyText("Please wait while store is loading.");

        getListView().setDivider(null);

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())

                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)

                        // We need to mark the ListView and it's Empty View as pullable
                        // This is because they are not dirent children of the ViewGroup
                .theseChildrenArePullable(android.R.id.list, android.R.id.empty)

                        // We can now complete the setup as desired

                .listener(this)
                .options(Options.create().scrollDistance(0.5f).headerLayout(R.layout.refresh_header).build())
                .setup(mPullToRefreshLayout);

        mPullToRefreshLayout.setRefreshing(((StoreActivity)getSherlockActivity()).isRefreshing());
    }

    public void setRefreshing(boolean bool){
        mPullToRefreshLayout.setRefreshing(bool);
        getSherlockActivity().invalidateOptionsMenu();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = new Database(Aptoide.getDb());
        arrayAdapter = new CategoryAdapter(getSherlockActivity());
        setHasOptionsMenu(true);


        if(savedInstanceState==null){
            parentId = getArguments().getLong("parentid");
            storeId = getArguments().getLong("storeid");
        }else{
            parentId = savedInstanceState.getLong("parentid");
            storeId = savedInstanceState.getLong("storeid");
        }


        if(parentId==0){
            setListAdapter(arrayAdapter);
        }
        Bundle bundle = new Bundle();

        bundle.putLong("storeid", storeId);
        bundle.putLong("parentid", parentId);

        if(savedInstanceState==null){
            getLoaderManager().restartLoader(20, bundle, this);
        }else{
            getLoaderManager().initLoader(20, bundle, this);
        }



        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("Aptoide-", "StoreFragment id" + getArguments().getLong("storeid") + " " + storeId + " " + parentId + " " +  getArguments().getLong("parentid"));


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("storeid", storeId);
        outState.putLong("parentid", parentId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(((StoreActivity)getSherlockActivity()).isRefreshing()){
            inflater.inflate(R.menu.category_refresh, menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Bundle args = getArguments();

        int type = l.getAdapter().getItemViewType(position);

        switch (type){
            case 0:
                Intent i = new Intent(getSherlockActivity(), AppViewActivity.class);
                i.putExtra("id", id);
                startActivity(i);
                break;
            case 1:
                Fragment fragment = new FragmentStoreListCategories();

                args.putLong("storeid", storeId);
                args.putLong("parentid", id);

                fragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).commit();
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
        if(getListView().getAdapter()==null)
            setListAdapter(arrayAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        arrayAdapter.swapCursor(null);
    }

    @Override
    public void onRefreshStarted(View view) {

    }

    @Override
    public void onRefresh() {
        Bundle bundle = new Bundle();
        bundle.putLong("storeid", storeId);
        bundle.putLong("parentid", parentId);
        getLoaderManager().restartLoader(20, bundle, this);
    }

    @Override
    public void onError() {

    }
}
