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
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-12-2013
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class SearchAdapter extends CursorAdapter {

    final private String sizeString;


    public SearchAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        sizeString = IconSizes.generateSizeString(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_app_search_result, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AppViewHolder holder = (AppViewHolder) view.getTag();
        String name = cursor.getString(cursor.getColumnIndex("name"));
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        if(holder==null){
            holder = new AppViewHolder();
            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
            holder.overFlow = (ImageView) view.findViewById(R.id.ic_action);
            holder.appName = (TextView) view.findViewById(R.id.app_name);
            holder.versionName = (TextView) view.findViewById(R.id.app_version);
            view.setTag(holder);
        }


        holder.overFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.appName.setText(Html.fromHtml(name).toString());
        String icon1 = cursor.getString(cursor.getColumnIndex("icon"));
        String iconpath = cursor.getString(cursor.getColumnIndex("iconpath"));
        if(icon1.contains("_icon")){
            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
            icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }else{
            holder.appIcon.setBackgroundResource(R.drawable.fab__gradient);
        }
        ImageLoader.getInstance().displayImage(iconpath + icon1,holder.appIcon);
        holder.versionName.setText(cursor.getString(cursor.getColumnIndex("version_name")));
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
