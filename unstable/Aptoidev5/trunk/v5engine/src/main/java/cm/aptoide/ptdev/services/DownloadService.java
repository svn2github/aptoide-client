package cm.aptoide.ptdev.services;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.downloadmanager.*;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rmateus on 11-12-2013.
 */
public class DownloadService extends Service {

    DownloadManager manager = new DownloadManager();
    private Timer timer;
    private boolean isStopped = true;
    private NotificationCompat.Builder mBuilder;
    private LongSparseArray<DownloadInfo> downloads = new LongSparseArray<DownloadInfo>();


    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }


    public void updateDownload() {
        ArrayList<DownloadInfo> ongoingDownloads = getOngoingDownloads();
        if (!ongoingDownloads.isEmpty()) {
            updateProgress(mBuilder, ongoingDownloads);
        } else {
            timer.cancel();
            timer.purge();
            stopSelf();
            stopForeground(true);
            isStopped = true;
        }

    }

    public DownloadInfo getDownload(long id){

        if(downloads.get(id)!=null){
            return downloads.get(id);
        }else{
            DownloadInfo downloadInfo = new DownloadInfo(manager, id);
            return downloadInfo;
        }

    }

    public void startDownloadFromJson(GetApkInfoJson json, long id, Download download){

        ArrayList<DownloadModel> filesToDownload = new ArrayList<DownloadModel>();

        String path = Aptoide.getConfiguration().getPathCacheApks();
        DownloadModel downloadModel = new DownloadModel(json.getApk().getPath(), path + json.getApk().getMd5sum() + ".apk", json.getApk().getMd5sum(), json.getApk().getSize().longValue());
        downloadModel.setAutoExecute(true);
        filesToDownload.add(downloadModel);
        DownloadInfo info = getDownload(id);

        info.setDownloadExecutor(new DownloadExecutorImpl(new FinishedApk(download.getName(), download.getPackageName(),id, download.getIcon(), path + json.getApk().getMd5sum() + ".apk")));
        info.setDownload(download);
        info.setFilesToDownload(filesToDownload);

        downloads.put(info.getId(), info);
        info.download();

        startService(new Intent(getApplicationContext(), DownloadService.class));
        mBuilder = setNotification();
        startForeground(-3, mBuilder.build());

        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }

    }

    private TimerTask getTask() {
        return new TimerTask(){

            @Override
            public void run() {
                updateDownload();
                Log.d("Aptoide-DownloadService", "Updating progress bar");
            }
        };
    }

    public ArrayList<DownloadInfo> getOngoingDownloads() {

        ArrayList<DownloadInfo> ongoingDownloads = new ArrayList<DownloadInfo>();

        ongoingDownloads.addAll(manager.getmActiveList());

        return ongoingDownloads;
    }

    public ArrayList<Download> getAllActiveDownloads(){

        ArrayList<DownloadInfo> allDownloads = getOngoingDownloads();

        ArrayList<Download> allDownloads2 = new ArrayList<Download>();
        for(DownloadInfo info: allDownloads){
            allDownloads2.add(info.getDownload());
        }

        return allDownloads2;
    }

    public ArrayList<Download> getAllNotActiveDownloads(){

        ArrayList<DownloadInfo> allDownloads = new ArrayList<DownloadInfo>();
        allDownloads.addAll(manager.getmErrorList());
        allDownloads.addAll(manager.getmCompletedList());

        ArrayList<Download> allDownloads2 = new ArrayList<Download>();
        for(DownloadInfo info: allDownloads){
            allDownloads2.add(info.getDownload());
        }

        return allDownloads2;
    }

    public void stopDownload(long id) {

        DownloadInfo info = getDownload(id);

        info.remove();

    }

    public class LocalBinder extends Binder {

        public DownloadService getService(){
            return DownloadService.this;
        }

    }

    private NotificationCompat.Builder setNotification() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent onClick = new Intent();
        onClick.setClassName(getPackageName(), getApplicationContext().getPackageName()+".DownloadManager");
        onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        onClick.setAction("");
        onClick.putExtra("", "");

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent onClickAction = PendingIntent.getActivity(getApplicationContext(), 0, onClick, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getString(R.string.aptoide_downloading, Aptoide.getConfiguration().getMarketName()))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true)
                .setContentIntent(onClickAction);
        updateProgress(mBuilder, getOngoingDownloads());
        return mBuilder;
    }

    private void updateProgress(NotificationCompat.Builder mBuilder, ArrayList<DownloadInfo> ongoingDownloads) {
        int percentage = getOngoingDownloadsPercentage();
        mBuilder.setProgress(100, percentage, percentage == 0);
        mBuilder.setContentText(getString(R.string.x_app, ongoingDownloads.size()));

        // Displays the progress bar for the first time
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(-3, mBuilder.build());
    }

    private int getOngoingDownloadsPercentage() {

        Collection<DownloadInfo> list = getOngoingDownloads();
        list.addAll(manager.getmCompletedList());

        int progressPercentage = 0;
        for(DownloadInfo info : list){
            progressPercentage = progressPercentage + info.getPercentDownloaded();
        }

        if(!list.isEmpty()){
            return progressPercentage / list.size();
        }

        return 0;
    }
}
