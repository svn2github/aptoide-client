package cm.aptoide.pt.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import cm.aptoide.pt.*;
import cm.aptoide.pt.download.*;
import cm.aptoide.pt.download.DownloadManager;
import cm.aptoide.pt.download.event.DownloadStatusEvent;
import cm.aptoide.pt.download.state.EnumState;
import cm.aptoide.pt.events.BusProvider;
import cm.aptoide.pt.util.Constants;
import cm.aptoide.pt.views.ViewApk;
import cm.aptoide.pt.webservices.WebserviceGetApkInfo;
import com.squareup.otto.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-07-2013
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */
public class ServiceManagerDownload extends Service {
    private static final String DEFAULT_APK_DESTINATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.aptoide/apks/";
    private static final String OBB_DESTINATION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/obb/";
    private NotificationManager managerNotification;
    private Collection<DownloadInfo> ongoingDownloads;
    private boolean showNotification = false;
    private NotificationCompat.Builder mBuilder;

    private BroadcastReceiver autoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor updates = Database.getInstance().getUpdates(Order.DATE);

            for(updates.moveToFirst();!updates.isAfterLast();updates.moveToNext()){
                new GetApkWebserviceInfo(context, ServiceManagerDownload.this, false).execute(updates.getLong(updates.getColumnIndex("_id")));
            }

