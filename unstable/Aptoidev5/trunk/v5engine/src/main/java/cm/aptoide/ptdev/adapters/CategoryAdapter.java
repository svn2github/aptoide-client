package cm.aptoide.ptdev.adapters;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.Category;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

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
    public void bindView(View view, Context context, Cursor cursor) {

        String name = cursor.getString(cursor.getColumnIndex("name"));
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        int type = getItemViewType(cursor);


        switch (type) {

            case 0:
                AppViewHolder holder = (AppViewHolder) view.getTag();

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
//                    holder.appIcon.setBackgroundResource(R.drawable.);
                }
                ImageLoader.getInstance().displayImage(iconpath + icon1,holder.appIcon);
                holder.versionName.setText(cursor.getString(cursor.getColumnIndex("version_name")));

                break;
            case 1:
                ImageView icon = (ImageView) view.findViewById(R.id.category_first_level_icon);
                ((TextView) view.findViewById(R.id.category_first_level_name)).setText(name);
                ((TextView) view.findViewById(R.id.category_first_level_number)).setText(String.valueOf(count));
                if (name.equals("Applications")) {
                    icon.setImageResource(R.drawable.cat_applications);
                } else if (name.equals("Games")) {
                    icon.setImageResource(R.drawable.cat_games);
                } else if (name.equals("Top Apps")) {
                    icon.setImageResource(R.drawable.cat_top_apps);
                } else if (name.equals("Latest Apps")) {
                    icon.setImageResource(R.drawable.cat_latest);
                }else if (name.equals("Business")) {
                    icon.setImageResource(R.drawable.business);
                } else if (name.equals("Books & Reference")) {
                    icon.setImageResource(R.drawable.books_and_reference);
                } else if (name.equals("Comics")) {
                    icon.setImageResource(R.drawable.comics);
                } else if (name.equals("Communication")) {
                    icon.setImageResource(R.drawable.communication);
                } else if (name.equals("Demo")) {
                    icon.setImageResource(R.drawable.demo);
                } else if (name.equals("Education")) {
                    icon.setImageResource(R.drawable.education);
                } else if (name.equals("Entertainment")) {
                    icon.setImageResource(R.drawable.entertainment);
                } else if (name.equals("Finance")) {
                    icon.setImageResource(R.drawable.finance);
                } else if (name.equals("Health")) {
                    icon.setImageResource(R.drawable.health);
                } else if (name.equals("Health & Fitness")) {
                    icon.setImageResource(R.drawable.health_and_fitness);
                } else if (name.equals("Libraries & Demo")) {
                    icon.setImageResource(R.drawable.libraries_and_demo);
                } else if (name.equals("Lifestyle")) {
                    icon.setImageResource(R.drawable.lifestyle);
                } else if (name.equals("Media & Video")) {
                    icon.setImageResource(R.drawable.media_and_video);
                } else if (name.equals("Medical")) {
                    icon.setImageResource(R.drawable.medical);
                } else if (name.equals("Music & Audio")) {
                    icon.setImageResource(R.drawable.music_and_audio);
                } else if (name.equals("Multimedia")) {
                    icon.setImageResource(R.drawable.multimedia);
                } else if (name.equals("News & Magazines")) {
                    icon.setImageResource(R.drawable.news_and_magazines);
                } else if (name.equals("News & Weather")) {
                    icon.setImageResource(R.drawable.news_and_weather);
                } else if (name.equals("Personalization")) {
                    icon.setImageResource(R.drawable.personalization);
                } else if (name.equals("Photography")) {
                    icon.setImageResource(R.drawable.photography);
                } else if (name.equals("Productivity")) {
                    icon.setImageResource(R.drawable.productivity);
                } else if (name.equals("Reference")) {
                    icon.setImageResource(R.drawable.reference);
                } else if (name.equals("Shopping")) {
                    icon.setImageResource(R.drawable.shopping);
                } else if (name.equals("Social")) {
                    icon.setImageResource(R.drawable.social);
                } else if (name.equals("Software Libraries")) {
                    icon.setImageResource(R.drawable.software_libraries);
                } else if (name.equals("Sports")) {
                    icon.setImageResource(R.drawable.sports);
                } else if (name.equals("Themes")) {
                    icon.setImageResource(R.drawable.themes);
                } else if (name.equals("Tools")) {
                    icon.setImageResource(R.drawable.tools);
                } else if (name.equals("Transport")) {
                    icon.setImageResource(R.drawable.transport);
                } else if (name.equals("Transportation")) {
                    icon.setImageResource(R.drawable.transportation);
                } else if (name.equals("Travel")) {
                    icon.setImageResource(R.drawable.travel);
                } else if (name.equals("Travel & Local")) {
                    icon.setImageResource(R.drawable.travel_and_local);
                } else if (name.equals("Weather")) {
                    icon.setImageResource(R.drawable.weather);
                } else if (name.equals("Arcade & Action")) {
                    icon.setImageResource(R.drawable.arcade_and_action);
                } else if (name.equals("Brain & Puzzle")) {
                    icon.setImageResource(R.drawable.brain_and_puzzle);
                } else if (name.equals("Cards & Casino")) {
                    icon.setImageResource(R.drawable.cards_and_casino);
                } else if (name.equals("Casual")) {
                    icon.setImageResource(R.drawable.casual);
                } else if (name.equals("Racing")) {
                    icon.setImageResource(R.drawable.racing);
                } else if (name.equals("Sports Games")) {
                    icon.setImageResource(R.drawable.sports_games);
                }  else {
                    icon.setImageResource(R.drawable.custom_categ_green);
                }
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

    public static class CategoryViewHolder{
        ImageView catIcon;
        TextView catName;
        TextView appsCount;
    }
}
