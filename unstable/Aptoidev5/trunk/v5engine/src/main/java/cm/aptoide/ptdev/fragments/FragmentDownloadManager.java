package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import cm.aptoide.ptdev.DownloadServiceConnected;
import cm.aptoide.ptdev.MainActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.DownloadManagerSectionAdapter;
import cm.aptoide.ptdev.adapters.NotOngoingAdapter;
import cm.aptoide.ptdev.adapters.OngoingAdapter;
import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.DownloadManagerCallback;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;
import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentDownloadManager extends ListFragment {


    MainActivity callback;
    DownloadService service;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (MainActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;

    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initAdapters(null);
    }

    @Subscribe
    public void initAdapters(DownloadServiceConnected event) {

        service = callback.getDownloadService();

        if(service!=null){
            ongoingList = new ArrayList<Download>();
            ongoingList.addAll(service.getAllActiveDownloads());
            notOngoingList = new ArrayList<Download>();
            notOngoingList.addAll(service.getAllNotActiveDownloads());
            adapter = new MergeAdapter();

            ongoingAdapter = new OngoingAdapter(getActivity(), ongoingList);
            notOngoingAdapter = new NotOngoingAdapter(getActivity(), notOngoingList);

            adapter.addAdapter(ongoingAdapter);
            adapter.addAdapter(notOngoingAdapter);

            DownloadManagerSectionAdapter adapterDownloads = new DownloadManagerSectionAdapter(getActivity(), LayoutInflater.from(getActivity()), adapter);

            setListAdapter(adapterDownloads);
            getActivity().supportInvalidateOptionsMenu();

        }


    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    MergeAdapter adapter;

    OngoingAdapter ongoingAdapter;
    NotOngoingAdapter notOngoingAdapter;
    ArrayList<Download> ongoingList;
    ArrayList<Download> notOngoingList = new ArrayList<Download>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_download_manager, menu);
        if(!notOngoingList.isEmpty()){
            menu.findItem(R.id.menu_clear_downloads).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_clear_downloads){
            service.removeNonActiveDownloads();
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.installAppFromManager(id);
    }

    @Subscribe
    public void onDownloadStatus(DownloadEvent event){

        ongoingList.clear();
        notOngoingList.clear();

        ongoingList.addAll(service.getAllActiveDownloads());
        notOngoingList.addAll(service.getAllNotActiveDownloads());

        ongoingAdapter.notifyDataSetChanged();
        notOngoingAdapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
        getActivity().supportInvalidateOptionsMenu();
        Log.d("Aptoide-DownloadManager", "On Download Status");

    }



    @Subscribe
    public void onDownloadUpdate(Download download){
        Log.d("Aptoide-DownloadManager", "onDownloadUpdate " + download.getId());

        try{
            int start = getListView().getFirstVisiblePosition();
            for (int i = start, j = getListView().getLastVisiblePosition(); i <= j; i++){
                if (download.equals((getListView().getItemAtPosition(i)))) {
                    View view = getListView().getChildAt(i - start);
                    getListView().getAdapter().getView(i, view, getListView());
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);

    }
}
