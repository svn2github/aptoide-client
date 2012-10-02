/*
 * ApplicationServiceManager, part of Aptoide
 * Copyright (C) 2012 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package cm.aptoide.pt2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cm.aptoide.pt2.services.AIDLServiceDownload;
import cm.aptoide.pt2.services.ServiceDownload;
import cm.aptoide.pt2.util.Constants;
import cm.aptoide.pt2.views.ViewCache;
import cm.aptoide.pt2.views.ViewDownload;
import cm.aptoide.pt2.views.ViewDownloadManagement;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * ApplicationServiceManager, manages interaction between interface classes and services
 *
 * @author dsilveira
 *
 */
public class ApplicationServiceManager extends Application {
	private boolean isRunning = false;

	private AIDLServiceDownload serviceDownloadCaller = null;

	private boolean serviceDownloadSeenRunning = false;
	private boolean serviceDownloadIsBound = false;
	
	private ServiceConnection serviceDownloadConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using AIDL, so here we set the remote service interface.
			serviceDownloadCaller = AIDLServiceDownload.Stub.asInterface(service);
			serviceDownloadIsBound = true;
			
			Log.v("Aptoide-ApplicationServiceManager", "Connected to ServiceData");	
			
//			if(!serviceDownloadSeenRunning){
//			}
            
            try {
                Log.v("Aptoide-ApplicationServiceManager", "Called for registering as Download Status Observer");
				serviceDownloadCaller.callRegisterDownloadStatusObserver(serviceDownloadCallback);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDownloadCaller = null;
			serviceDownloadIsBound = false;
			
			Log.v("Aptoide-ApplicationServiceManager", "Disconnected from ServiceData");
		}
	};
	
	
	
	private AIDLDownloadManager.Stub serviceDownloadCallback = new AIDLDownloadManager.Stub() {

		@Override
		public void updateDownloadStatus(int id, ViewDownload update) throws RemoteException {
			ongoingDownloads.get(id).updateProgress(update);
			updateGlobalProgress();
		}
		
	};
	
	private ConnectivityManager connectivityState;

	HashMap<Integer, ViewDownloadManagement> ongoingDownloads;
	
	ViewDownload globaDownloadStatus;
	
	private ExecutorService cachedThreadPool;
	
	@Override
	public void onCreate() {
		if (!isRunning) {
			connectivityState = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			
			ongoingDownloads = new HashMap<Integer, ViewDownloadManagement>();
			
			globaDownloadStatus = new ViewDownload("local:\\GLOBAL");
			
			cachedThreadPool = Executors.newCachedThreadPool();
			
			makeSureServiceDownloadIsRunning();
			isRunning = true;
		}
		super.onCreate();
	}

	private void makeSureServiceDownloadIsRunning(){
    	ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
    	for (RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if(runningService.service.getClassName().equals(Constants.SERVICE_DOWNLOAD_CLASS_NAME)){
				this.serviceDownloadSeenRunning = true;
				break;
			}
		}

    	if(!serviceDownloadIsBound){
//    		startService(new Intent(this, ServiceDownload.class));	//TODO uncomment this to make service independent of Aptoide's lifecycle
    		bindService(new Intent(this, ServiceDownload.class), serviceDownloadConnection, Context.BIND_AUTO_CREATE);
    	}
    }
	
	public boolean isConnectionAvailable(){
		boolean connectionAvailable = false;
		try {
			connectionAvailable = connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable mobile: "+connectionAvailable);	
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable wifi: "+connectionAvailable);
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable wimax: "+connectionAvailable);
		} catch (Exception e) { }
		try {
			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
			Log.d("ManagerDownloads", "isConnectionAvailable ethernet: "+connectionAvailable);
		} catch (Exception e) { }
		
		return connectionAvailable;
	}
	
//	public boolean isPermittedConnectionAvailable(ViewIconDownloadPermissions permissions){
//		boolean connectionAvailable = false;
//		if(permissions.isWiFi()){
//			try {
//				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
//				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wifi: "+connectionAvailable);
//			} catch (Exception e) { }
//		} 
//		if(permissions.isWiMax()){
//			try {
//				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
//				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wimax: "+connectionAvailable);
//			} catch (Exception e) { }
//		} 
//		if(permissions.isMobile()){
//			try {
//				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
//				Log.d("ManagerDownloads", "isPermittedConnectionAvailable mobile: "+connectionAvailable);
//			} catch (Exception e) { }
//		}
//		if(permissions.isEthernet()){
//			try {
//				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
//				Log.d("ManagerDownloads", "isPermittedConnectionAvailable ethernet: "+connectionAvailable);
//			} catch (Exception e) { }
//		}
//
//		Log.d("ManagerDownloads", "isPermittedConnectionAvailable: "+connectionAvailable+"  permissions: "+permissions);
//		return connectionAvailable;
//	}
	
	
	public void installApp(ViewCache apk){
//		if(isAppScheduledToInstall(appHashid)){
//			unscheduleInstallApp(appHashid);
//		}
		Intent install = new Intent(Intent.ACTION_VIEW);
		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		install.setDataAndType(Uri.fromFile(apk.getFile()),"application/vnd.android.package-archive");
		Log.d("Aptoide", "Installing app: "+apk.getLocalPath());
		startActivity(install);
	}
	
	public void startDownload(final ViewDownloadManagement viewDownload){
		if(viewDownload.getCache().isCached()){
			installApp(viewDownload.getCache());
		}else{
			ongoingDownloads.put(viewDownload.hashCode(), viewDownload);
			cachedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if(viewDownload.isLoginRequired()){
							serviceDownloadCaller.callDownloadPrivateApk(viewDownload.getDownload(), viewDownload.getCache(), viewDownload.getLogin());
						}else{
							serviceDownloadCaller.callDownloadApk(viewDownload.getDownload(), viewDownload.getCache());
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	private synchronized void updateGlobalProgress(){
		globaDownloadStatus.setProgressTarget(100*ongoingDownloads.size());
		globaDownloadStatus.setProgress(0);
		globaDownloadStatus.setSpeedInKbps(0);
		for (ViewDownloadManagement download : ongoingDownloads.values()) {
			globaDownloadStatus.incrementProgress(download.getProgress());
			globaDownloadStatus.incrementSpeed(download.getSpeedInKbps());
		}
//		updateNofification();
	}
	
}