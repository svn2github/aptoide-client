package cm.aptoide.ptdev;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 12-11-2013
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class CategoryAdapter extends BaseAdapter {


    private final Context context;
    ArrayList<Category> items;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        items = categories;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Category getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = LayoutInflater.from(context).inflate(R.layout.row_item_category_first_level_list, null);

        ((TextView)v.findViewById(R.id.category_first_level_name)).setText(getItem(position).getName());
        ((TextView)v.findViewById(R.id.category_first_level_number)).setText(String.valueOf(getItem(position).getAppsNumber()));
        ImageView icon = (ImageView) v.findViewById(R.id.category_first_level_icon);
        if(getItem(position).getName().equals("Applications")){
            icon.setImageResource(R.drawable.cat_applications);
        } else if(getItem(position).getName().equals("Games")){
            icon.setImageResource(R.drawable.cat_games);
        } else if(getItem(position).getName().equals("Top Apps")){
            icon.setImageResource(R.drawable.cat_top_apps);
        } else if(getItem(position).getName().equals("Latest Apps")){
            icon.setImageResource(R.drawable.cat_latest);
        } else {
            icon.setImageResource(R.drawable.cat_custom);
        }

        return v;
    }
}
