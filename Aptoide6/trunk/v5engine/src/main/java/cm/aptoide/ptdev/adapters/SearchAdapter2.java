package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.SearchManager;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.json.SearchJson;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-12-2013
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class SearchAdapter2 extends ArrayAdapter<SearchJson.Results.Apks> {

    final private String sizeString;
    private final LayoutInflater mInflater;
    private Context mContext;

    public SearchAdapter2(Context context, List<SearchJson.Results.Apks> objects) {
        super(context, 0, objects);
        sizeString = IconSizes.generateSizeString(context);
        this.mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.row_app_search_result, parent, false);
        } else {
            view = convertView;
        }

        AppViewHolder holder = (AppViewHolder) view.getTag();

        SearchJson.Results.Apks item = getItem(position);

        String name = item.getName();
        int count = getCount();
        if(holder==null){
            holder = new AppViewHolder();
            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
//            holder.overFlow = (ImageView) view.findViewById(R.id.ic_action);
            holder.appName = (TextView) view.findViewById(R.id.app_name);
            holder.versionName = (TextView) view.findViewById(R.id.app_version);
            holder.rating = (RatingBar) view.findViewById(R.id.app_rating);
            view.setTag(holder);
        }


        holder.rating.setRating(item.getStars());
//        holder.overFlow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FlurryAgent.logEvent("Search_Result_Clicked_On_Popup_Install");
//                showPopup(v, id);
//            }
//        });
        holder.appName.setText(Html.fromHtml(name).toString());
        String icon1 = item.getIconhd();

        if(icon1 == null){
            icon1 = item.getIcon();
        }

        if(icon1.contains("_icon")){
            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
            icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }
        ImageLoader.getInstance().displayImage(icon1,holder.appIcon);
        holder.versionName.setText(item.getVername());

        return view;
    }
//
//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        return LayoutInflater.from(context).inflate(R.layout.row_app_search_result, parent, false);
//    }
//
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        AppViewHolder holder = (AppViewHolder) view.getTag();
//        String name = cursor.getString(cursor.getColumnIndex("name"));
//        int count = cursor.getInt(cursor.getColumnIndex("count"));
//        if(holder==null){
//            holder = new AppViewHolder();
//            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
//            holder.overFlow = (ImageView) view.findViewById(R.id.ic_action);
//            holder.appNameplusversion = (TextView) view.findViewById(R.id.app_name);
//            holder.versionName = (TextView) view.findViewById(R.id.app_version);
//            holder.rating = (RatingBar) view.findViewById(R.id.app_rating);
//            view.setTag(holder);
//        }
//
//        final long id = cursor.getLong(cursor.getColumnIndex("_id"));
//        holder.rating.setRating(cursor.getFloat(cursor.getColumnIndex("rating")));
//        holder.overFlow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopup(v, id);
//            }
//        });
//        holder.appNameplusversion.setText(Html.fromHtml(name).toString());
//        String icon1 = cursor.getString(cursor.getColumnIndex("icon"));
//        String iconpath = cursor.getString(cursor.getColumnIndex("iconpath"));
//        if(icon1.contains("_icon")){
//            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
//            icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
//        }else{
////            holder.appIcon.setBackgroundResource(R.drawable.fab__gradient);
//        }
//        ImageLoader.getInstance().displayImage(iconpath + icon1,holder.appIcon);
//        holder.versionName.setText(cursor.getString(cursor.getColumnIndex("version_name")));
//    }

    public static class AppViewHolder{
        ImageView appIcon;
//        ImageView overFlow;
        TextView appName;
        TextView versionName;
        TextView downloads;
        RatingBar rating;
    }

    public void showPopup(View v, long id) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.setOnMenuItemClickListener(new MenuListener(mContext, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class MenuListener implements PopupMenu.OnMenuItemClickListener{

        Context context;
        long id;

        MenuListener(Context context, long id) {
            this.context = context;
            this.id = id;


        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int i = menuItem.getItemId();

            if (i == R.id.menu_install) {
                ((SearchManager)context).installApp(id);
                Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                FlurryAgent.logEvent("Search_Result_Installed_From_Popup");
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }
}
