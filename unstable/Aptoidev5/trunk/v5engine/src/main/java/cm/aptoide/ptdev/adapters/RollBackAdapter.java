package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.schema.Schema;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tdeus
 * Date: 9/18/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollBackAdapter extends CursorAdapter {

    public RollBackAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int type = getItemViewType(cursor.getPosition());
        View v = null;

                v = LayoutInflater.from(context).inflate(R.layout.row_app_rollback, parent, false);


        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {



                RollBackViewHolder holder = (RollBackViewHolder) view.getTag();
                if (holder == null) {
                    holder = new RollBackViewHolder();
                    holder.name = (TextView) view.findViewById(R.id.app_name);
                    holder.icon = (ImageView) view.findViewById(R.id.app_icon);
                    holder.version = (TextView) view.findViewById(R.id.app_version);
                    holder.action = (TextView) view.findViewById(R.id.ic_action);
                    //holder.timestamp = view.findViewById(R.id.)
                    view.setTag(holder);
                }


                // holder.timestamp.setText(cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_ACTION)));

                holder.name.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_NAME))));


                ImageLoader.getInstance().displayImage(cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_ICONPATH)), holder.icon);
                //holder.icon.setImageBitmap(bitmap);

                holder.version.setText(cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_VERSION)));
                //holder.timestamp.setText(cursor.getString(cursor.getColumnIndex(Schema.Rollback.COLUMN_TIMESTAMP)));



    }

    public static class RollBackViewHolder {

        public TextView action;
        public TextView name;
        public ImageView icon;
        public TextView version;
        public TextView timestamp;

    }
}
