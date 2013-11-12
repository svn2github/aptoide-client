package cm.aptoide.ptdev;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

        TextView v = (TextView) LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, null);

        v.setText(getItem(position).getName());

        return v;
    }
}
