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
import android.view.ViewGroup;
import android.widget.ListView;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockListFragment;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 11-11-2013
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class FragmentStoreListCategories extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, FragmentStore {

    private ArrayList<Category> categories = new ArrayList<Category>();
    private Database database;
    private CategoryAdapter arrayAdapter;
    private long storeid;
    private long parentid;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) view;

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())

                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)

                        // We need to mark the ListView and it's Empty View as pullable
                        // This is because they are not dirent children of the ViewGroup
                .theseChildrenArePullable(android.R.id.list)

                        // We can now complete the setup as desired

        .listener(this)
                .options(Options.create().scrollDistance(0.75f).build())
        .setup(mPullToRefreshLayout);

        mPullToRefreshLayout.setRefreshing(((StoreActivity)getSherlockActivity()).isRefreshing());


    }

    public void setRefreshing(boolean bool){
        mPullToRefreshLayout.setRefreshing(bool);
    }

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
                Fragment fragment = new FragmentStoreListCategories();
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

    @Override
    public void onRefreshStarted(View view) {

    }

    @Override
    public void onRefresh() {
        Bundle bundle = new Bundle();
        bundle.putLong("storeid", storeid);
        bundle.putLong("parentid", parentid);
        getLoaderManager().restartLoader(20, bundle, this);
    }

    @Override
    public void onError() {

    }
}
