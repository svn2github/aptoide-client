/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoidetv.pt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;

import cm.aptoidetv.pt.WebServices.old.AptoideUtils;

public class Settings extends PreferenceActivity {

	String aptoide_path = AppTV.getConfiguration().getPathCache();
	String icon_path = aptoide_path + "icons/";
	Context mctx;
	private boolean unlocked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mctx=this;
        new GetDirSize().execute(new File(aptoide_path),new File(icon_path));

/*        findPreference("showAllUpdates").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsResult();
                if(!((CheckBoxPreference)preference).isChecked()){

                }
                return true;
            }
        });*/

        findPreference("clearcache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
    		@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
					new DeleteDir().execute(new File(icon_path));

                }

				return false;
			}
		});
		findPreference("clearapk").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
                    new DeleteDir().execute(new File(aptoide_path));
                }

				return false;
			}
		});

//		Preference hwspecs = (Preference) findPreference("hwspecs");
//		hwspecs.setIntent(new Intent(getBaseContext(), HWSpecActivity.class));
		Preference hwSpecs = findPreference("hwspecs");

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
                        }
                    });
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

				return true;
			}
		});

		EditTextPreference maxFileCache = (EditTextPreference) findPreference("maxFileCache");

		maxFileCache.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		maxFileCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				((EditTextPreference) preference).getEditText().setText(PreferenceManager.getDefaultSharedPreferences(mctx).getString("maxFileCache","200"));
                return false;
			}
		});

		Preference about = findPreference("aboutDialog");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(mctx).inflate(R.layout.about, null);
                String versionName = "";

                try {
                     versionName = mctx.getPackageManager().getPackageInfo(mctx.getPackageName(), 0).versionName;
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
        findPreference("clearapk").setSummary(getString(R.string.clearcontent_sum)+
                " ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[0]))+")");
        findPreference("clearcache").setSummary(getString(R.string.clearcache_sum)+
                " ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[1]))+")");
	}

	public boolean deleteDirectory(File path) {
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
}
