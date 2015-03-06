package cm.aptoide.ptdev.adapters.V6.Holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cm.aptoide.ptdev.R;
import info.hoang8f.widget.FButton;

/**
 * Created by asantos on 09-12-2014.
 */
public class StoreViewHolder extends RecyclerView.ViewHolder{

    public TextView name;
    public TextView store_info;

    public ImageView icon;

    public FButton addstore;

    public StoreViewHolder(View itemView, int viewType, Context context) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.store_name);
        icon = (ImageView) itemView.findViewById(R.id.store_avatar);
        store_info = (TextView) itemView.findViewById(R.id.store_info_dwn);
        addstore = (FButton) itemView.findViewById(R.id.add_store_button);
    }
}
