/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserSettingsRequest;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	String aptoide_path = Aptoide.getConfiguration().getPathCache();
	String icon_path = aptoide_path + "icons/";
	ManagerPreferences preferences;
	Context mctx;
	private boolean unlocked = false;
    private static boolean isSetingPIN = false;

    private Dialog DialogSetAdultpin(final Preference mp){
        isSetingPIN=true;
        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_requestpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(this)
                .setMessage(R.string.asksetadultpinmessage)
                .setView(v)

                .setPositiveButton(R.string.setpin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = ((EditText) v.findViewById(R.id.pininput)).getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            SecurePreferences.getInstance()
                                    .edit()
                                    .putInt(AdultDialog.MATUREPIN, new Integer(input))
                                    .commit();
                            mp.setTitle(R.string.remove_mature_pin_title);
                            mp.setSummary(R.string.remove_mature_pin_summary);
                            if (Build.VERSION.SDK_INT >= 10)
                                FlurryAgent.logEvent("Settings_Added_Pin_To_Lock_Adult_Content");
                            //mp.setOnPreferenceClickListener(removeclick);
                        }
                        isSetingPIN = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSetingPIN=false;
                    }
                });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSetingPIN = false;
            }
        });

        return alertDialog;
    }

    private void maturePinSetRemoveClick(){
        int pin = SecurePreferences.getInstance().getInt(AdultDialog.MATUREPIN,-1);
        final Preference mp= findPreference("Maturepin");
        if(pin!=-1) {
            // With Pin
            AdultDialog.dialogRequestMaturepin( Settings.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SecurePreferences.getInstance().edit().putInt(AdultDialog.MATUREPIN, -1).commit();
                    final Preference mp = findPreference("Maturepin");
                    mp.setTitle(R.string.set_mature_pin_title);
                    mp.setSummary(R.string.set_mature_pin_summary);
                    if (Build.VERSION.SDK_INT >= 10)
                        FlurryAgent.logEvent("Settings_Removed_Pin_Adult_Content");
                }
            }).show();
        }
        else{
            DialogSetAdultpin(mp).show();// Without Pin

        }
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mctx = this;
        new GetDirSize().execute(new File(aptoide_path),new File(icon_path));
        preferences = new ManagerPreferences(mctx);