            updates.close();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(autoUpdateReceiver, new IntentFilter("auto_update"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();  //To change body of implemented methods use File | Settings | File Templates.
    }

    HashMap<Integer, DownloadInfo> downloads = new HashMap<Integer, DownloadInfo>();

    public Collection<DownloadInfo> getDownloads() {
        return downloads.values();
    }

    public ArrayList<DownloadInfo> getNotOngoingDownloads() {
        ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();

        for(DownloadInfo downloadInfo: getDownloads()){
            if(downloadInfo.getStatusState().getEnumState().equals(EnumState.COMPLETE) || downloadInfo.getStatusState().getEnumState().equals(EnumState.ERROR)){
                list.add(downloadInfo);
            }
        }

        return list;
    }

    public boolean existsDownload(ViewApk apk) {
        return downloads.get(apk.getAppHashId())!=null;
    }

    public class LocalBinder extends Binder {

        public ServiceManagerDownload getService(){

            BusProvider.getInstance().register(ServiceManagerDownload.this);

            return ServiceManagerDownload.this;
        }

    }



    public DownloadInfo getDownload(ViewApk apk){

        if(downloads.get(apk.getAppHashId())!=null){
            return downloads.get(apk.getAppHashId());
        }else{
            return new DownloadInfo(apk.getAppHashId(), apk);
        }

    }

    public void startDownload(DownloadInfo download, ViewApk apk){
        downloads.put(apk.getAppHashId(), download);
        ArrayList<DownloadModel> downloadList = new ArrayList<DownloadModel>();
        download.setDownloadExecutor(new DownloadExecutorImpl());
        DownloadModel apkDownload = new DownloadModel(apk.getPath(), DEFAULT_APK_DESTINATION + apk.getApkid() + "." +apk.getMd5()+".apk", apk.getMd5());
        apkDownload.setAutoExecute(true);
        downloadList.add(apkDownload);

        if(apk.getMainObbUrl()!=null){
            DownloadModel mainObbDownload = new DownloadModel(apk.getMainObbUrl(), OBB_DESTINATION + apk.getApkid() + "/" +apk.getMainObbFileName(), apk.getMainObbMd5());
            downloadList.add(mainObbDownload);
            if(apk.getPatchObbUrl()!=null){
                DownloadModel patchObbDownload = new DownloadModel(apk.getPatchObbUrl(), OBB_DESTINATION + apk.getApkid() + "/" +apk.getPatchObbFileName(), apk.getPatchObbMd5());
                downloadList.add(patchObbDownload);
            }
        }



        download.setFilesToDownload(downloadList);
        download.download();

    }

    public void startDownloadWithWebservice(final DownloadInfo download, final ViewApk apk){
        if (existsDownload(apk)) {
            startDownload(download, apk);
        } else {
            new AsyncTask<Void, Void, WebserviceGetApkInfo>() {

                @Override
                protected WebserviceGetApkInfo doInBackground(Void... params) {
                    try {
                        return new WebserviceGetApkInfo(ApplicationAptoide.getContext(), apk.getWebservicesPath(), apk, Category.INFOXML, null, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(WebserviceGetApkInfo webserviceGetApkInfo) {
                    super.onPostExecute(webserviceGetApkInfo);
                    try {
                        if (webserviceGetApkInfo != null) {
                            apk.setMd5(webserviceGetApkInfo.getApkMd5());
                            apk.setPath(webserviceGetApkInfo.getApkDownloadPath());

                            if (webserviceGetApkInfo.hasOBB()) {
                                apk.setMainObbUrl(webserviceGetApkInfo.getMainOBB().getString("path"));
                                apk.setMainObbFileName(webserviceGetApkInfo.getMainOBB().getString("filename"));
                                apk.setMainObbMd5(webserviceGetApkInfo.getMainOBB().getString("md5sum"));

                                if (webserviceGetApkInfo.hasPatchOBB()) {
                                    apk.setPatchObbUrl(webserviceGetApkInfo.getPatchOBB().getString("path"));
                                    apk.setPatchObbFileName(webserviceGetApkInfo.getPatchOBB().getString("filename"));
                                    apk.setPatchObbMd5(webserviceGetApkInfo.getPatchOBB().getString("md5sum"));
                                }
                            }
                            JSONArray array = webserviceGetApkInfo.getApkPermissions();
                            ArrayList<String> permissionList = new ArrayList<String>();
                            for (int i = 0; i != array.length(); i++) {
                                permissionList.add(array.getString(i));
                            }
                            apk.setPermissionsList(permissionList);

                            startDownload(download, apk);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }




    }

        @Subscribe public void removeDownload(DownloadRemoveEvent id){
        DownloadInfo info = downloads.remove(id.getId());

        if(info!=null){
            BusProvider.getInstance().post(info);
        }

        BusProvider.getInstance().post(new DownloadStatusEvent());
    }

    @Subscribe public void updateDownload(DownloadInfo id){
        ongoingDownloads = getOngoingDownloads();
        if(!showNotification){
            mBuilder = setNotification();
        }
        if(!ongoingDownloads.isEmpty()){
            updateProgress(mBuilder);
        }else{
            dismissNotification();
        }

    }

    private synchronized void dismissNotification(){
        try {
            managerNotification.cancel(-3);
            showNotification = false;
            stopForeground(true);
        } catch (Exception e) { }
    }

    @SuppressLint("NewApi")
	@Override
    public void onTaskRemoved(Intent rootIntent) {

        DownloadManager.INSTANCE.removeAllActiveDownloads();
        dismissNotification();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        dismissNotification();
        unregisterReceiver(autoUpdateReceiver);
        super.onDestroy();
    }

    private NotificationCompat.Builder setNotification() {

        managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        showNotification = true;
        Intent onClick = new Intent();
        onClick.setClassName(getPackageName(), Constants.APTOIDE_PACKAGE_NAME+".DownloadManager");
        //onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        onClick.setAction("");

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent onClickAction = PendingIntent.getActivity(this, 0, onClick, 0);

        mBuilder.setOngoing(true);
        mBuilder.setContentTitle(getString(R.string.aptoide_downloading, ApplicationAptoide.MARKETNAME))
                .setContentText(getString(R.string.x_app, ongoingDownloads.size()))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true)
                .setContentIntent(onClickAction);

        startForeground(-3, mBuilder.build());

        updateProgress(mBuilder);
        return mBuilder;
    }

    private void updateProgress(NotificationCompat.Builder mBuilder) {
        int percentage = getOngoingDownloadsPercentage();
        mBuilder.setProgress(100, percentage, percentage == 0);
        mBuilder.setContentText(getString(R.string.x_app, ongoingDownloads.size()));

        // Displays the progress bar for the first time
        managerNotification.notify(-3, mBuilder.build());
    }


    public ArrayList<DownloadInfo> getOngoingDownloads(){
        ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();

        for(DownloadInfo downloadInfo: getDownloads()){
            if(!downloadInfo.getStatusState().getEnumState().equals(EnumState.COMPLETE) && !downloadInfo.getStatusState().getEnumState().equals(EnumState.NOSTATE) && !downloadInfo.getStatusState().getEnumState().equals(EnumState.ERROR)){
                list.add(downloadInfo);
            }
        }

        return list;
    }


    public int getOngoingDownloadsPercentage(){

        Collection<DownloadInfo> list = getOngoingDownloads();

        int progressPercentage = 0;
        for(DownloadInfo info : list){
            progressPercentage = progressPercentage + info.getPercentDownloaded();
        }

        if(!list.isEmpty()){
            return progressPercentage / list.size();
        }

        return 0;


    }

	public void clearCompletedDownloads() {
		ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();
		list.addAll(getDownloads());
		Iterator<DownloadInfo> it = list.iterator();
		while(it.hasNext()){
			DownloadInfo downloadInfo = it.next();
			if(downloadInfo.getStatusState().getEnumState().equals(EnumState.COMPLETE) ){
                downloads.remove(downloadInfo.getId());
                DownloadManager.INSTANCE.removeFromCompletedList(downloadInfo);
            }
        }

		BusProvider.getInstance().post(new DownloadStatusEvent());

	}


}
