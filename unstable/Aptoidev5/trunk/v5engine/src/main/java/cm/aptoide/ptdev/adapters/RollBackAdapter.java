package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.RollBackItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: tdeus
 * Date: 9/18/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollBackAdapter extends CursorAdapter {

    private final Context context;

    public RollBackAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        this.context = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int type = getItemViewType(cursor.getPosition());
        View v = null;

        v = LayoutInflater.from(context).inflate(R.layout.row_app_rollback, parent, false);


        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


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


        final String name = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_NAME));
        holder.name.setText(Html.fromHtml(name));
        final String icon = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_ICONPATH));
        ImageLoader.getInstance().displayImage(icon, holder.icon);
        final String versionName = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_VERSION));
        holder.version.setText(versionName);

        final String appState = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_ACTION));
        holder.appState.setText(appState);
        final String packageName = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_APKID));
        final String md5sum = cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_MD5));
        holder.action.setText(getActionFromState(appState));
        holder.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RollBackItem.Action action = RollBackItem.Action.valueOf(appState.toUpperCase(Locale.ENGLISH));

                switch (action){
                    case INSTALLED:
                        UninstallHelper.uninstall(context, packageName);
                        new Database(Aptoide.getDb()).insertRollbackAction(new RollBackItem(name, packageName, versionName, null, icon , null, md5sum, RollBackItem.Action.UNINSTALLING ));
                        break;
                    case UPDATED:
                    case UNINSTALLED:

                        Intent i = new Intent(context, AppViewActivity.class);
                        i.putExtra("fromRollback", true);
                        i.putExtra("md5sum", md5sum);
                        context.startActivity(i);

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

    private static String getActionFromState(String appState) {
        if(RollBackItem.Action.INSTALLED.toString().equals(appState)) {
            return "Uninstall";
        } else if(RollBackItem.Action.UNINSTALLED.toString().equals(appState)) {
            return "Reinstall";
        } else if(RollBackItem.Action.UPDATED.toString().equals(appState)) {
            return "Revert";
        } else {
            return "";
        }
    }
}
