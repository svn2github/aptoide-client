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
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.MainActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.HomeItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 27-11-2013
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class HomeBucketAdapter extends BucketListAdapter<HomeItem> implements PopupMenu.OnMenuItemClickListener{

    public HomeBucketAdapter(Activity ctx, List<HomeItem> elements) {

        super(ctx, elements);
        enableAutoMeasure(120);
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
            holder.icon = (ImageView) v.findViewById(R.id.home_icon);
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }

        final HomeItem item = currentElement;

        holder.name.setText(item.getName());
        holder.category.setText(item.getCategory());
        ImageLoader.getInstance().displayImage(item.getIcon(), holder.icon);

        v.findViewById(R.id.ic_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });
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

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_install) {
//            Log.d("HomeBucketAdapter-onMenuItemClick", "installId: "+installId);
//            ((MainActivity)getContext()).installApp(installId);
            return true;
        } else if (i == R.id.menu_schedule) {
            return true;
        } else {
            return false;
        }
    }

    static class ViewHolder{
        TextView name;
        TextView category;
        ImageView icon;
    }

}
