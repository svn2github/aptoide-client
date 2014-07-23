package cm.aptoide.ptdev;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.IconSizes;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class ExcludedUpdatesActivity extends ActionBarActivity {
    ArrayList<ExcludedUpdate> excludedUpdates = new ArrayList<ExcludedUpdate>();

    private ArrayAdapter<ExcludedUpdate> adapter;
    private ListView lv;
    private TextView tv_no_excluded_downloads;
    private Database db = new Database(Aptoide.getDb());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_excluded_uploads);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.excluded_updates));

        lv = (ListView) findViewById(R.id.excluded_updates_list);
        lv.setDivider(null);
        tv_no_excluded_downloads = (TextView) findViewById(R.id.tv_no_excluded_downloads);
        final String sizeString = IconSizes.generateSizeString(this);
        adapter = new ArrayAdapter<ExcludedUpdate>(this, 0, excludedUpdates) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView == null) {
                    v = newView(parent);
                } else {
                    v = convertView;
                }
                bindView(v, position);
                return v;
            }

            public View newView(ViewGroup arg2) {
                return LayoutInflater.from(getContext()).inflate(R.layout.row_excluded_update, null);
            }
            @Override
            public long getItemId(int position) {
                return position;
            }

            public void bindView(View convertView, int c) {
                ExcludedUpdate excludedUpdate = getItem(c);

                CheckBox cb_exclude;
                ImageView icon;
                TextView tv_name;
                TextView tv_vercode;
                TextView tv_apkid;

                if (convertView.getTag() == null) {
                    icon = (ImageView) convertView.findViewById(R.id.app_icon);
                    tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                    tv_vercode = (TextView) convertView.findViewById(R.id.tv_vercode);
                    tv_apkid = (TextView) convertView.findViewById(R.id.tv_apkid);
                    cb_exclude = (CheckBox) convertView.findViewById(R.id.cb_exclude);
                    convertView.setTag(new ExcludedUpdatesHolder(icon, tv_name, tv_apkid, tv_vercode, cb_exclude));

                    cb_exclude.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v;
                            ExcludedUpdate excludedUpdateItem = (ExcludedUpdate) cb.getTag();
                            excludedUpdateItem.setChecked(cb.isChecked());
                        }
                    });
                } else {
                    ExcludedUpdatesHolder viewHolder = (ExcludedUpdatesHolder) convertView.getTag();
                    cb_exclude = viewHolder.cb_exclude;
                    icon = viewHolder.icon;
                    tv_vercode = viewHolder.tv_vercode;
                    tv_name = viewHolder.tv_name;
                    tv_apkid = viewHolder.tv_apkid;
                }
                cb_exclude.setTag(excludedUpdate);
                cb_exclude.setChecked(cb_exclude.isChecked());

                String iconString = excludedUpdate.getIcon();

                if (iconString.contains("_icon")) {
                    String[] splittedUrl = iconString.split("\\.(?=[^\\.]+$)");
                    iconString = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(iconString, icon);
                tv_name.setText(excludedUpdate.getName());
                tv_vercode.setText("" + excludedUpdate.getVercode());
                tv_apkid.setText(excludedUpdate.getApkid());
            }
        };

        redraw();

//        Button bt_restore_updates = (Button) findViewById(R.id.restore_update);
//        bt_restore_updates.setText(getString(R.string.restore_updates));
//        bt_restore_updates.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                if (isAllChecked()) {
//                    for (ExcludedUpdate excludedUpdate : excludedUpdates) {
//                        if (excludedUpdate.checked) {
//                            db.deleteFromExcludeUpdate(excludedUpdate.apkid, excludedUpdate.vercode);
//                        }
//                    }
//                    redraw();
//                } else {
//                    Toast toast = Toast.makeText(ExcludedUpdatesActivity.this,
//                            R.string.no_excluded_updates_selected, Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
        lv.setAdapter(adapter);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_excluded_updates, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        } else if (i == R.id.menu_remove) {
            if (isAllChecked()) {
                for (ExcludedUpdate excludedUpdate : excludedUpdates) {
                    if (excludedUpdate.checked) {
                        db.deleteFromExcludeUpdate(excludedUpdate.apkid, excludedUpdate.vercode);
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Excluded_Updates_Removed_Update_From_List");
                    }
                }
                redraw();
            } else {
                Toast toast = Toast.makeText(ExcludedUpdatesActivity.this,
                        R.string.no_excluded_updates_selected, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private static class ExcludedUpdate {
        private String name = "" ;
        private int vercode = 0;
        private String apkid = "";
        private boolean checked = false;
        private String icon;

        public ExcludedUpdate(String name, String apkid, String icon, int vercode) {
            this.name = name;
            this.apkid = apkid;
            this.vercode = vercode;
            this.icon = icon;
        }

        public boolean isChecked() {
            return checked;
        }
        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getName() {
            return name;
        }

        public int getVercode() {
            return vercode;
        }

        public String getApkid() {
            return apkid;
        }

        public String toString(){ return "Name: " + name + ", vercode: " + vercode + ", apkid: " + apkid; }

        public String getIcon() { return icon; }
    }

    private void redraw() {
        Cursor c = db.getExcludedApks();
        excludedUpdates.clear();
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            ExcludedUpdate excludedUpdate = new ExcludedUpdate(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("package_name")), c.getString(c.getColumnIndex("iconpath")), c.getInt(c.getColumnIndex("vercode")));
            excludedUpdates.add(excludedUpdate);
        }
        c.close();
        adapter.notifyDataSetChanged();

        Log.d("ExcludedUpdatesActivity", "excluded updates: " + excludedUpdates.toString());

        if(!adapter.isEmpty()){
            tv_no_excluded_downloads.setVisibility(View.GONE);
        }else{
            tv_no_excluded_downloads.setVisibility(View.VISIBLE);
        }

    }

    private boolean isAllChecked(){
        if(adapter.isEmpty()){
            return false;
        }
        for(ExcludedUpdate excludedUpdate: excludedUpdates){
            if (excludedUpdate.checked){
                return true;
            }
        }
        return false;
    }
    private static class ExcludedUpdatesHolder {
        public CheckBox cb_exclude;
        public TextView tv_name;
        public TextView tv_apkid;
        public TextView tv_vercode;
        public ImageView icon;

        public ExcludedUpdatesHolder(ImageView icon, TextView tv_name, TextView tv_apkid, TextView tv_vercode, CheckBox cb_exclude) {
            this.cb_exclude = cb_exclude;
            this.icon = icon;
            this.tv_name = tv_name;
            this.tv_apkid = tv_apkid;
            this.tv_vercode = tv_vercode;
        }
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
