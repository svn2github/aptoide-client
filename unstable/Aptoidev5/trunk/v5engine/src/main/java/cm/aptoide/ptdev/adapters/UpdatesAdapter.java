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
import cm.aptoide.ptdev.MainActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 20-11-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class UpdatesAdapter extends CursorAdapter {

    final private String sizeString;


    public UpdatesAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        sizeString = IconSizes.generateSizeString(context);

    }

    @Override
    public int getItemViewType(int position) {
        Cursor c = (Cursor) getItem(position);
        return getItemViewType(c);
    }

    private int getItemViewType(Cursor cursor){
        return cursor.getInt(cursor.getColumnIndex("is_update"));
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
               v = LayoutInflater.from(context).inflate(R.layout.row_app_installed, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_update, parent, false);
            break;
        }

        return v;
    }



    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        int type = getItemViewType(cursor.getPosition());

        String name = cursor.getString(cursor.getColumnIndex("name"));

        AppViewHolder holder = (AppViewHolder) view.getTag();


        if(holder==null){
            holder = new AppViewHolder();
            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
            holder.overFlow = (ImageView) view.findViewById(R.id.manage_icon);
            holder.appName = (TextView) view.findViewById(R.id.app_name);
            holder.versionName = (TextView) view.findViewById(R.id.app_version);
            view.setTag(holder);
        }



        holder.appName.setText(Html.fromHtml(name).toString());
        String icon1 = cursor.getString(cursor.getColumnIndex("icon"));
        String iconpath = cursor.getString(cursor.getColumnIndex("iconpath"));
        if(icon1.contains("_icon")){
            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
            icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }
        ImageLoader.getInstance().displayImage(iconpath + icon1,holder.appIcon);
        holder.versionName.setText(cursor.getString(cursor.getColumnIndex("version_name")));

        switch (type){
            case 1:

                final long id = cursor.getLong(cursor.getColumnIndex("_id"));

                holder.overFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)context).installApp(id);
                    }
                });
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
