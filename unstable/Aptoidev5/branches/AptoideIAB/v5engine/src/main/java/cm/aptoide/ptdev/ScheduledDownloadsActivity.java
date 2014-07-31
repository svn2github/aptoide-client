/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromMd5;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.HashMap;

public class ScheduledDownloadsActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>, ScheduledDownloadsDialog.DialogCallback {

    private ListView lv;
    private Database db;
    private CursorAdapter adapter;
    private HashMap<Long, ScheduledDownload> scheduledDownloadsHashMap = new HashMap<Long, ScheduledDownload>();
    private DownloadService downloadService;
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private int i;
    private boolean showDownloadAll;

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_sch_downloads);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.setting_schdwntitle));

        lv = (ListView) findViewById(android.R.id.list);
        lv.setDivider(null);
        db = new Database(Aptoide.getDb());
        bindService(new Intent(this, DownloadService.class), conn, Context.BIND_AUTO_CREATE);


        adapter = new CursorAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {

            @Override
            public View newView(Context context, Cursor arg1, ViewGroup arg2) {
                return LayoutInflater.from(context).inflate(R.layout.row_sch_download, null);
            }

            @Override
            public void bindView(View convertView, Context arg1, Cursor c) {
                // Planet to display
                ScheduledDownload scheduledDownload = scheduledDownloadsHashMap.get(c.getLong(c.getColumnIndex("_id")));

                // The child views in each row.
                CheckBox checkBoxScheduled;
                TextView textViewName;
                TextView textViewVersion;
                ImageView imageViewIcon;

                // Create a new row view
                if (convertView.getTag() == null) {

                    // Find the child views.
                    textViewName = (TextView) convertView.findViewById(R.id.name);
                    textViewVersion = (TextView) convertView.findViewById(R.id.appversion);
                    checkBoxScheduled = (CheckBox) convertView.findViewById(R.id.schDwnChkBox);
                    imageViewIcon = (ImageView) convertView.findViewById(R.id.appicon);
                    // Optimization: Tag the row with it's child views, so we don't have to
                    // call findViewById() later when we reuse the row.
                    convertView.setTag(new Holder(textViewName, textViewVersion, checkBoxScheduled, imageViewIcon));

                    // If CheckBox is toggled, update the planet it is tagged with.
                    checkBoxScheduled.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v;
                            ScheduledDownload schDownload = (ScheduledDownload) cb.getTag();
                            schDownload.setChecked(cb.isChecked());
                        }
                    });
                }
                // Reuse existing row view
                else {
                    // Because we use a ViewHolder, we avoid having to call findViewById().
                    Holder viewHolder = (Holder) convertView.getTag();
                    checkBoxScheduled = viewHolder.checkBoxScheduled;
                    textViewVersion = viewHolder.textViewVersion;
                    textViewName = viewHolder.textViewName;
                    imageViewIcon = viewHolder.imageViewIcon;
                }


                // Tag the CheckBox with the Planet it is displaying, so that we can
                // access the planet in onClick() when the CheckBox is toggled.
                checkBoxScheduled.setTag(scheduledDownload);

                // Display planet data
                checkBoxScheduled.setChecked(scheduledDownload.isChecked());
                textViewName.setText(scheduledDownload.getName());
                textViewVersion.setText(scheduledDownload.getVername());

                ImageLoader.getInstance().displayImage(scheduledDownload.getIconPath(), imageViewIcon);

            }
        };


        getSupportLoaderManager().initLoader(0, null, this);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View item, int arg2, long arg3) {
                ScheduledDownload scheduledDownload = (ScheduledDownload) ((Holder) item.getTag()).checkBoxScheduled.getTag();
                scheduledDownload.toggleChecked();
                Holder viewHolder = (Holder) item.getTag();
                viewHolder.checkBoxScheduled.setChecked(scheduledDownload.isChecked());
            }

        });

        if(getIntent().hasExtra("downloadAll")) {
            ScheduledDownloadsDialog pd = new ScheduledDownloadsDialog();
            pd.show(getSupportFragmentManager(), "installAllScheduled");
        }


        lv.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new SimpleCursorLoader(this) {

            @Override
            public Cursor loadInBackground() {
                return db.getScheduledDownloads();
            }

        };
    }




    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
        scheduledDownloadsHashMap.clear();
        if (c.getCount() == 0) {
            findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
        } else {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            ScheduledDownload scheduledDownload = new ScheduledDownload(c.getLong(c.getColumnIndex("_id")), true);

            scheduledDownload.setApkid(c.getString(c.getColumnIndex("package_name")));
            scheduledDownload.setMd5(c.getString(c.getColumnIndex("md5")));
            scheduledDownload.setName(c.getString(c.getColumnIndex("name")));
            scheduledDownload.setVername(c.getString(c.getColumnIndex("version_name")));
            scheduledDownload.setRepoName(c.getString(c.getColumnIndex("repo_name")));
            scheduledDownload.setIconPath(c.getString(c.getColumnIndex("icon")));

            scheduledDownloadsHashMap.put(c.getLong(c.getColumnIndex("_id")), scheduledDownload);
        }
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scheduled_downloads, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();


        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        } else if (i == R.id.menu_install) {

            if (isAllChecked()) {
                for (Long scheduledDownload : scheduledDownloadsHashMap.keySet()) {
                    if (scheduledDownloadsHashMap.get(scheduledDownload).checked) {
                        final ScheduledDownload schDown = scheduledDownloadsHashMap.get(scheduledDownload);


                        GetApkInfoRequestFromMd5 requestFromMd5 = new GetApkInfoRequestFromMd5(Aptoide.getContext());
                        requestFromMd5.setRepoName(schDown.getRepoName());
                        requestFromMd5.setMd5Sum(schDown.getMd5());

                        spiceManager.execute(requestFromMd5, new RequestListener<GetApkInfoJson>() {
                            @Override
                            public void onRequestFailure(SpiceException spiceException) {

                            }

                            @Override
                            public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {

                                if (getApkInfoJson != null) {
                                    Download download = new Download();
                                    download.setId(schDown.getMd5().hashCode());
                                    download.setName(schDown.getName());
                                    download.setVersion(schDown.getVername());
                                    download.setIcon(schDown.getIconPath());
                                    download.setPackageName(schDown.getApkid());
                                    download.setMd5(schDown.getMd5());
                                    downloadService.startDownloadFromJson(getApkInfoJson, schDown.getId(), download);
                                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Scheduled_Downloads_Installed_Apps");
                                }


                            }
                        });
                    }
                }

            } else {
                Toast toast = Toast.makeText(Aptoide.getContext(), R.string.schDown_nodownloadselect, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (i == R.id.menu_remove) {
            Log.d("ScheduledDownloadsActivity-onOptionsItemSelected", "remove");
            if (isAllChecked()) {
                for (Long scheduledDownload : scheduledDownloadsHashMap.keySet()) {
                    if (scheduledDownloadsHashMap.get(scheduledDownload).checked) {
                        db.deleteScheduledDownload(scheduledDownloadsHashMap.get(scheduledDownload).md5);
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Scheduled_Downloads_Removed_Apps");
                    }
                }
                getSupportLoaderManager().restartLoader(0, null, this);
            }else{
                Toast toast = Toast.makeText(Aptoide.getContext(), R.string.schDown_nodownloadselect, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (i == R.id.menu_invert) {
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Scheduled_Downloads_Inverted_Apps");
            for (Long scheduledDownload : scheduledDownloadsHashMap.keySet()) {
                scheduledDownloadsHashMap.get(scheduledDownload).checked =
                        !scheduledDownloadsHashMap.get(scheduledDownload).checked;
            }
            adapter.notifyDataSetChanged();
        } else if( i == R.id.menu_SendFeedBack){
            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();



    }

    public boolean isAllChecked() {
        if (scheduledDownloadsHashMap.isEmpty()) {
            return false;
        }
        for (Long scheduledDownload : scheduledDownloadsHashMap.keySet()) {
            if (scheduledDownloadsHashMap.get(scheduledDownload).checked) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onOkClick() {
        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        for (Long scheduledDownload : scheduledDownloadsHashMap.keySet()) {

            final ScheduledDownload schDown = scheduledDownloadsHashMap.get(scheduledDownload);
            GetApkInfoRequestFromMd5 requestFromMd5 = new GetApkInfoRequestFromMd5(Aptoide.getContext());
            requestFromMd5.setRepoName(schDown.getRepoName());
            requestFromMd5.setMd5Sum(schDown.getMd5());

            spiceManager.execute(requestFromMd5, new RequestListener<GetApkInfoJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {

                }

                @Override
                public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {
                    if(getApkInfoJson == null) return;
                    Download download = new Download();
                    download.setId(schDown.getId());
                    download.setName(schDown.getName());
                    download.setVersion(schDown.getVername());
                    download.setIcon(schDown.getIconPath());
                    download.setPackageName(schDown.getApkid());
                    download.setMd5(schDown.getMd5());
                    downloadService.startDownloadFromJson(getApkInfoJson, schDown.getId(), download);
                    i++;
                    if(i==scheduledDownloadsHashMap.size()){
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onCancelClick() {
       finish();

    }

    private static class ScheduledDownload {
        private String name = "";

        private String apkid = "";

        private String vername = "";
        private int vercode = 0;
        private boolean checked = false;
        private String iconPath = "";

        private String md5 = "";
        private String repoName;
        private long id;


        public ScheduledDownload(long id, boolean checked) {
            this.id = id;
            this.checked = checked;
        }

        public String getIconPath() {
            return this.iconPath;
        }

        public String getName() {
            return name;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String toString() {
            return name;
        }

        public void toggleChecked() {
            checked = !checked;
        }

        public int getVercode() {
            return vercode;
        }

        public void setVercode(int vercode) {
            this.vercode = vercode;
        }

        public String getApkid() {
            return apkid;
        }

        public void setApkid(String apkid) {
            this.apkid = apkid;
        }

        public String getVername() {
            return vername;
        }

        public void setVername(String vername) {
            this.vername = vername;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public String getRepoName() {
            return repoName;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class Holder {
        public CheckBox checkBoxScheduled;
        public TextView textViewName;
        public TextView textViewVersion;
        public ImageView imageViewIcon;

        public Holder(TextView textView, TextView textViewVersion, CheckBox checkBox, ImageView imageView) {
            this.checkBoxScheduled = checkBox;
            this.textViewName = textView;
            this.textViewVersion = textViewVersion;
            this.imageViewIcon = imageView;
        }
    }

}
