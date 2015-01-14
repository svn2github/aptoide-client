/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoidetv.pt;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import cm.aptoidetv.pt.WebServices.old.AptoideUtils;

public class SettingsWHeaders extends PreferenceActivity {

    public static final String PrefKEYhwspecsChkBox = "hwspecsChkBox";
    public static final String PrefKEYMAXFILECACHE = "maxFileCache";

    public static final boolean PrefKEYhwspecsChkBoxDEFAULT = true;
    public static final String PrefKEYMAXFILECACHEDEFAULT = "200";

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//

/*        findPreference("showAllUpdates").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsResult();
                if(!((CheckBoxPreference)preference).isChecked()){

                }
                return true;
            }
        });*/

  /*

//		Preference hwspecs = (Preference) findPreference("hwspecs");
//		hwspecs.setIntent(new Intent(getBaseContext(), HWSpecActivity.class));
/*		Preference hwSpecs = findPreference("hwspecs");

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




//		if(ApplicationAptoide.PARTNERID!=null){
//			PreferenceScreen preferenceScreen = getPreferenceScreen();
//			Preference etp = preferenceScreen.findPreference("aboutDialog");
//
//			PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("about");
//			preferenceGroup.removePreference(etp);
//			preferenceScreen.removePreference(preferenceGroup);
//
//		}
*/
    }




/*    public static class SettingHWspecs extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_hw);

        }
    }*/
    public static class SettingHWspecs extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Context c = getActivity();
            View v = inflater.inflate(R.layout.hardware_specs, container, false);
            ((TextView)v.findViewById(R.id.hwinfo)).setText(
                    getString(R.string.setting_sdk_version)+ ": "+ AptoideUtils.HWSpecifications.getSdkVer()+"\n" +
                    getString(R.string.setting_screen_size)+ ": "+AptoideUtils.HWSpecifications.getScreenSize(c)+"\n" +
                    getString(R.string.setting_esgl_version)+ ": "+AptoideUtils.HWSpecifications.getGlEsVer(c) +"\n" +
                    getString(R.string.screenCode)+ ": "+AptoideUtils.HWSpecifications.getNumericScreenSize(c) + "/" + AptoideUtils.HWSpecifications.getDensityDpi(c) +"\n" +
                    getString(R.string.cpuAbi)+ ": "+AptoideUtils.HWSpecifications.getCpuAbi() + " " + AptoideUtils.HWSpecifications.getCpuAbi2());

            CheckBox cb = (CheckBox) v.findViewById(R.id.CheckedTextViewFilterAplications);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

            cb.setChecked(prefs.getBoolean(PrefKEYhwspecsChkBox,PrefKEYhwspecsChkBoxDEFAULT));
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean now = !prefs.getBoolean(PrefKEYhwspecsChkBox,PrefKEYhwspecsChkBoxDEFAULT);
                    ((CheckBox)v).setChecked(now);
                    prefs.edit().putBoolean(PrefKEYhwspecsChkBox,now).apply();
                }
            });
            return v;
        }
    }

    public static class SettingClearMemory extends PreferenceFragment {
        String aptoide_path = Defaults.PATH_CACHE;
        String icon_path = Defaults.PATH_CACHE_ICONS;
        Context mctx;
        private boolean unlocked = false;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mctx = getActivity();
            addPreferencesFromResource(R.xml.preferences_mem);
            new GetDirSize().execute(new File(aptoide_path),new File(icon_path));
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

            EditTextPreference maxFileCache = (EditTextPreference) findPreference(PrefKEYMAXFILECACHE);

            maxFileCache.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            maxFileCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((EditTextPreference) preference).getEditText()
                            .setText(PreferenceManager
                            .getDefaultSharedPreferences(mctx)
                            .getString(PrefKEYMAXFILECACHE, PrefKEYMAXFILECACHEDEFAULT));
                    return false;
                }
            });
        }

        public boolean deleteDirectory(File path) {
            if( path.exists() ) {
                File[] files = path.listFiles();
                if (files != null)
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
                        return dir.length();
                    }
                    for (File file : dir.listFiles()) {
                        size += getDirSize(file);
                    }

                }catch (Exception e){
                    Log.e("pois", "GetDirSize : "+ e.toString());
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
    }
    public static class FragmentAbout extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.about, container, false);
            String versionName;
            try {
                versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                ( (TextView) v.findViewById(R.id.aptoide_version) ).setText("Version " + versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return inflater.inflate(R.layout.about, container, false);
        }
    }
}
