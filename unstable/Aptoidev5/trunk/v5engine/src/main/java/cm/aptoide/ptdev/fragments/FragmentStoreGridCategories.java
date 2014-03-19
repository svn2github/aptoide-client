package cm.aptoide.ptdev.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.CategoryAdapter;
import cm.aptoide.ptdev.adapters.CategoryGridAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.AbcDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.AbsListViewDelegate;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStoreGridCategories extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, FragmentStore, AdapterView.OnItemClickListener {

    private Database database;
    private CategoryGridAdapter categoryAdapter;

    private PullToRefreshLayout mPullToRefreshLayout;
    private long parentId;
    private long storeId;
    private MergeAdapter mainAdapter;
    private CategoryGridAdapter apkAdapter;
    private GridView gridView;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.page_store_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        gridView = (GridView) view.findViewById(R.id.store_gridview);

        if(parentId==0){
            gridView.setAdapter(mainAdapter);
        }
        gridView.setOnItemClickListener(this);
                // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);


        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        // Here we make the refresh scroll distance to 75% of the GridView height
                        .scrollDistance(.75f)
                        .build())
                .allChildrenArePullable()
                .listener(this)
                        // Here we'll set a custom ViewDelegate
                .useViewDelegate(GridView.class, new AbsListViewDelegate())
                .setup(mPullToRefreshLayout);

    }





    StoreActivity.SortObject sort;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sort = ((StoreActivity)getActivity()).getSort();
        setRefreshing(((StoreActivity) getActivity()).isRefreshing());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mainAdapter = new MergeAdapter();
        database = new Database(Aptoide.getDb());
        categoryAdapter = new CategoryGridAdapter(getActivity());
        apkAdapter = new CategoryGridAdapter(getActivity());
        mainAdapter.addAdapter(categoryAdapter);
        mainAdapter.addAdapter(apkAdapter);
        setHasOptionsMenu(true);

        if(savedInstanceState==null){
            parentId = getArguments().getLong("parentid");
            storeId = getArguments().getLong("storeid");
        }else{
            parentId = savedInstanceState.getLong("parentid");
            storeId = savedInstanceState.getLong("storeid");
        }







        Log.d("Aptoide-", "StoreFragment id" + getArguments().getLong("storeid") + " " + storeId + " " + parentId + " " +  getArguments().getLong("parentid"));


    }
    int counter;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("storeid", storeId);
        outState.putLong("parentid", parentId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(((StoreActivity)getActivity()).isRefreshing()){
            inflater.inflate(R.menu.category_refresh, menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = new Bundle();

        bundle.putLong("storeid", storeId);
        bundle.putLong("parentid", parentId);
        getLoaderManager().restartLoader(20, bundle, this);
        getLoaderManager().restartLoader(21, bundle, this);
        setRefreshing(((StoreActivity) getActivity()).isRefreshing());


    }




    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {

                switch (id) {
                    case 20:
                        if(sort.isNoCategories()){

                            return null;
                        }
                        return database.getCategories(storeId, parentId);
                    case 21:

                        if(sort.isNoCategories()){

                            return database.getAllStoreApks(storeId, sort);
                        }

                        return database.getApks(storeId, parentId, sort);

                }
                return null;

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case 20:
                categoryAdapter.swapCursor(data);
                break;
            case 21:
                apkAdapter.swapCursor(data);
                break;
        }

        Log.d("Aptoide-StoreListCategories", "Counter is " + counter);

        if(gridView.getAdapter()==null)
            gridView.setAdapter(mainAdapter);
        setListShown(true);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        categoryAdapter.swapCursor(null);
    }

    @Override
    public void onRefreshStarted(View view) {

        ((StoreActivity)getActivity()).onRefreshStarted();

    }

    @Override
    public void onRefresh() {
        Bundle bundle = new Bundle();
        bundle.putLong("storeid", storeId);
        bundle.putLong("parentid", parentId);
        sort = ((StoreActivity)getActivity()).getSort();
        getLoaderManager().restartLoader(20, bundle, this);
        getLoaderManager().restartLoader(21, bundle, this);
    }

    @Override
    public void onError() {

    }

    @Override
    public void setRefreshing(final boolean bool) {
        final View v = getActivity().getWindow().getDecorView();

        v.post(new Runnable() {
            @Override
            public void run() {
                if (v.getWindowToken() != null) {
                    // The Decor View has a Window Token, so we can add the HeaderView!
                    mPullToRefreshLayout.setRefreshing(bool);
                } else {
                    // The Decor View doesn't have a Window Token yet, post ourselves again...
                    v.post(this);
                }
            }
        });
        onRefresh();
        getActivity().supportInvalidateOptionsMenu();
    }


    @Override
    public void setListShown(boolean b) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> l, View view, int position, long id) {
        Bundle args = getArguments();


        Log.d("Aptoide-Stores", l.getAdapter().getClass().getCanonicalName());

        int type = l.getAdapter().getItemViewType(position);

        Log.d("Aptoide-Stores", String.valueOf(type));


        switch (type) {
            case 1:
                Fragment fragment = new FragmentStoreGridCategories();
                args.putLong("storeid", storeId);
                args.putLong("parentid", id);
                fragment.setArguments(args);
                Cursor c = (Cursor) l.getAdapter().getItem(position);
                String name;
                int res = EnumCategories.getCategoryName((int) id);
                if ( res == 0) {
                    name = ((Cursor) l.getAdapter().getItem(position)).getString(0);
                } else{
                    name = getString(res);
                }
                switch ( (int) id) {
                    case EnumCategories.LATEST_LIKES:
                        fragment = new LatestLikesFragment();
                        fragment.setArguments(args);
                        break;
                    case EnumCategories.LATEST_COMMENTS:
                        fragment = new LatestCommentsFragment();
                        fragment.setArguments(args);
                        break;
                }

                getFragmentManager().beginTransaction().setBreadCrumbTitle(name).replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
//                getFragmentManager().beginTransaction().setBreadCrumbTitle(title).replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).commit();
                break;

            default:
                Intent i = new Intent(getActivity(), appViewClass);
                i.putExtra("id", id);
                startActivity(i);
                break;
        }
    }
}
