package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import cm.aptoide.ptdev.adapters.UninstallHelper;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.io.File;

/**
 * Created by brutus on 27-12-2013.
 */
public class UninstallRetainFragment extends Fragment {


    private MainActivity mainActivity;

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

        new UninstallTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    private class UninstallTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Database db = new Database(Aptoide.getDb());
                String apkMd5 = db.getUnistallingActionMd5(packageName);

                if (db.getUnistallingActionMd5(packageName) == null) {
                    String sourceDir = mainActivity.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.sourceDir;
                    File apkFile = new File(sourceDir);
                    apkMd5 = AptoideUtils.Algorithms.md5Calc(apkFile);
                }

                RollBackItem rollBackItem = new RollBackItem(appName, packageName, versionName, iconPath, null, apkMd5, RollBackItem.Action.UNINSTALLING);
                db.insertRollbackAction(rollBackItem);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (mainActivity != null) {
                UninstallHelper.uninstall(mainActivity, packageName);
            }
        }
    }
}