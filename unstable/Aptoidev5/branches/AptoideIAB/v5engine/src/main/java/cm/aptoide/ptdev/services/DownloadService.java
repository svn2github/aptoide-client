package cm.aptoide.ptdev.services;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import android.util.SparseArray;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.*;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rmateus on 11-12-2013.
 */
public class DownloadService extends Service {

    private DownloadManager manager = new DownloadManager();
    private Timer timer;
    private boolean isStopped = true;
    private NotificationCompat.Builder mBuilder;
    private LongSparseArray<DownloadInfo> downloads = new LongSparseArray<DownloadInfo>();


    @Override
    public void onCreate() {
        super.onCreate();
        String file = getCacheDir().getAbsolutePath();
        File fileToCheck = new File(file+"/downloadManager");

            try{
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileToCheck));
                manager = (DownloadManager) in.readObject();
                in.close();

                for(DownloadInfo info: manager.getmErrorList()){
                    downloads.put(info.getId(), info);
                }

                for(DownloadInfo info: manager.getmActiveList()){
                    downloads.put(info.getId(), info);
                }

                for(DownloadInfo info: manager.getmCompletedList()){
                    downloads.put(info.getId(), info);
                }

                for(DownloadInfo info: manager.getmInactiveList()){
                    downloads.put(info.getId(), info);
                }

                for(DownloadInfo info: manager.getmPendingList()){
                    downloads.put(info.getId(), info);
                }


            }catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }



    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            String file = getCacheDir().getAbsolutePath();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file + "/downloadManager"));
            out.writeObject(manager);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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





    public void startExistingDownload(long id){


        startService(new Intent(getApplicationContext(), DownloadService.class));
        mBuilder = setNotification();
        startForeground(-3, mBuilder.build());

        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }

        Log.d("Aptoide-DownloadManager", "Starting existing download " + id);
        for(DownloadInfo info: manager.getmCompletedList()){
            if(info.getId()==id){
                info.download();
                return;
            }
        }

        for(DownloadInfo info: manager.getmErrorList()){
            if(info.getId()==id){
                info.download();
                return;
            }
        }



    }

    private static final String OBB_DESTINATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/";

    public void startDownloadFromJson(GetApkInfoJson json, long id, Download download){

        ArrayList<DownloadModel> filesToDownload = new ArrayList<DownloadModel>();

        if(json.getObb()!=null){
            DownloadModel mainObbDownload = new DownloadModel(json.getObb().getMain().getPath(), OBB_DESTINATION + download.getPackageName() + "/" +json.getObb().getMain().getFilename(), json.getObb().getMain().getMd5sum(), json.getObb().getMain().getFilesize().longValue());
            filesToDownload.add(mainObbDownload);
            if(json.getObb().getPatch()!=null){
                DownloadModel patchObbDownload = new DownloadModel(json.getObb().getPatch().getPath(), OBB_DESTINATION + download.getPackageName() + "/" +json.getObb().getPatch().getFilename(), json.getObb().getPatch().getMd5sum(), json.getObb().getPatch().getFilesize().longValue());
                filesToDownload.add(patchObbDownload);
            }
        }

        String path = Aptoide.getConfiguration().getPathCacheApks();



        DownloadModel downloadModel = new DownloadModel(json.getApk().getPath(), path + json.getApk().getMd5sum() + ".apk", json.getApk().getMd5sum(), json.getApk().getSize().longValue());
        downloadModel.setAutoExecute(true);
        filesToDownload.add(downloadModel);
        DownloadInfo info = getDownload(id);

        info.setDownloadExecutor(new DownloadExecutorImpl(new FinishedApk(download.getName(), download.getPackageName(), download.getVersion(), id, download.getIcon(), path + json.getApk().getMd5sum() + ".apk", new ArrayList<String>(json.getApk().getPermissions()))));
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
        ongoingDownloads.addAll(manager.getmPendingList());


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

    public void resumeDownload(int downloadId) {
        startService(new Intent(getApplicationContext(), DownloadService.class));
        mBuilder = setNotification();
        startForeground(-3, mBuilder.build());

        DownloadInfo info = getDownload(downloadId);
        info.download();

        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }


    }

    public void removeNonActiveDownloads() {
        ArrayList<DownloadInfo> allDownloads = new ArrayList<DownloadInfo>();
        allDownloads.addAll(manager.getmErrorList());
        allDownloads.addAll(manager.getmCompletedList());

        for(DownloadInfo downloadInfo : allDownloads){
            downloadInfo.remove();
        }

    }

    public class DownloadRequest implements RequestListener<GetApkInfoJson> {

        private final long id;
        private final Download download;

        public DownloadRequest(long id, Download download) {

            this.id = id;
            this.download = download;
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            stopSelf();
            stopForeground(true);
            isStopped = true;
        }

        @Override
        public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {

            startDownloadFromJson(getApkInfoJson, id, download);

        }
    }


    public void startDownloadFromAppId(final long id){

        startService(new Intent(getApplicationContext(), DownloadService.class));
        mBuilder = setNotification();
        startForeground(-3, mBuilder.build());

        final SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
        if(!manager.isStarted()) manager.start(getApplicationContext());
        final String sizeString = IconSizes.generateSizeString(getApplicationContext());


        new Thread(new Runnable() {
            @Override
            public void run() {

                Cursor apkCursor = new Database(Aptoide.getDb()).getApkInfo(id);

                if(apkCursor.moveToFirst()){


                    String repoName = apkCursor.getString(apkCursor.getColumnIndex("reponame"));
                    final String name = apkCursor.getString(apkCursor.getColumnIndex("name"));
                    String package_name = apkCursor.getString(apkCursor.getColumnIndex("package_name"));
                    final String versionName = apkCursor.getString(apkCursor.getColumnIndex("version_name"));
                    String icon = apkCursor.getString(apkCursor.getColumnIndex("icon"));
                    final String iconpath = apkCursor.getString(apkCursor.getColumnIndex("iconpath"));


                    GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

                    request.setRepoName(repoName);
                    request.setPackageName(package_name);
                    request.setVersionName(versionName);

                    Download download = new Download();
                    download.setId(id);
                    download.setName(name);
                    download.setPackageName(package_name);
                    download.setVersion(versionName);

                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                    }

                    download.setIcon(iconpath + icon);

                    manager.getFromCacheAndLoadFromNetworkIfExpired(request, package_name + repoName, DurationInMillis.ONE_HOUR, new DownloadRequest(id, download));
                    apkCursor.close();
                }


            }
        }).start();




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
