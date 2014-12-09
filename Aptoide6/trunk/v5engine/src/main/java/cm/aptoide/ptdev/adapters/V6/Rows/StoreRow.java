package cm.aptoide.ptdev.adapters.V6.Rows;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.adapters.V6.Holders.StoreViewHolder;
import cm.aptoide.ptdev.model.Store;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by asantos on 09-12-2014.
 */
public class StoreRow extends AppsRow{

    private final Context context;
    public String name;
    public String avatar;
    public int downloads;
    public int appscount;



    public StoreRow(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getViewType() {
        return 1000;
    }

    @Override
    public void bindView(RecyclerView.ViewHolder vh) {
        StoreViewHolder viewHolder = (StoreViewHolder) vh;
        viewHolder.name.setText(name);
        ImageLoader.getInstance().displayImage(avatar, viewHolder.icon);
        viewHolder.store_info.setText(appscount + " " + viewHolder.itemView.getContext().getString(R.string.applications) + " • " + viewHolder.itemView.getContext().getString(R.string.X_download_number, withSuffix(String.valueOf(downloads))));
        viewHolder.addstore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Store store = new Store();
                store.setName(name);
                ((Start) context).startParse(store);
                Toast.makeText(context, context.getString(R.string.store_added), Toast.LENGTH_SHORT).show();
                ((Start) context).setCurrentItem(2);
            }
        });
    }
}
