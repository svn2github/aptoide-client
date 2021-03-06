/**
 * Settings, part of Aptoide
 * Copyright (C) 2011 Duarte Silveira
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
package cm.aptoide.pt;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.pt.data.AIDLAptoideServiceData;
import cm.aptoide.pt.data.AptoideServiceData;
import cm.aptoide.pt.data.preferences.EnumAgeRating;
import cm.aptoide.pt.data.preferences.ViewSettings;
import cm.aptoide.pt.data.system.ViewHwFilters;
import cm.aptoide.pt.data.webservices.ViewIconDownloadPermissions;


/**
 * Settings, handles Aptoide's settings interface
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class Settings extends PreferenceActivity {
	
	ViewSettings storedSettings;
	ViewHwFilters hwFilters;
	ViewIconDownloadPermissions iconDownloadPermissions;
	
	CheckBoxPreference hwFilter;
	ListPreference ageRating;
	
	private AIDLAptoideServiceData serviceDataCaller = null;

	private boolean serviceDataIsBound = false;

	private ServiceConnection serviceDataConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using AIDL, so here we set the remote service interface.
			serviceDataCaller = AIDLAptoideServiceData.Stub.asInterface(service);
			serviceDataIsBound = true;
			
			Log.v("Aptoide-Settings", "Connected to ServiceData");
	        
			try {
				storedSettings = serviceDataCaller.callGetSettings();
				hwFilters = serviceDataCaller.callGetHwFilters();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			showSettings();
			
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			serviceDataIsBound = false;
			serviceDataCaller = null;
			
			Log.v("Aptoide-Settings", "Disconnected from ServiceData");
		}
	};
	
	
	private void showSettings(){
		addPreferencesFromResource(R.xml.settings);
//		setContentView(R.layout.settings);
		
		Preference iconDownloadTextView = (Preference) findPreference("icon_download_permissions");
		iconDownloadTextView.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					iconDownloadPermissions = serviceDataCaller.callGetIconDownloadPermissions();
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				final ViewIconDownloadPermissions storedPermissions = Settings.this.iconDownloadPermissions;
				Log.d("Aptoide-Settings", "clicked icon download permissions");
				View iconDownloadView = LinearLayout.inflate(Settings.this, R.layout.dialog_icon_download_permissions, null);
				Builder dialogBuilder = new AlertDialog.Builder(Settings.this).setView(iconDownloadView);
				final AlertDialog iconDownloadDialog = dialogBuilder.create();
				iconDownloadDialog.setIcon(R.drawable.ic_menu_manage);
				iconDownloadDialog.setTitle(getString(R.string.download_icons));
				
				final RadioButton wifi = (RadioButton) iconDownloadView.findViewById(R.id.wifi);
				final RadioButton ethernet = (RadioButton) iconDownloadView.findViewById(R.id.ethernet);
				final RadioButton wimax = (RadioButton) iconDownloadView.findViewById(R.id.wimax);
				final RadioButton mobile = (RadioButton) iconDownloadView.findViewById(R.id.mobile);
				final RadioButton never = (RadioButton) iconDownloadView.findViewById(R.id.never);
				
				wifi.setChecked(storedPermissions.isWiFi());
				ethernet.setChecked(storedPermissions.isEthernet());
				wimax.setChecked(storedPermissions.isWiMax());
				mobile.setChecked(storedPermissions.isMobile());
				never.setChecked(storedPermissions.isNever());
				
				
				wifi.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							if(never.isChecked()){
								never.setChecked(false);
							}
							wifi.setChecked(!wifi.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				ethernet.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							if(never.isChecked()){
								never.setChecked(false);
							}
							ethernet.setChecked(!ethernet.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				wimax.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							if(never.isChecked()){
								never.setChecked(false);
							}
							wimax.setChecked(!wimax.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				mobile.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							if(never.isChecked()){
								never.setChecked(false);
							}
							mobile.setChecked(!mobile.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				
				never.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							never.setChecked(!never.isChecked());
							if(never.isChecked()){
								wifi.setChecked(false);
								ethernet.setChecked(false);
								wimax.setChecked(false);
								mobile.setChecked(false);
							}else{
								wifi.setChecked(true);
								ethernet.setChecked(true);
								wimax.setChecked(true);
								mobile.setChecked(true);
							}						
							return true;
						}else{
							return false;
						}
					}
				});
				
				
				final Button done = (Button) iconDownloadView.findViewById(R.id.done);
				done.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ViewIconDownloadPermissions newPermissions 
							= new ViewIconDownloadPermissions( wifi.isChecked(), ethernet.isChecked(), wimax.isChecked(), mobile.isChecked() );
						if(!newPermissions.equals(storedPermissions)){
							try {
								serviceDataCaller.callSetIconDownloadPermissions(newPermissions);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						iconDownloadDialog.dismiss();
					}
				});
				
				final Button cancel = (Button) iconDownloadView.findViewById(R.id.cancel);
				cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						iconDownloadDialog.dismiss();
					}
				});
				
				iconDownloadDialog.show();
				return true;
			}
		});
		

		Preference clearCacheTextView = (Preference) findPreference("clear_cache");
		clearCacheTextView.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Log.d("Aptoide-Settings", "clicked clear cache");
				View clearCacheView = LinearLayout.inflate(Settings.this, R.layout.dialog_clear_cache, null);
				Builder dialogBuilder = new AlertDialog.Builder(Settings.this).setView(clearCacheView);
				final AlertDialog clearCacheDialog = dialogBuilder.create();
				clearCacheDialog.setIcon(R.drawable.ic_menu_delete);
				clearCacheDialog.setTitle(getString(R.string.clear_cache));
				
				final RadioButton icon = (RadioButton) clearCacheView.findViewById(R.id.icon);
				final RadioButton apk = (RadioButton) clearCacheView.findViewById(R.id.apk);
				
				icon.setChecked(false);
				apk.setChecked(false);
				
				icon.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View view, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							icon.setChecked(!icon.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				apk.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							apk.setChecked(!apk.isChecked());
							return true;
						}else{
							return false;
						}
					}
				});
				
				
				final Button done = (Button) clearCacheView.findViewById(R.id.done);
				done.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						try {
							if(icon.isChecked()){
								serviceDataCaller.callClearIconCache();
								Toast.makeText(Settings.this, "Icon cache cleared", Toast.LENGTH_SHORT).show();
							}
							if(apk.isChecked()){
								serviceDataCaller.callClearApkCache();
								Toast.makeText(Settings.this, "Apk cache cleared", Toast.LENGTH_SHORT).show();
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						clearCacheDialog.dismiss();
					}
				});
				
				final Button cancel = (Button) clearCacheView.findViewById(R.id.cancel);
				cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						clearCacheDialog.dismiss();
					}
				});
				
				clearCacheDialog.show();
				return true;
			
			}
		});
		
		
		Preference clearServerLogin = (Preference) findPreference("clear_server_login");
		clearServerLogin.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Log.d("Aptoide-Settings", "clicked clear server login");
				try {
					serviceDataCaller.callClearServerLogin();
					Toast.makeText(Settings.this, "Login cleared", Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});
		
		
		Preference setServerLogin = (Preference) findPreference("set_server_login");
		setServerLogin.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Log.d("Aptoide-Settings", "clicked set server login");
				String token = null;
				try {
					token = serviceDataCaller.callGetServerToken();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(token == null){
					Log.d("Aptoide-AppInfo", "No login set");
					DialogLogin loginComments = new DialogLogin(Settings.this, serviceDataCaller, DialogLogin.InvoqueNature.NO_CREDENTIALS_SET);
//					loginComments.setOnDismissListener(new OnDismissListener() {
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//							addAppVersionComment();
//						}
//					});
					loginComments.show();
				}else{
					Toast.makeText(Settings.this, "Login already set", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		
		Preference hwSpecs = (Preference) findPreference("hw_specs");
		hwSpecs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Log.d("Aptoide-Settings", "clicked hw specs: "+hwFilters);
				View hwSpecsView = LinearLayout.inflate(Settings.this, R.layout.dialog_hw_specs, null);
				Builder dialogBuilder = new AlertDialog.Builder(Settings.this).setView(hwSpecsView);
				final AlertDialog hwSpecsDialog = dialogBuilder.create();
				hwSpecsDialog.setIcon(R.drawable.ic_menu_info_details);
				hwSpecsDialog.setTitle(getString(R.string.hw_specs));
				
				final TextView sdk = (TextView) hwSpecsView.findViewById(R.id.sdk);
				sdk.setText(Integer.toString(hwFilters.getSdkVersion()));
				final TextView screen = (TextView) hwSpecsView.findViewById(R.id.screen);
				screen.setText(Integer.toString(hwFilters.getScreenSize()));
				final TextView gles = (TextView) hwSpecsView.findViewById(R.id.gles);
				gles.setText(Float.toString(hwFilters.getGlEsVersion()));
				
				hwSpecsDialog.setButton(getString(R.string.back), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						hwSpecsDialog.dismiss();
					}
				});
				
				hwSpecsDialog.show();
				return true;
			}
		});

		
		hwFilter = (CheckBoxPreference) findPreference("check_hw_specs");
		hwFilter.setChecked(storedSettings.isHwFilterOn());
		hwFilter.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {	//isChecked on preferences returns oposite value
				Log.d("Aptoide-Settings", "hwFilter isChecked: "+!hwFilter.isChecked()+" storedValue: "+storedSettings.isHwFilterOn());
				if(!hwFilter.isChecked() != storedSettings.isHwFilterOn()){
					try {
						serviceDataCaller.callSetHwFilter(!hwFilter.isChecked());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		
		ageRating = (ListPreference) findPreference("app_rating");
		ageRating.setDefaultValue(storedSettings.getRating().equals(EnumAgeRating.Pre_Teen)?getString(R.string.pre_teen):(storedSettings.getRating().equals(EnumAgeRating.No_Filter)?getString(R.string.no_filter):storedSettings.getRating().name()));
		ageRating.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				EnumAgeRating rating = EnumAgeRating.safeValueOf(newValue.toString());
				Log.d("Aptoide-Settings", "ageRating: "+rating+" storedValue: "+storedSettings.getRating());
				if(!rating.equals(storedSettings.getRating()) && !rating.equals(EnumAgeRating.unrecognized)){
					try {
						serviceDataCaller.callSetAgeRating(rating.ordinal());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		
		final CheckBoxPreference automaticInstall = (CheckBoxPreference) findPreference("automatic_install");
		automaticInstall.setChecked(storedSettings.isAutomaticInstallOn());
		automaticInstall.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) { //isChecked on preferences returns oposite value
				Log.d("Aptoide-Settings", "automati install isChecked: "+!automaticInstall.isChecked()+" storedValue: "+storedSettings.isAutomaticInstallOn());
				if(!automaticInstall.isChecked() != storedSettings.isAutomaticInstallOn()){
					try {
						serviceDataCaller.callSetAutomaticInstall(!automaticInstall.isChecked());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!serviceDataIsBound){
    		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
    	}
		
	}

	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK ) {
//			Log.d("Aptoide-SelfUpdate", "");
//			//TODO cancel download
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
	
	@Override
	public void finish() {
		if(hwFilter.isChecked() != storedSettings.isHwFilterOn() || !EnumAgeRating.safeValueOf(ageRating.getValue()).equals(storedSettings.getRating())){
			try {
				serviceDataCaller.callResetAvailableApps();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(serviceDataIsBound){
			unbindService(serviceDataConnection);
		}
		super.finish();
	}
	
}
