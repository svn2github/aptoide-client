package cm.aptoide.ptdev;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public CategoryAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        this.context = context;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View v = null;
        int type = getItemViewType(cursor);

        switch (type){
            case 0:
                v = LayoutInflater.from(context).inflate(R.layout.row_app, null);

                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_item_category_first_level_list, null);
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
                ((TextView) view.findViewById(R.id.app_name)).setText(name);
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
                } else {
                    icon.setImageResource(R.drawable.cat_custom);
                }
                break;


        }
    }
}
