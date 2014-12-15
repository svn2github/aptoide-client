//package cm.aptoide.ptdev.fragments;
//
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.support.v4.app.*;
//import android.support.v4.content.Loader;
//import android.support.v4.view.ViewCompat;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.util.Log;
//import android.view.*;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import cm.aptoide.ptdev.*;
//import cm.aptoide.ptdev.adapters.CategoryAdapter;
//import cm.aptoide.ptdev.database.Database;
//import cm.aptoide.ptdev.utils.SimpleCursorLoader;
//import com.commonsware.cwac.merge.MergeAdapter;
//
//
///**
// * Created with IntelliJ IDEA.
// * User: rmateus
// * Date: 11-11-2013
// * Time: 17:12
// * To change this template use File | Settings | File Templates.
// */
//public class FragmentStoreListCategories extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, FragmentStore {
//
//    private Database database;
//    private CategoryAdapter categoryAdapter;
//
//
//    private long parentId;
//    private long storeId;
//    private MergeAdapter mainAdapter;
//    private CategoryAdapter apkAdapter;
//    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();
//    private ListFragmentSwipeRefreshLayout mSwipeRefreshLayout;
//
//
//    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {
//
//        public ListFragmentSwipeRefreshLayout(Context context) {
//            super(context);
//        }
//
//        /**
//         * As mentioned above, we need to override this method to properly signal when a
//         * 'swipe-to-refresh' is possible.
//         *
//         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
//         */
//        @Override
//        public boolean canChildScrollUp() {
//            final ListView listView = getListView();
//            if (listView.getVisibility() == View.VISIBLE) {
//                return canListViewScrollUp(listView);
//            } else {
//                return false;
//            }
//        }
//
//    }
//
//    /**
//     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
//     * Handles platform version differences, providing backwards compatible functionality where
//     * needed.
//     */
//    private static boolean canListViewScrollUp(ListView listView) {
//        if (android.os.Build.VERSION.SDK_INT >= 14) {
//            // For ICS and above we can call canScrollVertically() to determine this
//            return ViewCompat.canScrollVertically(listView, -1);
//        } else {
//            // Pre-ICS we need to manually check the first visible item and the child view's top
//            // value
//            return listView.getChildCount() > 0 &&
//                    (listView.getFirstVisiblePosition() > 0
//                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Create the list fragment's content view by calling the super method
//        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);
//
//        if (storeId != -1) {
//            mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());
//
//            // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
//            // the SwipeRefreshLayout
//            mSwipeRefreshLayout.addView(listFragmentView,
//                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//            // Make sure that the SwipeRefreshLayout will fill the fragment
//            mSwipeRefreshLayout.setLayoutParams(
//                    new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT));
//            mSwipeRefreshLayout.setOnRefreshListener(this);
//
//            // Now return the SwipeRefreshLayout as this fragment's content view
//            return mSwipeRefreshLayout;
//        } else {
//            return listFragmentView;
//        }
//        // Now create a SwipeRefreshLayout to wrap the fragment's content view
//
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//
//        ViewGroup viewGroup = (ViewGroup) view;
//        setEmptyText(getString(R.string.preparing_to_load));
//
//        getListView().setDivider(null);
//        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
//        getListView().setFastScrollEnabled(true);
//        // We need to create a PullToRefreshLayout manually
//
//
//        // We can now setup the PullToRefreshLayout
//
//
//    }
//
//    StoreActivity.SortObject sort ;
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        sort = ((CategoryCallback)getActivity()).getSort();
//
//
//        if(parentId==0){
//            setListAdapter(mainAdapter);
//        }
//
//        if(storeId == -1){
//            ((StoreActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
//            ((StoreActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.all_stores));
//        }
//
//    }
//
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mainAdapter = new MergeAdapter();
//        database = new Database(Aptoide.getDb());
//        categoryAdapter = new CategoryAdapter(getActivity());
//        apkAdapter = new CategoryAdapter(getActivity());
//        mainAdapter.addAdapter(categoryAdapter);
//        mainAdapter.addAdapter(apkAdapter);
//
//        setHasOptionsMenu(true);
//
//        if(savedInstanceState==null){
//            parentId = getArguments().getLong("parentid");
//            storeId = getArguments().getLong("storeid");
//        }else{
//            parentId = savedInstanceState.getLong("parentid");
//            storeId = savedInstanceState.getLong("storeid");
//        }
//
//
//        Log.d("Aptoide-", "StoreFragment id" + getArguments().getLong("storeid") + " " + storeId + " " + parentId + " " +  getArguments().getLong("parentid"));
//
//
//    }
//    int counter;
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putLong("storeid", storeId);
//        outState.putLong("parentid", parentId);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//
//        if(getActivity() != null && ((CategoryCallback)getActivity()).isRefreshing()){
//            inflater.inflate(R.menu.category_refresh, menu);
//        }
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        Bundle bundle = new Bundle();
//
//        bundle.putLong("storeid", storeId);
//        bundle.putLong("parentid", parentId);
//        getLoaderManager().restartLoader(20, bundle, this);
//        getLoaderManager().restartLoader(21, bundle, this);
//        setRefreshing(((CategoryCallback) getActivity()).isRefreshing());
//        if( mSwipeRefreshLayout!=null) mSwipeRefreshLayout.setColorSchemeResources(R.color.default_progress_bar_color, R.color.custom_color, R.color.default_progress_bar_color, R.color.custom_color);
//
//    }
//
//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        Bundle args = getArguments();
//
//
//        Log.d("Aptoide-Stores", l.getAdapter().getClass().getCanonicalName());
//
//        int type = l.getAdapter().getItemViewType(position);
//
//        Log.d("Aptoide-Stores", String.valueOf(type));
//
//
//        switch (type) {
//            case 1:
//                Fragment fragment = new FragmentStoreListCategories();
//                args.putLong("storeid", storeId);
//                args.putLong("parentid", id);
//                fragment.setArguments(args);
//                String name;
//                int res = EnumCategories.getCategoryName((int) id);
//                if ( res == 0) {
//                    name = ((Cursor) l.getAdapter().getItem(position)).getString(0);
//                } else{
//                    name = getString(res);
//                }
//
//                switch ( (int) id) {
//                    case EnumCategories.LATEST_LIKES:
//                        fragment = new LatestLikesFragment();
//                        fragment.setArguments(args);
//                        break;
//                    case EnumCategories.LATEST_COMMENTS:
//                        fragment = new LatestCommentsFragment();
//                        fragment.setArguments(args);
//                        break;
//                }
//
//                getFragmentManager().beginTransaction().setBreadCrumbTitle(name).replace(R.id.content_layout, fragment, "fragStore").addToBackStack(String.valueOf(id)).commit();
//
//                break;
//
//
//            default:
//                Intent i = new Intent(getActivity(), appViewClass);
//                i.putExtra("id", id);
//                i.putExtra("download_from", "store_navigation");
//                startActivity(i);
//                break;
//        }
//
//
//    }
//
//    @Override
//    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
//        return new SimpleCursorLoader(getActivity()) {
//            @Override
//            public Cursor loadInBackground() {
//
//                switch (id) {
//                    case 20:
//                        if(sort.isNoCategories()){
//
//                            return null;
//                        }
//                        return database.getCategories(storeId, parentId);
//                    case 21:
//
//                        if(sort.isNoCategories()){
//
//                            return database.getAllStoreApks(storeId, sort);
//                        }
//
//                        return database.getApks(storeId, parentId, sort);
//
//                }
//                return null;
//
//            }
//        };
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        switch (loader.getId()){
//            case 20:
//                categoryAdapter.swapCursor(data);
//                break;
//            case 21:
//                apkAdapter.swapCursor(data);
//                break;
//        }
//
//        Log.d("Aptoide-StoreListCategories", "Counter is " + counter);
//        if(data==null) return;
//        if(getListView().getAdapter()==null && data.getCount()>0){
//            setListAdapter(mainAdapter);
//        }
//
//        if(data.getCount() > 0) setListShown(true);
//
//        if(new Database(Aptoide.getDb()).isStoreError(getArguments().getLong("storeid")) ){
//            setListAdapter(null);
//            setListShown(true);
//            setEmptyText(getString(R.string.connection_error));
//        }
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        categoryAdapter.swapCursor(null);
//    }
//
//    @Override
//    public void onRefresh() {
//
//        ((CategoryCallback)getActivity()).onRefreshStarted();
//
//    }
//
//    @Override
//    public void onRefreshCalled() {
//        Bundle bundle = new Bundle();
//        bundle.putLong("storeid", storeId);
//        bundle.putLong("parentid", parentId);
//        sort = ((CategoryCallback) getActivity()).getSort();
//
//        if(sort.isNoCategories()){
//            categoryAdapter.swapCursor(null);
//        }
//
//        getLoaderManager().restartLoader(20, bundle, this);
//        getLoaderManager().restartLoader(21, bundle, this);
//
//    }
//
//    @Override
//    public void onError() {
//        setEmptyText(getString(R.string.connection_error));
//    }
//
//    @Override
//    public void setRefreshing(final boolean bool) {
//
//        if(mSwipeRefreshLayout!=null) mSwipeRefreshLayout.setRefreshing(bool);
//
//        onRefreshCalled();
//        getActivity().supportInvalidateOptionsMenu();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }
//}
