package cm.aptoide.ptdev.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 12-11-2013
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class CategoryAdapter extends CursorAdapter {


    private final Context context;

    ArrayList<Category> items;
    final private String sizeString;

    public CategoryAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        this.context = context;
        sizeString = IconSizes.generateSizeString(context);
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View v = null;
        int type = getItemViewType(cursor);

        switch (type){
            case 0:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_standard, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_item_category_first_level_list, parent, false);
                break;
        }

        return v;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor c = (Cursor) getItem(position);
        return getItemViewType(c);
    }

    private int getItemViewType(Cursor cursor){
        return cursor.getInt(cursor.getColumnIndex("type"));
    }



    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        String name = cursor.getString(cursor.getColumnIndex("name"));
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        int type = getItemViewType(cursor);
        final int id = cursor.getInt(cursor.getColumnIndex("_id"));

        switch (type) {

            case 0:
                AppViewHolder holder = (AppViewHolder) view.getTag();

                if(holder==null){
                    holder = new AppViewHolder();
                    holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
                    holder.overFlow = (ImageView) view.findViewById(R.id.ic_action);
                    holder.appName = (TextView) view.findViewById(R.id.app_name);
                    holder.versionName = (TextView) view.findViewById(R.id.app_version);
                    holder.rating = (RatingBar) view.findViewById(R.id.app_rating);
                    view.setTag(holder);
                }



                holder.appName.setText(Html.fromHtml(name).toString());
                holder.rating.setRating(cursor.getFloat(cursor.getColumnIndex("rating")));
                String icon1 = cursor.getString(cursor.getColumnIndex("icon"));
                String iconpath = cursor.getString(cursor.getColumnIndex("iconpath"));
                if(icon1.contains("_icon")){
                    String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
                    icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
                }
                ImageLoader.getInstance().displayImage(iconpath + icon1,holder.appIcon);
                holder.versionName.setText(cursor.getString(cursor.getColumnIndex("version_name")));

                holder.overFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(v, id);
                    }
                });
                break;
            case 1:
                ImageView icon = (ImageView) view.findViewById(R.id.category_first_level_icon);

                String categoryName;


                    int res = EnumCategories.getCategoryName(id);

                    if (res > 0) {
                        categoryName = context.getString(res);
                        Log.d("CategoryAdapter-categ", "Category Name: " + categoryName);

                    }else{
                        categoryName = name;
                        Log.d("CategoryAdapter-categ", "Untranslated Category Name: " + categoryName);

                    }

                ((TextView) view.findViewById(R.id.category_first_level_name)).setText(categoryName);

                if(count>0){
                    ((TextView) view.findViewById(R.id.category_first_level_number)).setText(String.valueOf(count));
                }else{
                    ((TextView) view.findViewById(R.id.category_first_level_number)).setText("");
                }

                String repoName = cursor.getString(cursor.getColumnIndex("repo_name")).toUpperCase(Locale.ENGLISH);

                EnumStoreTheme theme;
                try{
                    String themeString = cursor.getString(cursor.getColumnIndex("theme")).toUpperCase(Locale.ENGLISH);

                    theme = EnumStoreTheme.valueOf("APTOIDE_STORE_THEME_" + themeString);
                }catch (Exception e){
                    theme = EnumStoreTheme.APTOIDE_STORE_THEME_ORANGE;
                }


                switch (id) {
                    case EnumCategories.APPLICATIONS:
                        icon.setImageResource(R.drawable.cat_applications);
                        break;
                    case EnumCategories.GAMES:
                        icon.setImageResource(R.drawable.cat_games);
                        break;
                    case EnumCategories.TOP_APPS:
                        icon.setImageResource(R.drawable.cat_top_apps);
                        break;
                    case EnumCategories.LATEST_APPS:
                        icon.setImageResource(R.drawable.cat_latest);
                        break;
                    case EnumCategories.LATEST_LIKES:
                        icon.setImageResource(R.drawable.cat_likes);
                        break;
                    case EnumCategories.LATEST_COMMENTS:
                        icon.setImageResource(R.drawable.cat_comments);
                        break;
                    case EnumCategories.RECOMMENDED_APPS:
                        icon.setImageResource(R.drawable.cat_recommended);
                        break;

                    default:
                        String iconUrl = EnumCategories.getCategoryIcon(id, repoName);
                        if (iconUrl != null) {
                            ImageLoader.getInstance().displayImage(iconUrl, icon);
                        } else {
                            icon.setImageResource(theme.getStoreCategoryDrawable());
                        }
                        break;
                }




                break;


        }
    }

    public void showPopup(View v, long id) {
        PopupMenu popup = new PopupMenu(context, v);
        popup.setOnMenuItemClickListener(new MenuListener(context, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    public static class AppViewHolder{
        ImageView appIcon;
        ImageView overFlow;
        TextView appName;
        TextView versionName;
        TextView downloads;
        RatingBar rating;
    }

    public static class CategoryViewHolder{
        ImageView catIcon;
        TextView catName;
        TextView appsCount;
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
                ((CategoryCallback)context).installApp(id);
                Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }
}
