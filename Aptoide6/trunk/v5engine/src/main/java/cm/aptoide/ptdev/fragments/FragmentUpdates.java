//package cm.aptoide.ptdev.fragments;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.os.Build;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.v4.app.ListFragment;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.Loader;
//import android.view.*;
//import android.widget.AbsListView;
//import android.widget.AdapterView;
//import android.widget.ListView;
//
//import cm.aptoide.ptdev.*;
//import cm.aptoide.ptdev.adapters.UpdatesSectionAdapter;
//import cm.aptoide.ptdev.adapters.UpdateItem;
//import cm.aptoide.ptdev.adapters.UpdatesAdapter;
//import cm.aptoide.ptdev.database.Database;
//import cm.aptoide.ptdev.events.BusProvider;
//import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
//import cm.aptoide.ptdev.utils.SimpleCursorLoader;
//
//import com.flurry.android.FlurryAgent;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.squareup.otto.Subscribe;
//
//import java.util.ArrayList;
//
///**
// * Created with IntelliJ IDEA.
// * User: rmateus
// * Date: 28-10-2013
// * Time: 11:40
// * To change this template use File | Settings | File Templates.
// */
//public class FragmentUpdates extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
//
//    private UpdatesAdapter updatesAdapter;
//    private Database db;
//
//    private UpdatesSectionAdapter adapter;
//    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();
//
//
//
//    @Subscribe
//    public void newAppEvent(InstalledApkEvent event) {
//        refreshStoresEvent(null);
//    }
//
//    @Subscribe
//    public void removedAppEvent(UnInstalledApkEvent event) {
//        refreshStoresEvent(null);
//    }
//
//    @Subscribe
//    public void refreshStoresEvent(RepoCompleteEvent event) {
////        getLoaderManager().restartLoader(91, null, this);
//
//    }
//
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        setListShown(false);
//
//        return new SimpleCursorLoader(getActivity()) {
//            @Override
//            public Cursor loadInBackground() {
//
//
//                return db.getUpdates();
//            }
//        };
//    }
//
//    @Override
//    public synchronized void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
//
//        if(getActivity()!=null){
//
//                    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
//                    SharedPreferences.Editor editor = sPref.edit();
//                    int updates = 0;
//                    if (data.getCount() > 0) {
//
//                        //updatesAdapter.swapCursor(data);
//                        items.clear();
//                        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
//                            UpdateItem item = new UpdateItem();
//
//                            if(data.getInt(data.getColumnIndex("is_update"))==1){
//                                item.setUpdate(true);
//                                item.setVersionName(data.getString(data.getColumnIndex("version_name")));
//                                updates++;
//                            }else{
//                                item.setVersionName(data.getString(data.getColumnIndex("installed_version_name")));
//                            }
//
//                            item.setIsSignatureValid(data.getInt(data.getColumnIndex("signature_valid"))==1);
//
//                            String iconPath = data.getString(data.getColumnIndex("iconpath"));
//                            String icon = data.getString(data.getColumnIndex("icon"));
//                            item.setName(data.getString(data.getColumnIndex("name")));
//                            item.setPackageName(data.getString(data.getColumnIndex("package_name")));
//                            item.setRepoName(data.getString(data.getColumnIndex("repo_name")));
//
//
//                            item.setIcon(iconPath+icon);
//                            item.setId(data.getLong(data.getColumnIndex("_id")));
//                            items.add(item);
//
//                        }
//
//                        editor.putInt("updates", updates);
//                    } else {
//
//                        items.clear();
//                        //updatesAdapter.swapCursor(null);
//
//                        editor.remove("updates");
//                    }
//
//                    editor.commit();
//
//                    adapter.notifyDataSetChanged();
//
//                    if(getActivity()!=null){
//                        ((Start) getActivity()).updateBadge(sPref);
//                    }
//                    if (getListView().getAdapter() == null)
//                        setListAdapter(adapter);
//
//                    setListShown(true);
//                }
//
//
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        items.clear();
//        //adapter.notifyDataSetChanged();
//    }
//
//
//    ArrayList<UpdateItem> items = new ArrayList<UpdateItem>();
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        this.db = new Database(Aptoide.getDb());
//
//        updatesAdapter = new UpdatesAdapter(getActivity(), items);
//
//        adapter = new UpdatesSectionAdapter(getActivity(),updatesAdapter);
//
//        setHasOptionsMenu(true);
//
//
//
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_rollback, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        if (id == R.id.menu_rollback) {
//            FlurryAgent.logEvent("Updates_Page_Clicked_On_Rollback_Button");
//            Intent i = new Intent(getActivity(), RollbackActivity.class);
//            startActivity(i);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        if (id > 0) {
//            Intent i = new Intent(getActivity(), appViewClass);
//
//            UpdateItem item = (UpdateItem) l.getAdapter().getItem(position);
//
//            i.putExtra("packageName", item.getPackageName());
//            i.putExtra("repoName", item.getRepoName());
//
//            i.putExtra("id", id);
//            i.putExtra("download_from", "updates");
//            startActivity(i);
//        }
//
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        BusProvider.getInstance().register(this);
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        getLoaderManager().destroyLoader(91);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        BusProvider.getInstance().unregister(this);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
//        getListView().setDivider(null);
//
//        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                switch (scrollState) {
//                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//                        ImageLoader.getInstance().resume();
//                        break;
//                    default:
//                        ImageLoader.getInstance().pause();
//                        break;
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//            }
//        });
//
//        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              getLoaderManager().initLoader(91, null, this);
//
//    }
//
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        //super.onCreateContextMenu(menu, v, menuInfo);
//
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//
//
//
//        Object type =  getListView().getAdapter().getItem(info.position);
//        MenuInflater inflater = this.getActivity().getMenuInflater();
//
//        if(type instanceof UpdateItem){
//            if(((UpdateItem)type).isUpdate()){
//                inflater.inflate(R.menu.menu_updates_context, menu);
//            }else{
//                inflater.inflate(R.menu.menu_installed_context, menu);
//            }
//        }
//
//    }
//
//
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        int i = item.getItemId();
//        if (i == R.id.menu_ignore_update) {
//            FlurryAgent.logEvent("Updates_Page_Clicked_On_Ignore_Update_Context_Menu");
//            db.addToExcludeUpdate(info.id);
//            //Toast.makeText(getActivity(), "Ignoring update...", Toast.LENGTH_LONG).show();
//            refreshStoresEvent(null);
//            return true;
//        } else if (i == R.id.menu_discard) {
//            FlurryAgent.logEvent("Updates_Page_Clicked_On_Uninstall_Context_Menu");
//            UninstallRetainFragment uninstallRetainFragment = new UninstallRetainFragment();
//            Bundle arg = new Bundle(  );
//            arg.putLong( "id", info.id );
//            uninstallRetainFragment.setArguments( arg );
//            getFragmentManager().beginTransaction().add(uninstallRetainFragment, "UnistallTask").commit();
//            return true;
//        } else {
//            return super.onContextItemSelected(item);
//        }
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        registerForContextMenu(getListView());
//    }
//}
