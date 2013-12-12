package cm.aptoide.ptdev.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import cm.aptoide.ptdev.adapters.NotOngoingAdapter;
import cm.aptoide.ptdev.adapters.OngoingAdapter;
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


    DownloadManagerCallback callback;
    DownloadService service;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((DownloadService.LocalBinder)binder).getService();


            ongoingList = new ArrayList<Download>();
            ongoingList.addAll(service.getAllActiveDownloads());
            notOngoingList = new ArrayList<Download>();
            notOngoingList.addAll(service.getAllNotActiveDownloads());
            adapter = new MergeAdapter();

            ongoingAdapter = new OngoingAdapter(getActivity(), ongoingList);
            notOngoingAdapter = new NotOngoingAdapter(getActivity(), notOngoingList);




            adapter.addAdapter(ongoingAdapter);


            adapter.addAdapter(notOngoingAdapter);

            setListAdapter(adapter);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (DownloadManagerCallback) activity;
        activity.bindService(new Intent(activity, DownloadService.class), conn, activity.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
        getActivity().unbindService(conn);
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
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
    ArrayList<Download> notOngoingList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Subscribe
    public void onDownloadStatus(DownloadStatusEvent event){

        ongoingList.clear();
        notOngoingList.clear();

        ongoingList.addAll(service.getAllActiveDownloads());
        notOngoingList.addAll(service.getAllNotActiveDownloads());

        ongoingAdapter.notifyDataSetChanged();
        notOngoingAdapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
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
}
