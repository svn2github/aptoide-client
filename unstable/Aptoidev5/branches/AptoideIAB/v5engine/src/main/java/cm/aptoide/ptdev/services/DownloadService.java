package cm.aptoide.ptdev.services;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import android.util.SparseArray;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.*;
import cm.aptoide.ptdev.downloadmanager.state.ActiveState;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromMd5;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Subscribe;

import org.w3c.dom.NamedNodeMap;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Collection;

/**
 * Created by rmateus on 11-12-2013.
 */
public class DownloadService extends Service{

    private DownloadManager manager = new DownloadManager();
    private Timer timer;
    private boolean isStopped = true;
    //private NotificationCompat.Builder mBuilder;
    private LongSparseArray<DownloadInfo> downloads = new LongSparseArray<DownloadInfo>();
    private NotificationCompat.Builder mBuilder;


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
            // updateProgress(mBuilder, ongoingDownloads);
            updateProgress();
        } else {
            timer.cancel();
            timer.purge();
            stopSelf();
            mBuilder = null;
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

        NotificationCompat.Builder builder = setNotification(id);
        DownloadInfo inf = getDownload(id);
        Log.d("download-trace", "setmbuilder: startExistingDownload");

        if(mBuilder==null) mBuilder = createDefaultNotification();
        startForeground(-3, mBuilder.build());


        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }

        Log.d("Aptoide-DownloadManager", "Starting existing download " + id);
        for(final DownloadInfo info: manager.getmCompletedList()){
            if(info.getId()==id){
                final PackageManager packageManager = getPackageManager();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(DownloadModel model : info.getmFilesToDownload()) {
                            try {
                                PackageInfo packageInfo = packageManager.getPackageInfo(info.getDownload().getPackageName(), PackageManager.SIGNATURE_MATCH);
                                if(packageInfo.versionName.equals(info.getDownload().getVersion())){
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent LaunchIntent = packageManager.getLaunchIntentForPackage(info.getDownload().getPackageName());
                                            if(LaunchIntent != null) startActivity(LaunchIntent);
                                        }
                                    });
                                }else{
                                    throw new PackageManager.NameNotFoundException();
                                }

                            } catch (PackageManager.NameNotFoundException e) {

                                String calculatedMd5 = AptoideUtils.Algorithms.md5Calc(new File(model.getDestination()));
                                if(!calculatedMd5.equals(info.getDownload().getMd5())){
                                    Log.d("download-trace", "Failed Md5 for " + info.getDownload().getName() + " : " + info.getDestination() + "   calculated " + calculatedMd5 + " vs " + info.getDownload().getMd5());
                                    info.download();
                                    break;
                                } else {
                                    info.autoExecute();
                                    Log.d("download-trace", "Checked Md5 for " + info.getDownload().getName() + ", application download it's already completed!");
                                    break;
                                }
                            }
                        }
                    }
                }).start();
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


    public void startDownloadFromUrl(String remotePath, String md5, long id, Download download, String repoName){
        ArrayList<DownloadModel> filesToDownload = new ArrayList<DownloadModel>();

        String path = Aptoide.getConfiguration().getPathCacheApks();

        DownloadModel downloadModel = new DownloadModel(remotePath, path + md5 + ".apk", md5, 0);

        downloadModel.setAutoExecute(true);
        filesToDownload.add(downloadModel);
        DownloadInfo info = getDownload(id);
        FinishedApk apk = new FinishedApk(download.getName(), download.getPackageName(), download.getVersion(), id, download.getIcon(), path + md5 + ".apk", new ArrayList<String>());
        apk.setRepoName(repoName);
        info.setDownloadExecutor(new DownloadExecutorImpl(apk));
        info.setDownload(download);
        info.setFilesToDownload(filesToDownload);

        downloads.put(info.getId(), info);
        info.download();

        startService(new Intent(getApplicationContext(), DownloadService.class));


        if(mBuilder==null) mBuilder = createDefaultNotification();
        startForeground(-3, mBuilder.build());

        Log.d("donload-trace", "setmBuilder: startDownloadFromUrl");
        NotificationCompat.Builder builder = setNotification(id);
        info.setmBuilder(builder);


        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }

    }



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

        if(json.getApk().getMd5sum()!=null){
            download.setId(json.getApk().getMd5sum().hashCode());
        }

        DownloadModel downloadModel = new DownloadModel(json.getApk().getPath(), path + json.getApk().getMd5sum() + ".apk", json.getApk().getMd5sum(), json.getApk().getSize().longValue());
        downloadModel.setAutoExecute(true);
        downloadModel.setFallbackUrl(json.getApk().getAltPath());
        filesToDownload.add(downloadModel);
        DownloadInfo info = getDownload(id);
        FinishedApk apk = new FinishedApk(download.getName(), download.getPackageName(), download.getVersion(), id, download.getIcon(), path + json.getApk().getMd5sum() + ".apk", new ArrayList<String>(json.getApk().getPermissions()));
        apk.setRepoName(json.getApk().getRepo());
        info.setDownloadExecutor(new DownloadExecutorImpl(apk));
        info.setDownload(download);
        info.setFilesToDownload(filesToDownload);

        downloads.put(info.getId(), info);
        info.download();

        Log.d("download-trace", "setmBuilder: startDownloadFromJson");
        NotificationCompat.Builder builder = setNotification(info.getId());
        info.setmBuilder(builder);

        if(mBuilder==null) mBuilder = createDefaultNotification();

        startForeground(-3, mBuilder.build());
        startService(new Intent(getApplicationContext(), DownloadService.class));



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
        info.remove(true);

    }



    public void resumeDownload(int downloadId) {
        startService(new Intent(getApplicationContext(), DownloadService.class));

        if(mBuilder==null) mBuilder = createDefaultNotification();
        startForeground(-3, mBuilder.build());

        Log.d("donwload-trace", "setmBuilder: resumeDownload");
        DownloadInfo info = getDownload(downloadId);
        NotificationCompat.Builder builder = setNotification(downloadId);
        info.setmBuilder(builder);

        info.download();

        if(isStopped){
            isStopped = false;
            timer = new Timer();
            timer.schedule(getTask(), 0, 1000);
        }


    }

    public void removeNonActiveDownloads(boolean isChecked) {
        ArrayList<DownloadInfo> allDownloads = new ArrayList<DownloadInfo>();
        allDownloads.addAll(manager.getmErrorList());
        allDownloads.addAll(manager.getmCompletedList());

        for(DownloadInfo downloadInfo : allDownloads){
            downloadInfo.remove(isChecked);
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
            if (getApkInfoJson != null) {
                if (getApkInfoJson.getStatus().equals("OK")) {
                    startDownloadFromJson(getApkInfoJson, id, download);
                } else {
                    final HashMap<String, Integer> errorsMapConversion = Errors.getErrorsMap();
                    Integer stringId;
                    String message;
                    for (Error error : getApkInfoJson.getErrors()) {
                        stringId = errorsMapConversion.get( error.getCode() );
                        if(stringId != null) {
                            message = getString( stringId );
                        } else {
                            message = error.getMsg();
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


    public void startDownloadFromAppId(final long id){
        startService(new Intent(getApplicationContext(), DownloadService.class));

        if(mBuilder==null) mBuilder = createDefaultNotification();
        startForeground(-3, mBuilder.build());
        /*DownloadInfo info = getDownload(id);
        Log.d("donwload-trace", "setMbuilder: startDownloadFromAppId");
        NotificationCompat.Builder builder = setNotification(id);
        info.setmBuilder(builder);
        info.setCompleteCallback(this);
*/
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
                    final int versionCode = apkCursor.getInt(apkCursor.getColumnIndex("version_code"));
                    final String md5sum = apkCursor.getString(apkCursor.getColumnIndex("md5"));
                    String icon = apkCursor.getString(apkCursor.getColumnIndex("icon"));
                    final String iconpath = apkCursor.getString(apkCursor.getColumnIndex("iconpath"));


                    GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

                    request.setRepoName(repoName);
                    request.setPackageName(package_name);
                    request.setVersionName(versionName);
                    request.setVercode(versionCode);


                    Download download = new Download();
                    download.setId(md5sum.hashCode());
                    download.setName(name);
                    download.setPackageName(package_name);
                    download.setVersion(versionName);
                    download.setMd5(md5sum);


                    if (icon.contains("_icon")) {
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                    }

                    download.setIcon(iconpath + icon);

                    manager.getFromCacheAndLoadFromNetworkIfExpired(request, repoName + md5sum, DurationInMillis.ONE_HOUR, new DownloadRequest(download.getId(), download));
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

    private NotificationCompat.Builder createDefaultNotification() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent onClick = new Intent();
        onClick.setClassName(getPackageName(), Aptoide.getConfiguration().getStartActivityClass().getName());
        onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        onClick.setAction(Intent.ACTION_VIEW);
        onClick.putExtra("fromDownloadNotification", true);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent onClickAction = PendingIntent.getActivity(getApplicationContext(), 0, onClick, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getString(R.string.aptoide_downloading, Aptoide.getConfiguration().getMarketName()))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true)
                .setContentIntent(onClickAction);
        mBuilder.setProgress(100, 0, true);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(-3, mBuilder.build());

        return mBuilder;
    }





    private NotificationCompat.Builder setNotification(final long id) {

        DownloadInfo info = getDownload(id);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent onClick = new Intent();
        onClick.setClassName(getPackageName(), Aptoide.getConfiguration().getStartActivityClass().getName());
        onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        onClick.setAction(Intent.ACTION_VIEW);
        onClick.putExtra("fromDownloadNotification", true);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent onClickAction = PendingIntent.getActivity(getApplicationContext(), 0, onClick, PendingIntent.FLAG_UPDATE_CURRENT);

        int size = DownloadExecutorImpl.dpToPixels(getApplicationContext(), 36);

        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getString(R.string.aptoide_downloading, Aptoide.getConfiguration().getMarketName()))
                .setContentText(info.getDownload().getName())
                .setLargeIcon(DownloadExecutorImpl.decodeSampledBitmapFromResource(ImageLoader.getInstance().getDiscCache().get(info.getDownload().getIcon()).getAbsolutePath(), size, size))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true)
                .setContentIntent(onClickAction);
        Log.d("download-trace", "ETA: " + info.getEta());
        if(info.getEta() > 0) {
            String remaining = Utils.formatEta(info.getEta(), "");
            mBuilder.setContentInfo("ETA: " + (!remaining.equals("") ? remaining : "0s"));
        }

        return mBuilder;
    }

    private void updateProgress() {
        Collection<DownloadInfo> list = getOngoingDownloads();
        list.addAll(manager.getmCompletedList());

        for(DownloadInfo info : list) {
            if(info.getStatusState() instanceof ActiveState) {
                info.getmBuilder().setProgress(100, info.getPercentDownloaded(), info.getPercentDownloaded() == 0);
                if(info.getEta() > 0) {
                    String remaining = Utils.formatEta(info.getEta(), "");
                    info.getmBuilder().setContentInfo("ETA: " + (!remaining.equals("") ? remaining : "0s"));
                }

                mBuilder = info.getmBuilder();
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(-3, mBuilder.build());
                return;
            }
        }
    }
}