//        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
//
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//					String key) {
//				preferences.setIconDownloadPermissions(new ViewIconDownloadPermissions(((CheckBoxPreference)findPreference("wifi")).isChecked(),
//						((CheckBoxPreference)findPreference("ethernet")).isChecked(),
//						((CheckBoxPreference)findPreference("4g")).isChecked(),
//						((CheckBoxPreference)findPreference("3g")).isChecked()));
//			}
//		});

        int pin = SecurePreferences.getInstance().getInt(AdultDialog.MATUREPIN,-1);
        final Preference mp= findPreference("Maturepin");
        if(pin!=-1) {
            Log.d("PINTEST","PinBuild");
            mp.setTitle(R.string.remove_mature_pin_title);
            mp.setSummary(R.string.remove_mature_pin_summary);
        }
        mp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                maturePinSetRemoveClick();
                return true;
            }
        });
        findPreference("matureChkBox").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final CheckBoxPreference cb = (CheckBoxPreference) preference;
                if (!cb.isChecked()) {
                    cb.setChecked(true);
                    AdultDialog.buildAreYouAdultDialog( Settings.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == DialogInterface.BUTTON_POSITIVE) {
                                cb.setChecked(false);
                            }
                        }
                    }).show();
                }
                return true;
            }
        });

        findPreference("showAllUpdates").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsResult();
                if(!((CheckBoxPreference)preference).isChecked()){
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Do_Not_Filter_Incompatible_Updates");
                }
                return true;
            }
        });

        findPreference("clearcache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
					new DeleteDir().execute(new File(icon_path));
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Cleared_Cache");
                }

				return false;
			}
		});
		findPreference("clearapk").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
                    new DeleteDir().execute(new File(aptoide_path));
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Removed_Data_And_Configurations");
                }

				return false;
			}
		});


        if(Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL,false)){
            findPreference("disablesocialtimeline").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ChangeUserSettingsRequest request = new ChangeUserSettingsRequest();
                    request.addTimeLineSetting(ChangeUserSettingsRequest.TIMELINEACTIVE);

                    HashMap<String, String> parameters = new HashMap<String, String>();
                    parameters.put("settings","timeline="+ChangeUserSettingsRequest.TIMELINEINACTIVE+";");
                    parameters.put("mode" , "json");
                    parameters.put("access_token", SecurePreferences.getInstance().getString("access_token", null));
                    HttpContent content = new UrlEncodedContent(parameters);
                    GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink+"3/changeUserSettings");

                    try {
                        final HttpRequest httpRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildPostRequest(url, content);
                        new UnsubscribeTimeline().execute(httpRequest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }
        else{
            ((PreferenceScreen)findPreference("root")).removePreference(findPreference("socialtimeline"));
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);


//		Preference hwspecs = (Preference) findPreference("hwspecs");
//		hwspecs.setIntent(new Intent(getBaseContext(), HWSpecActivity.class));
		Preference hwSpecs = findPreference("hwspecs");

        findPreference("theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Changed_Application_Theme");
                Toast.makeText(Settings.this, getString(R.string.restart_aptoide), Toast.LENGTH_LONG).show();
                return true;
            }
        });

		hwSpecs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mctx);
				alertDialogBuilder.setTitle(getString(R.string.setting_hwspecstitle));
				alertDialogBuilder
					.setIcon(android.R.drawable.ic_menu_info_details)
					.setMessage(getString(R.string.setting_sdk_version)+ ": "+ AptoideUtils.HWSpecifications.getSdkVer()+"\n" +
							    getString(R.string.setting_screen_size)+ ": "+AptoideUtils.HWSpecifications.getScreenSize(mctx)+"\n" +
							    getString(R.string.setting_esgl_version)+ ": "+AptoideUtils.HWSpecifications.getGlEsVer(mctx) +"\n" +
                                getString(R.string.screenCode)+ ": "+AptoideUtils.HWSpecifications.getNumericScreenSize(mctx) + "/" + AptoideUtils.HWSpecifications.getDensityDpi(mctx) +"\n" +
                                getString(R.string.cpuAbi)+ ": "+AptoideUtils.HWSpecifications.getCpuAbi() + " " + AptoideUtils.HWSpecifications.getCpuAbi2()
//                            + (ApplicationAptoide.PARTNERID!=null ? "\nPartner ID:" + ApplicationAptoide.PARTNERID : "")
                    )
					.setCancelable(false)
					.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Opened_Dialog_Hardware_Filters");
                        }
                    });
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

				return true;
			}
		});

//		if(!ApplicationAptoide.MATURECONTENTSWITCH){
//			CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("matureChkBox");
//			PreferenceCategory mCategory = (PreferenceCategory) findPreference("filters");
//			mCategory.removePreference(mCheckBoxPref);
//		}

//		Preference showExcluded = findPreference("showexcludedupdates");
//		showExcluded.setIntent(new Intent(mctx, ExcludedUpdatesActivity.class));

		EditTextPreference maxFileCache = (EditTextPreference) findPreference("maxFileCache");

		maxFileCache.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		maxFileCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				((EditTextPreference) preference).getEditText().setText(PreferenceManager.getDefaultSharedPreferences(mctx).getString("maxFileCache","200"));
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Added_Max_File_Cache");
                return false;
			}
		});


		Preference about = findPreference("aboutDialog");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(mctx).inflate(R.layout.dialog_about, null);
                String versionName = "";

                try {
                     versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                ( (TextView) view.findViewById(R.id.aptoide_version) ).setText("Version " + versionName);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mctx).setView(view);
                final AlertDialog aboutDialog = alertDialogBuilder.create();
                aboutDialog.setTitle(getString(R.string.about_us));
                aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
                aboutDialog.setCancelable(false);
                aboutDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Setting_Opened_About_Us_Dialog");
                        dialog.cancel();
                    }
                });
                aboutDialog.show();

				return true;
			}
		});

