package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import cm.aptoide.ptdev.adapters.UninstallHelper;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.io.File;

/**
 * Created by brutus on 27-12-2013.
 */
public class UninstallRetainFragment extends Fragment {


    private ActionBarActivity activity;

    private String appName;
    private String packageName;
    private String versionName;
    private String iconPath;

    private String versionToDowngrade;
    private RollBackItem.Action rollBackAction;

    private long id;

    public UninstallRetainFragment(String appName, String packageName, String versionName, String iconPath) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.iconPath = iconPath;
        this.rollBackAction = RollBackItem.Action.UNINSTALLING;
    }

    public UninstallRetainFragment(long id) {
        this.id = id;
        this.rollBackAction = RollBackItem.Action.UNINSTALLING;
    }

    public UninstallRetainFragment(String appName, String packageName, String versionName, String versionToDowngrade, String iconPath) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionToDowngrade = versionToDowngrade;
        this.iconPath = iconPath;
        this.rollBackAction = RollBackItem.Action.DOWNGRADING;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Log.d("Aptoide-Uninstaller", "Uninstalling");

        new UninstallTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ActionBarActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private class UninstallTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
        }

        @Override
        protected Void doInBackground(Void... params) {

            Database db = new Database(Aptoide.getDb());
            RollBackItem rollBackItem;
            String apkMd5;

            try {

                if(id>0){
                    Cursor c = db.getApkInfo(id);
                    packageName = c.getString(c.getColumnIndex("package_name"));
                    appName = c.getString(c.getColumnIndex("name"));
                    versionName = c.getString(c.getColumnIndex("version_name"));
                    String icon = c.getString(c.getColumnIndex("icon"));
                    String repoIconPath = c.getString(c.getColumnIndex("iconpath"));
                    iconPath = repoIconPath + icon;
                }


                switch (rollBackAction) {
                    case DOWNGRADING:

                        apkMd5 = calcApkMd5(packageName);

                        rollBackItem = new RollBackItem(appName, packageName, versionToDowngrade, versionName, iconPath, null, apkMd5, null, "");
                        break;

                    default:
                        apkMd5 = db.getUnistallingActionMd5(packageName);

                        if (db.getUnistallingActionMd5(packageName) == null) {
                            apkMd5 = calcApkMd5(packageName);
                        }
                        rollBackItem = new RollBackItem(appName, packageName, versionName, null, iconPath, null, apkMd5, RollBackItem.Action.UNINSTALLING, "");
                        break;
                }

                db.insertRollbackAction(rollBackItem);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (activity != null) {
                if(rollBackAction == RollBackItem.Action.DOWNGRADING) {
                    UninstallHelper.uninstall(activity, packageName, true);
                } else {
                    UninstallHelper.uninstall(activity, packageName, false);
                }
            }

            DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
            if(pd!=null){
                pd.dismissAllowingStateLoss();
            }

            getFragmentManager().beginTransaction().remove(UninstallRetainFragment.this).commit();
        }

        private String calcApkMd5(String packageName) throws PackageManager.NameNotFoundException {
                String sourceDir = activity.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.sourceDir;
                File apkFile = new File(sourceDir);
                return AptoideUtils.Algorithms.md5Calc(apkFile);
        }

    }
}