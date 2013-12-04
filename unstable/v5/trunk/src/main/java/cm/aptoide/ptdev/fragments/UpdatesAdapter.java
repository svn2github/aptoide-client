package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.schema.Schema;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 20-11-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class UpdatesAdapter extends CursorAdapter {
    public UpdatesAdapter(SherlockFragmentActivity sherlockActivity) {
        super(sherlockActivity, null, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0){
            return 0;
        }

        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int type = getItemViewType(cursor.getPosition());
        View v = null;
        switch (type){
            case 0:
               v = LayoutInflater.from(context).inflate(R.layout.separator_updates, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_update, parent, false);
            break;
        }

        return v;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position)!=0;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int type = getItemViewType(cursor.getPosition());

        String name = cursor.getString(cursor.getColumnIndex("name"));

        switch (type){
            case 0:

                TextView tv = (TextView) view.findViewById(R.id.separator_label);
                tv.setText(name);

                break;
            case 1:

                AppViewHolder holder = (AppViewHolder) view.getTag();

                if(holder==null){
                    holder = new AppViewHolder();
                    holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
                    holder.appName = (TextView) view.findViewById(R.id.app_name);
                    view.setTag(holder);
                }



                holder.appName.setText(name);
                holder.appIcon.setImageResource(R.drawable.ic_launcher);

                break;

        }



    }

    public static class AppViewHolder{
        ImageView appIcon;
        ImageView overFlow;
        TextView appName;
        TextView versionName;
        TextView downloads;
        TextView rating;
    }
}