//		if(ApplicationAptoide.PARTNERID!=null){
//			PreferenceScreen preferenceScreen = getPreferenceScreen();
//			Preference etp = preferenceScreen.findPreference("aboutDialog");
//
//			PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("about");
//			preferenceGroup.removePreference(etp);
//			preferenceScreen.removePreference(preferenceGroup);
//
//		}
//
//
//        if(!ApplicationAptoide.DEBUG_MODE){
//            PreferenceScreen preferenceScreen = getPreferenceScreen();
//            Preference etp = preferenceScreen.findPreference("devmode");
//            PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("devmode");
//            preferenceGroup.removePreference(etp);
//            preferenceScreen.removePreference(preferenceGroup);
//        }



//        getActionBar().setTitle("");
//        getActionBar().setHomeButtonEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(isSetingPIN) {
            Log.d("PINTEST","is Setting adult pin");
            DialogSetAdultpin(mp).show();
        }

    }

    private final void SettingsResult(){
        setResult(RESULT_OK);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


    }

    public class UnsubscribeTimeline extends AsyncTask<HttpRequest, Void, GenericResponseV2>{
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(mctx);
            pd.setMessage(getString(R.string.please_wait));
            pd.show();
        }

        @Override
        protected GenericResponseV2 doInBackground(HttpRequest... params) {

            HttpRequest request = params[0];

            GenericResponseV2 genericResponseV2 =  null;
            try {
                request.setParser(new JacksonFactory().createJsonObjectParser());

                HttpResponse response = request.execute();
                genericResponseV2 = response.parseAs(GenericResponseV2.class);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return genericResponseV2;
        }

        @Override
        protected void onPostExecute(GenericResponseV2 responseV2) {
            super.onPostExecute(responseV2);

            if(responseV2.getStatus().equals("OK")){
                pd.dismiss();
                Preferences.putBooleanAndCommit(Preferences.TIMELINE_ACEPTED_BOOL,false);
                ((PreferenceScreen)findPreference("root")).removePreference(findPreference("socialtimeline"));
            }
        }
    }


    public class DeleteDir extends AsyncTask<File, Void, Void> {
		ProgressDialog pd;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(mctx);
			pd.setMessage(getString(R.string.please_wait));
			pd.show();
		}
		@Override
		protected Void doInBackground(File... params) {
			deleteDirectory(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			pd.dismiss();
			Toast toast= Toast.makeText(mctx, mctx.getString(R.string.clear_cache_sucess), Toast.LENGTH_SHORT);
			toast.show();
			new GetDirSize().execute(new File(aptoide_path),new File(icon_path));
		}

	}





	public class GetDirSize extends AsyncTask<File, Void, Double[]> {
		double getDirSize(File dir) {
			double size = 0;
			try{
				if (dir.isFile()) {
					size = dir.length();
				} else {
					File[] subFiles = dir.listFiles();
					for (File file : subFiles) {
						if (file.isFile()) {
							size += file.length();
						} else {
							size += this.getDirSize(file);
						}

					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return size;
		}
		@Override
		protected Double[] doInBackground(File... dir) {
			Double[] sizes = new Double[2];

			for (int i = 0; i!=sizes.length;i++){
				sizes[i]=this.getDirSize(dir[i]) / 1024 / 1024;
			}
			return sizes;
		}

		@Override
		protected void onPostExecute(Double[] result) {
			super.onPostExecute(result);
			redrawSizes(result);
			unlocked=true;
		}

	}

	private void redrawSizes(Double[] size) {
		if(!Build.DEVICE.equals("alien_jolla_bionic")){
            findPreference("clearapk").setSummary(getString(R.string.clearcontent_sum)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[0]))+")");
            findPreference("clearcache").setSummary(getString(R.string.clearcache_sum)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[1]))+")");
        }else{
            findPreference("clearapk").setSummary(getString(R.string.clearcontent_sum_jolla)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[0]))+")");
            findPreference("clearcache").setSummary(getString(R.string.clearcache_sum_jolla)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[1]))+")");
        }

	}


	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      if (files == null) {
	          return true;
	      }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
	    }
	    return true ;
	  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }else if( i == R.id.menu_SendFeedBack){
            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }
}
