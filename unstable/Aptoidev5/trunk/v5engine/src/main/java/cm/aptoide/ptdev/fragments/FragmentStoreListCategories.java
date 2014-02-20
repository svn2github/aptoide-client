package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.adapters.CategoryAdapter;
import cm.aptoide.ptdev.adapters.ListSocialAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.AbcDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
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
        setEmptyText(getString(R.string.preparing_to_load));

        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));

        // We need to create a PullToRefreshLayout manually


        // We can now setup the PullToRefreshLayout
        if (storeId > 0) {
            mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());


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
        apkAdapter = new CategoryAdapter(getActivity());
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


            if(storeId>0){

                ArrayList<ListSocialAdapter.SocialObject> objects = new ArrayList<ListSocialAdapter.SocialObject>();


                ListSocialAdapter.SocialObject likes = new ListSocialAdapter.SocialObject();
                likes.id = EnumCategories.LATEST_LIKES;
                likes.name = getString(R.string.latest_likes);


                ListSocialAdapter.SocialObject comments = new ListSocialAdapter.SocialObject();
                comments.name = getString(R.string.latest_comments);
                comments.id = EnumCategories.LATEST_COMMENTS;


                objects.add(likes);
                objects.add(comments);
                ListSocialAdapter socialAdapter = new ListSocialAdapter(getActivity(), 0, objects);
                mainAdapter.addAdapter(socialAdapter);
                Log.d("Aptoide-StoreList", "adding social adapter");
            }else{
                ((StoreActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.all_stores));
            }

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

                getFragmentManager().beginTransaction().setBreadCrumbTitle(EnumCategories.getCategoryName((int) id)).replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                break;

            case 4:
                Bundle bundle = new Bundle();
                bundle.putLong("storeid", storeId);
                switch ( (int) id) {

                    case EnumCategories.LATEST_LIKES:
                        Fragment likes = new LatestLikesFragment();
                        likes.setArguments(bundle);
                        getFragmentManager().beginTransaction().setBreadCrumbTitle(EnumCategories.getCategoryName((int) id)).replace(R.id.content_layout, likes, "fragStore").addToBackStack(String.valueOf(id)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                        break;
                    case EnumCategories.LATEST_COMMENTS:
                        Fragment comments = new LatestCommentsFragment();
                        comments.setArguments(bundle);
                        getFragmentManager().beginTransaction().setBreadCrumbTitle(EnumCategories.getCategoryName((int) id)).replace(R.id.content_layout, comments, "fragStore").addToBackStack(String.valueOf(id)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                        break;
                }


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
        if(data==null) return;
        if(getListView().getAdapter()==null && data.getCount()>0)
            setListAdapter(mainAdapter);

        if(data.getCount() > 0) setListShown(true);

        if(new Database(Aptoide.getDb()).isStoreError(getArguments().getLong("storeid")) ){
            setListAdapter(null);
            setListShown(true);
            setEmptyText(getString(R.string.connection_error));
        }

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
        sort = ((StoreActivity) getActivity()).getSort();

        if(sort.isNoCategories()){
            categoryAdapter.swapCursor(null);
        }



        getLoaderManager().restartLoader(20, bundle, this);
        getLoaderManager().restartLoader(21, bundle, this);

    }

    @Override
    public void onError() {

        setEmptyText(getString(R.string.connection_error));
    }

    @Override
    public void setRefreshing(final boolean bool) {

        if(mPullToRefreshLayout!=null){
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
        }

        onRefresh();
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
