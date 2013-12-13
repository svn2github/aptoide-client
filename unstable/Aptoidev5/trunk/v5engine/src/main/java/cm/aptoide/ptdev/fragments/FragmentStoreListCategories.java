package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.CategoryAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.AbcDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;

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
public class FragmentStoreListCategories extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, FragmentStore {

    private Database database;
    private CategoryAdapter categoryAdapter;

    private PullToRefreshLayout mPullToRefreshLayout;
    private long parentId;
    private long storeId;
    private MergeAdapter mainAdapter;
    private CategoryAdapter apkAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        return super.onCreateView(inflater, container, savedInstanceState);
    }

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

                .options(Options.create().headerTransformer(new AbcDefaultHeaderTransformer()).scrollDistance(0.5f).build())
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
        categoryAdapter = new CategoryAdapter(getActivity());
        apkAdapter = new ApkAdapter(getActivity());
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


        if(parentId==0){
            setListAdapter(mainAdapter);
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



    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Bundle args = getArguments();


        Log.d("Aptoide-Stores", l.getAdapter().getClass().getCanonicalName());

        int type = l.getAdapter().getItemViewType(position);

        Log.d("Aptoide-Stores", String.valueOf(type));


        switch (type) {
            case 1:
                Fragment fragment = new FragmentStoreListCategories();
                args.putLong("storeid", storeId);
                args.putLong("parentid", id);
                fragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).commit();
                break;

            default:
                Intent i = new Intent(getActivity(), AppViewActivity.class);
                i.putExtra("id", id);
                startActivity(i);
                break;
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new SimpleCursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {

                switch (id) {
                    case 20:

                        return database.getCategories(storeId, parentId);
                    case 21:

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

        if(getListView().getAdapter()==null)
            setListAdapter(mainAdapter);


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
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
