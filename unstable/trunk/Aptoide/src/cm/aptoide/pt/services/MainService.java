/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cm.aptoide.pt.*;
import cm.aptoide.pt.Server.State;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.exceptions.AptoideException;
import cm.aptoide.pt.util.NetworkUtils;
import cm.aptoide.pt.util.RepoUtils;
import cm.aptoide.pt.util.Utils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainService extends Service {




    //	Database db;
	private static boolean isParsing = false;
	String defaultPath = AptoideConfiguration.getInstance().getPathCache();
	String defaultXmlPath = defaultPath+"info.xml";
	String defaultTopXmlPath = defaultPath+"top.xml";
	String defaultLatestXmlPath = defaultPath+"latest.xml";
	String defaultExtrasXmlPath = defaultPath+"extras.xml";
	String defaultBootConfigXmlPath = defaultPath+"boot_config.xml";

	static ArrayList<String> serversParsing = new ArrayList<String>();
	private static final int ID_UPDATES_NOTIFICATION = 1;
	private ArrayList<String> updatesList = new ArrayList<String>();


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver , new IntentFilter("complete"));
        registerReceiver(parseCompletedReceiver , new IntentFilter("parse_completed"));
    }

    @Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try{
                Log.d("TAG", "Removing server");
				serversParsing.remove(intent.getStringExtra("server"));
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	};

	private BroadcastReceiver parseCompletedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
            Log.d("TAG", "Parse Complete");
            new Thread(new Runnable() {
				@Override
				public void run() {

                    if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("showUpdatesNotification", true) && serversParsing.isEmpty()){
    					Cursor updates = Database.getInstance().getUpdates(Order.DATE);
	    				setUpdatesNotification(updates);
                    }

                    if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_update", false) && serversParsing.isEmpty()){
                        Intent i = new Intent("auto_update");
                        sendBroadcast(i);
                    }

				}
			}).start();
		}
	};


    public class LocalBinder extends Binder{
		public MainService getService(){
			return MainService.this;
		}
	}

	public String get(Server server,String xmlpath,String what, boolean delta) throws IOException{
		getApplicationContext().sendBroadcast(new Intent("connecting"));
		String hash = "";
        server.name = RepoUtils.split(server.url);
		if (delta&&server.hash.length() > 0) {
			hash = "?hash=" + server.hash + ":"+ Utils.getMyCountryCode(getApplicationContext());
            server.showError = false;
            server.isDelta = true;
		}
		NetworkUtils utils = new NetworkUtils();
        Log.d("TAG", server.url + " " + server.getLogin().getUsername() + " " + server.getLogin().getPassword());
		if(delta && utils.checkServerConnection(server.url, server.getLogin().getUsername(),server.getLogin().getPassword())==401){
			throw new AptoideException("401", new IOException());
		}
		String url = server.url + what + hash;


		System.out.println(server);
		System.out.println(server.getClass().getCanonicalName());
		File f = new File(xmlpath);

        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
		InputStream in = utils.getInputStream(
				url,
				server.getLogin().getUsername(),
				server.getLogin().getPassword(),
				getApplicationContext());

		int i = 0;
		while (f.exists()) {
			f = new File(xmlpath + i++);
		}
		FileOutputStream out = new FileOutputStream(f);

		byte[] buffer = new byte[1024];
		int len;
		getApplicationContext().sendBroadcast(new Intent("downloading"));
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.close();

		return f.getAbsolutePath();
	}

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("TAG", "Removing files");
        try{
            File[] files = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide").listFiles();

            for(File file : files){
                if(file.getName().contains(".xml")&&!file.getName().contains("servers.xml")){
                    file.delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onTaskRemoved(rootIntent);
    }

    @Override
	public void onDestroy() {
		unregisterReceiver(receiver);


		unregisterReceiver(parseCompletedReceiver);
		try{
			File[] files = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide").listFiles();

			for(File file : files){
				if(file.getName().contains(".xml")&&!file.getName().contains("servers.xml")){
					file.delete();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public void addStore(Database db, String uri_str, String username, String password) {
		Server server;
		try{

            if(db.getServer(uri_str)!=null){
				return;
			}

			db.addStore(uri_str,username,password);
			server = db.getServer(uri_str);
            server.isBare = true;
			if(ApplicationAptoide.DEFAULTSTORENAME != null && uri_str.equals("http://" + ApplicationAptoide.DEFAULTSTORENAME + ".store.aptoide.com/")){
				server.oem = true;
				db.addStoreInfo(ApplicationAptoide.AVATAR, ApplicationAptoide.DEFAULTSTORENAME, "0", ApplicationAptoide.THEME, ApplicationAptoide.DESCRIPTION, ApplicationAptoide.VIEW, ApplicationAptoide.ITEMS, db.getServer("http://" + ApplicationAptoide.DEFAULTSTORENAME + ".store.aptoide.com/").id);
			}else{
				db.addStoreInfo("",RepoUtils.split(server.url),"0","","","","",server.id);
			}

			parseServer(db, server);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

    ExecutorService service = Executors.newSingleThreadExecutor();

	public void parseServer(final Database db, final Server server) throws  IOException {


		if(!serversParsing.contains(server.url)){

            server.state=State.QUEUED;
            db.updateStatus(server);

			service.submit(new Runnable() {

				@Override
				public void run() {
						addStoreInfo(db, server);
//						parseBootConfig(db, server);
						parseTop(db, server);
						parseLatest(db, server);
					try{
						parseInfoXml(db, server);
					} catch (AptoideException e){
						Intent i = new Intent("401");
						i.putExtra("url", server.url);
						getApplicationContext().sendBroadcast(i);
						serversParsing.remove(server.url);
					}catch (IOException e){
                        if(server.showError){
                            server.state=State.FAILED;
                        }else{
                            server.state=State.PARSED;
                        }
						db.updateStatus(server);
						serversParsing.remove(server.url);
						e.printStackTrace();
					}
				}
			});

			serversParsing.add(server.url);
		}
	}


	public boolean deleteStore(Database db, long id){
		if(!serversParsing.contains(db.getServer(id, false).url)){
			db.deleteServer(id,true);
			clearUpdatesList();
			cancelUpdatesNotification();
			return true;
		}
		return false;
	}

	public void parseTop(final Database db, final Server server) {
		new Thread(new Runnable() {
			public void run() {
				String path;
				try {
					//			serversParsing.put((int)server.id, server);

                    NetworkUtils utils = new NetworkUtils();

                    long lastModified = utils.getLastModified(new URL(server.url + "top.xml"));

                    if(Long.parseLong(db.getRepoHash(server.id, Category.TOP)) < lastModified){

                        path = get(server, defaultTopXmlPath, "top.xml", false);

                        Server serverTop = new ServerTop(server);

                        serverTop.hash = lastModified + "";
                        db.deleteTopOrLatest(server.id, Category.TOP);
                        RepoParser.getInstance(db).parseTop(path, serverTop);

                    }
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}



	public void parseLatest(final Database db, final Server server){
			new Thread(new Runnable() {

				public void run() {
					String path;
					//			serversParsing.put((int)server.id, server);
					try {

                        NetworkUtils utils = new NetworkUtils();

                        long lastModified = utils.getLastModified(new URL(server.url + "latest.xml"));



                        if(Long.parseLong(db.getRepoHash(server.id, Category.LATEST)) < lastModified){

						    path = get(server, defaultLatestXmlPath, "latest.xml", false);
                            Server serverLatest = new ServerLatest(server);
                            serverLatest.hash = lastModified + "";
                            db.deleteTopOrLatest(server.id, Category.LATEST);
                            RepoParser.getInstance(db).parseLatest(path, serverLatest);


                        }
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}).start();

	}

	public void parseInfoXml(final Database db, final Server server) throws IOException{

        String path = get(server,defaultXmlPath,"info.xml", !server.isBare);
		RepoParser.getInstance(db).parseInfoXML(path,server);

	}

	public void addStoreInfo(Database db, Server server) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(String.format(AptoideConfiguration.getInstance().getWebServicesUri() + "webservices/getRepositoryInfo/%s/json",
							RepoUtils.split(server.url))).openConnection();
			connection.connect();
			int rc = connection.getResponseCode();
			if (rc == 200) {
				String line;
				BufferedReader br = new BufferedReader(
						new java.io.InputStreamReader(connection
								.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null)
					sb.append(line).append('\n');

				JSONObject json = new JSONObject(sb.toString());
				JSONObject array = json.getJSONObject("listing");
				String avatar = array.getString("avatar");
				String name = array.getString("name");
				String downloads = array.getString("downloads");
				String theme = array.getString("theme");
				String description = array.getString("description");
				String view = array.getString("view");
				String items = array.getString("items");
				db.addStoreInfo(avatar,name,withSuffix(downloads),theme,description,view,items,server.id);
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			if(server.oem){
				db.addStoreInfo(ApplicationAptoide.AVATAR, ApplicationAptoide.DEFAULTSTORENAME, "0", ApplicationAptoide.THEME, ApplicationAptoide.DESCRIPTION, ApplicationAptoide.VIEW, ApplicationAptoide.ITEMS, db.getServer("http://" + ApplicationAptoide.DEFAULTSTORENAME + ".store.aptoide.com/").id);
			}else{
				db.addStoreInfo("",RepoUtils.split(server.url),"0","","","","",server.id);
			}

		}


	}


	public static String withSuffix(String input) {
		long count = Long.parseLong(input);
	    if (count < 1000) return "" + count;
	    int exp = (int) (Math.log(count) / Math.log(1000));
	    return String.format("%.1f %c",
	                         count / Math.pow(1000, exp),
	                         "kMGTPE".charAt(exp-1));
	}

	public void clearUpdatesList(){
		updatesList.clear();
	}

	public void cancelUpdatesNotification(){
		if (Context.NOTIFICATION_SERVICE!=null) {
	        String ns = Context.NOTIFICATION_SERVICE;
	        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
	        nMgr.cancel(ID_UPDATES_NOTIFICATION);
	    }
	}

	public void setUpdatesNotification(Cursor updates) {
		boolean isNotification = false;

		if(!updates.isClosed()){
			for(updates.moveToFirst(); !updates.isAfterLast(); updates.moveToNext()){
				String updateApkid = updates.getString(updates.getColumnIndex(DbStructure.COLUMN_APKID));
				if(!updatesList.contains(updateApkid)){
					updatesList.add(updateApkid);
					isNotification = true;
				}
			}
        updates.close();
		}

		if(isNotification){

			NotificationManager managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			int icon = android.R.drawable.stat_sys_download_done;
			if(ApplicationAptoide.MARKETNAME.equals("Aptoide")){

                icon = R.drawable.ic_stat_aptoide_512x512_notification;

			}
			CharSequence tickerText = getString(R.string.has_updates, ApplicationAptoide.MARKETNAME);
			long when = System.currentTimeMillis();

            Context context = getApplicationContext();
            CharSequence contentTitle = ApplicationAptoide.MARKETNAME;
            CharSequence contentText = getString(R.string.new_updates, updates.getCount()+"");
            if(updates.getCount()<2){
                contentText = getString(R.string.one_new_update, updates.getCount()+"");
            }
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra("new_updates", true);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(contentIntent)
                    .setTicker(tickerText)
                    .build();



			managerNotification.notify(ID_UPDATES_NOTIFICATION, notification);


			Log.d("Aptoide-MainActivity","Set updates notification");
		}
	}
}
