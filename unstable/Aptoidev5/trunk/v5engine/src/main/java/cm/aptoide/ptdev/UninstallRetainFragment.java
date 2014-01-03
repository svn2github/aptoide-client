package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.pm.PackageManager;
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

    public UninstallRetainFragment(String appName, String packageName, String versionName, String iconPath) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.iconPath = iconPath;
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

            try {
                Database db = new Database(Aptoide.getDb());
                String apkMd5 = db.getUnistallingActionMd5(packageName);

                if (db.getUnistallingActionMd5(packageName) == null) {
                    String sourceDir = activity.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.sourceDir;
                    File apkFile = new File(sourceDir);
                    apkMd5 = AptoideUtils.Algorithms.md5Calc(apkFile);
                }

                RollBackItem rollBackItem = new RollBackItem(appName, packageName, versionName, null, iconPath, null, apkMd5, RollBackItem.Action.UNINSTALLING);
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
                UninstallHelper.uninstall(activity, packageName);
            }
            DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
            pd.dismiss();
            getFragmentManager().beginTransaction().remove(UninstallRetainFragment.this).commit();
        }
    }
}