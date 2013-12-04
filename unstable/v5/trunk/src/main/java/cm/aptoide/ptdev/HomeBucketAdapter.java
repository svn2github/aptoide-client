package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
public class HomeBucketAdapter extends BucketListAdapter<HomeItem> {

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

        HomeItem item = currentElement;

        holder.name.setText(item.getName());
        holder.category.setText(item.getCategory());
        ImageLoader.getInstance().displayImage(item.getIcon(), holder.icon);


        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), AppViewActivity.class));
            }
        });


        return v;
    }


    static class ViewHolder{
        TextView name;
        TextView category;
        ImageView icon;
    }

}
