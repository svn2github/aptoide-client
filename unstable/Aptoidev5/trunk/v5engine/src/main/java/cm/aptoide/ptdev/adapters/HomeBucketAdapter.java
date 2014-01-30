package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MainActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 27-11-2013
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class HomeBucketAdapter extends BucketListAdapter<HomeItem> {

    private final String sizeString;

    public HomeBucketAdapter(Activity ctx, List<HomeItem> elements) {

        super(ctx, elements);
        enableAutoMeasure(120);
        sizeString = IconSizes.generateSizeString(ctx);

    }

    @Override
    public int getBucketSize() {
        return super.getBucketSize();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }


    @Override
    protected View bindBucketElement(int position, HomeItem currentElement, View convertView, ViewGroup parent) {
        final View v;


        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            v = LayoutInflater.from(ctx).inflate(R.layout.row_app_home, parent, false);

            holder.category= (TextView) v.findViewById(R.id.app_category);
            holder.name = (TextView) v.findViewById(R.id.app_name);
            holder.icon = (ImageView) v.findViewById(R.id.app_icon);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }

        final HomeItem item = currentElement;

        holder.name.setText(item.getName());
        holder.category.setText(item.getCategory());
        String icon = item.getIcon();

        if(icon.contains("_icon")){
            String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
            icon = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(icon, holder.icon);

//        v.findViewById(R.id.ic_action).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopup(v, item.getId());
//
//            }
//        });
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AppViewActivity.class);
                long id = item.getId();
                i.putExtra("id", id);
                getContext().startActivity(i);
            }
        });







        return v;
    }

    public void showPopup(View v, long id) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(new MenuListener(getContext(), id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class ViewHolder{
        TextView name;
        TextView category;
        ImageView icon;
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
                ((MainActivity)context).installApp(id);
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
