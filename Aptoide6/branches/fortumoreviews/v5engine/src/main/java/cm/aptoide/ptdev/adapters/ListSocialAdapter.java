package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.EnumCategories;
import cm.aptoide.ptdev.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 18-02-2014.
 */
public class ListSocialAdapter extends ArrayAdapter<ListSocialAdapter.SocialObject> {


    public ListSocialAdapter(Context context, int resource, ArrayList<SocialObject> objects) {
        super(context, resource, objects);
    }


    public static class SocialObject{
        public String name;
        public long id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_item_category_first_level_list, parent, false);
        }else{
            view = convertView;
        }

        ((TextView) view.findViewById(R.id.category_first_level_name)).setText(getItem(position).name);
        ((TextView) view.findViewById(R.id.category_first_level_number)).setText("");
        ImageView icon = (ImageView) view.findViewById(R.id.category_first_level_icon);

        int id = (int) getItemId(position);

        switch (id) {
            case EnumCategories.LATEST_COMMENTS:
                icon.setImageResource(R.drawable.cat_comments);
                break;
            case EnumCategories.LATEST_LIKES:
                icon.setImageResource(R.drawable.cat_likes);
                break;
        }


        return view;
    }


    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }
}
