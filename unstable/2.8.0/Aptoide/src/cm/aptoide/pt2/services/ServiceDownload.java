/*
 * ServiceDownload, part of Aptoide
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
package cm.aptoide.pt2.services;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.pt2.AIDLDownloadManager;
import cm.aptoide.pt2.exceptions.AptoideExceptionDownload;
import cm.aptoide.pt2.exceptions.AptoideExceptionNotFound;
import cm.aptoide.pt2.views.ViewCache;
import cm.aptoide.pt2.views.ViewDownload;
import cm.aptoide.pt2.views.ViewLogin;

/**
 * ServiceDownload, manages the actual download processes, and updates the download manager about the status of each download
 *
 * @author dsilveira
 *
 */
public class ServiceDownload extends Service {
	
	AIDLDownloadManager downloadStatusClient = null;
	
	/**
	 * When binding to the service, we return an interface to our AIDL stub
	 * allowing clients to send requests to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Aptoide-ServiceDownload", "binding new client");
		return serviceDownloadCallReceiver;
	}
	
	private final AIDLServiceDownload.Stub serviceDownloadCallReceiver = new AIDLServiceDownload.Stub() {
		
		@Override
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			try {
				return super.onTransact(code, data, reply, flags);
			} catch (RuntimeException e) {
				Log.w("Aptoide-ServiceDownload", "Unexpected serviceData exception", e);
				throw e;
			}
		}

		@Override
		public void callRegisterDownloadStatusObserver(AIDLDownloadManager downloadStatusClient) throws RemoteException {
			Log.d("Aptoide-ServiceDownload", "registered download status observer");
			registerDownloadStatusObserver(downloadStatusClient);
		}

		@Override
		public void callDownloadApk(ViewDownload download, ViewCache cache) throws RemoteException {
			Log.d("Aptoide-ServiceDownload", "starting apk download: "+download.getRemotePath());
			downloadManager.downloadApk(download, cache);
		}

		@Override
		public void callDownloadPrivateApk(ViewDownload download, ViewCache cache, ViewLogin login) throws RemoteException {
			Log.d("Aptoide-ServiceDownload", "starting apk download: "+download.getRemotePath());
			downloadManager.downloadApk(download, cache, login);
		}
		
	}; 
	
	public void registerDownloadStatusObserver(AIDLDownloadManager downloadStatusClient){
		this.downloadStatusClient = downloadStatusClient;
	}

	private Handler toastHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(ServiceDownload.this, msg.what, Toast.LENGTH_SHORT).show();
		}
	};

	private DownloadManager downloadManager;

    private class DownloadManager{
    	private ExecutorService installedColectorsPool;
    	
    	public DownloadManager(){
    		installedColectorsPool = Executors.newSingleThreadExecutor();
    	}
    	
    	public void downloadApk(ViewDownload download, ViewCache cache){
    		downloadApk(download, cache, null);
    	}
    	
    	public void downloadApk(ViewDownload download, ViewCache cache, ViewLogin login){
        	try {
				installedColectorsPool.execute(new DownloadApk(download, cache, login));
			} catch (Exception e) { }
        }
    	
    	private class DownloadApk implements Runnable{

    		ViewDownload download;
    		ViewCache cache;
    		ViewLogin login;
    		
			public DownloadApk(ViewDownload download, ViewCache cache, ViewLogin login) {
				this.download = download;
				this.cache = cache;
				this.login = login;
			}
    		
			@Override
			public void run() {
//	    		Log.d("Aptoide-ManagerDownloads", "apk download: "+download.getCache());
	    		if(!cache.isCached() || !cache.checkMd5()){
	    			try {
	    				download(download, cache, login);
	    			} catch (Exception e) {
	    				try {
	    					download(download, cache, login);
	    				} catch (Exception e2) {
	    					try {
								download(download, cache, login);
							} catch (Exception e3) {
//								e3.printStackTrace();
								download.setCompleted();
								try {
									downloadStatusClient.updateDownloadStatus(cache.hashCode(), download);
								} catch (RemoteException e4) {
									e4.printStackTrace();
								}
							}
	    				}
	    			}
	    		}
	    		Log.d("Aptoide-ManagerDownloads", "apk download: "+download.getRemotePath());
			}
    		
    	}
    	
    	private void download(ViewDownload download, ViewCache cache, ViewLogin login){
    		boolean overwriteCache = false;
    		boolean resuming = false;
    		boolean isLoginRequired = (login != null);
    		
    		String localPath = cache.getLocalPath();
    		String remotePath = download.getRemotePath();
    		long targetBytes;
    		
    		FileOutputStream fileOutputStream = null;
    		
    		try{
    			fileOutputStream = new FileOutputStream(localPath, !overwriteCache);
    			DefaultHttpClient httpClient = new DefaultHttpClient();
    			HttpGet httpGet = new HttpGet(remotePath);
    			Log.d("Aptoide-download","downloading from: "+remotePath+" to: "+localPath);
//    			Log.d("Aptoide-download","downloading with: "+getUserAgentString());
    			Log.d("Aptoide-download","downloading mode private: "+isLoginRequired);

//    			httpGet.setHeader("User-Agent", getUserAgentString());
    			
    			long resumeLength = cache.getFileLength();
    			if(!overwriteCache){
    				if(resumeLength > 0){
    					resuming = true;
    				}
    				Log.d("Aptoide-download","downloading from [bytes]: "+resumeLength);
    				httpGet.setHeader("Range", "bytes="+resumeLength+"-");
    				download.setProgress(resumeLength);
    			}

    			if(isLoginRequired){	
    				URL url = new URL(remotePath);
    				httpClient.getCredentialsProvider().setCredentials(
    						new AuthScope(url.getHost(), url.getPort()),
    						new UsernamePasswordCredentials(login.getUsername(), login.getPassword()));
    			}

    			HttpResponse httpResponse = httpClient.execute(httpGet);
    			if(httpResponse == null){
    				Log.d("Aptoide-ManagerDownloads","Problem in network... retry...");	
    				httpResponse = httpClient.execute(httpGet);
    				if(httpResponse == null){
    					Log.d("Aptoide-ManagerDownloads","Major network exception... Exiting!");
    					/*msg_al.arg1= 1;
    						 download_error_handler.sendMessage(msg_al);*/
    					if(!resuming){
    						cache.clearCache();
    					}
    					throw new TimeoutException();
    				}
    			}

    			if(httpResponse.getStatusLine().getStatusCode() == 401){
    				Log.d("Aptoide-ManagerDownloads","401 Timed out!");
    				fileOutputStream.close();
    				if(!resuming){
    					cache.clearCache();
    				}
    				throw new TimeoutException();
    			}else if(httpResponse.getStatusLine().getStatusCode() == 404){
    				fileOutputStream.close();
    				if(!resuming){
    					cache.clearCache();
    				}
    				throw new AptoideExceptionNotFound("404 Not found!");
    			}else{
    				
    				if(httpResponse.containsHeader("Content-Length")){
    					targetBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
    					Log.d("Aptoide-ManagerDownloads","Download targetBytes: "+targetBytes);
    					download.setProgressTarget(targetBytes);
    				}
    				

    				InputStream inputStream= null;
    				
    				if((httpResponse.getEntity().getContentEncoding() != null) && (httpResponse.getEntity().getContentEncoding().getValue().equalsIgnoreCase("gzip"))){

    					Log.d("Aptoide-ManagerDownloads","with gzip");
    					inputStream = new GZIPInputStream(httpResponse.getEntity().getContent());

    				}else{

//    					Log.d("Aptoide-ManagerDownloads","No gzip");
    					inputStream = httpResponse.getEntity().getContent();

    				}
    				
    				byte data[] = new byte[8096];
    				/** in percentage */
    				int progressTrigger = 5; 
    				int bytesRead;

    				while((bytesRead = inputStream.read(data, 0, 8096)) > 0) {
    					download.incrementProgress(bytesRead);
    					fileOutputStream.write(data,0,bytesRead);
    					if(download.getProgressPercentage() % progressTrigger == 0){
							try {
								downloadStatusClient.updateDownloadStatus(cache.hashCode(), download);
							} catch (RemoteException e4) {
								e4.printStackTrace();
							}
    					}
    				}
    				Log.d("Aptoide-ManagerDownloads","Download done! Name: "+download.getRemotePath()+" localPath: "+localPath);
    				download.setCompleted();
    				fileOutputStream.flush();
    				fileOutputStream.close();
    				inputStream.close();

    				if(cache.hasMd5Sum()){
    					if(!cache.checkMd5()){
    						cache.clearCache();
    						throw new AptoideExceptionDownload("md5 check failed!");
    					}
    				}
    				
    				installApp(cache);

    			}
    		}catch (Exception e) {
    			try {
    				fileOutputStream.flush();
    				fileOutputStream.close();	
    			} catch (Exception e1) { }		
    			e.printStackTrace();
    			if(cache.getFileLength() > 0){
    				download.setCompleted();
					try {
						downloadStatusClient.updateDownloadStatus(cache.hashCode(), download);
					} catch (RemoteException e4) {
						e4.printStackTrace();
					}
//    				scheduleInstallApp(cache.getId());
    			}
    			throw new AptoideExceptionDownload(e);
    		}
    	}
    }
	
    
    
    @Override
    public void onCreate() {
		downloadManager = new DownloadManager();
    	super.onCreate();
    }
    
    
//	private 
	

	
//	private String getUserAgentString(){
//		ViewClientStatistics clientStatistics = getClientStatistics();
//		return String.format(Constants.USER_AGENT_FORMAT
//				, clientStatistics.getAptoideVersionNameInUse(), clientStatistics.getScreenDimensions().getFormattedString()
//				, clientStatistics.getAptoideClientUUID(), getServerUsername());
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

}
