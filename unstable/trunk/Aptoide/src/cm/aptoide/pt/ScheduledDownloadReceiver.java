/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class ScheduledDownloadReceiver extends BroadcastReceiver {
	Database db;
	Context context;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		context = arg0;
		db=Database.getInstance();
		final ConnectivityManager connMgr = (ConnectivityManager)
				arg0.getSystemService(Context.CONNECTIVITY_SERVICE);

				final android.net.NetworkInfo wifi =
				connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor prefEdit = sPref.edit();

		if(wifi.getState()==NetworkInfo.State.CONNECTED){
			Log.d("Receiver", "Wireless Connected");
			if(sPref.getBoolean("schDwnBox", false)&&db.getScheduledDownloads().getCount()!=0){
				Intent intent = new Intent(arg0,ScheduledDownloads.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|-Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra("downloadAll", "");
				Log.i("Reeceiver",sPref.getBoolean("intentChanged", true)+"");
				arg0.startActivity(intent);
			}
		}

	}



}
