package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.RollBackItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: tdeus
 * Date: 9/18/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollBackAdapter extends CursorAdapter {

    private final RollbackActivity activity;

    public RollBackAdapter(RollbackActivity context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        this.activity = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int type = getItemViewType(cursor.getPosition());
        View v = null;

        v = LayoutInflater.from(context).inflate(R.layout.row_app_rollback, parent, false);


        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {


        RollBackViewHolder holder = (RollBackViewHolder) view.getTag();
        if (holder == null) {
            holder = new RollBackViewHolder();
            holder.name = (TextView) view.findViewById(R.id.app_name);
            holder.icon = (ImageView) view.findViewById(R.id.app_icon);
            holder.version = (TextView) view.findViewById(R.id.app_version);
            holder.appState = (TextView) view.findViewById(R.id.app_state);
            holder.action = (TextView) view.findViewById(R.id.ic_action);
            view.setTag(holder);
        }


        final String name = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_NAME));
        holder.name.setText(Html.fromHtml(name));
        final String icon = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_ICONPATH));
        ImageLoader.getInstance().displayImage(icon, holder.icon);
        final String versionName = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_VERSION));
        holder.version.setText(versionName);
        final long timeStamp = cursor.getLong(cursor.getColumnIndex("real_timestamp"));

        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date date = new Date(timeStamp * 1000);

        final String appState = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_ACTION));

        String appStateString = null;
        int appNameRes = 0;
        try {
            appNameRes = EnumRollbackState.states.get(cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_ACTION)));
        } catch (Exception e) {
            appStateString = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_ACTION));
            Log.d("Start-RollbackAdapter", "RollbackAdapter App state " + appStateString);
        }
        if (appStateString == null) {
            appStateString = context.getString(appNameRes);
        }


        holder.appState.setText(appStateString+" "+context.getString(R.string.at_time, timeFormat.format(date)));

        final String packageName = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_APKID));
        final String md5sum = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_MD5));
        final String previousVersion = cursor.getString(cursor.getColumnIndex(Schema.RollbackTbl.COLUMN_PREVIOUS_VERSION));

        holder.action.setText(getActionFromState(appState, context));
        holder.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RollBackItem.Action action = RollBackItem.Action.valueOf(appState.toUpperCase(Locale.ENGLISH));

                switch (action){
                    case INSTALLED:
                        Fragment fragment = new UninstallRetainFragment(name, packageName, versionName, icon);
                        activity.getSupportFragmentManager().beginTransaction().add(fragment, "uninstall").commit();
                        break;

                    default:
                        Intent intent = new Intent(context, AppViewActivity.class);
                        intent.putExtra("fromRollback", true);
                        intent.putExtra("md5sum", md5sum);
                        context.startActivity(intent);
                        break;
                }
            }
        });

    }

    public static class RollBackViewHolder {

        public TextView name;
        public ImageView icon;
        public TextView version;
        public TextView appState;
        public TextView action;

    }

    private static String getActionFromState(String appState, Context context) {
        if(RollBackItem.Action.INSTALLED.toString().equals(appState)) {
            return context.getString(R.string.uninstall);
        } else if(RollBackItem.Action.UNINSTALLED.toString().equals(appState)) {
            return context.getString(R.string.reinstall);
        } else if(RollBackItem.Action.UPDATED.toString().equals(appState)) {
            return context.getString(R.string.downgrade);
        } else if(RollBackItem.Action.DOWNGRADED.toString().equals(appState)) {
            return context.getString(R.string.update);
        } else {
            return "";
        }
    }

}
