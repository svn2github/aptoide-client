package cm.aptoide.ptdev.adapters.V6.Holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import cm.aptoide.ptdev.R;

/**
 * Created by asantos on 09-12-2014.
 */
public class AppViewHolder extends RecyclerView.ViewHolder{
    public TextView name;
    public TextView version;
    public RatingBar app_rating;
    public ImageView icon;

    public AppViewHolder(View itemView, int viewType, Context context) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.app_name);
        icon = (ImageView) itemView.findViewById(R.id.app_icon);
        version = (TextView) itemView.findViewById(R.id.app_version);
        app_rating = (RatingBar) itemView.findViewById(R.id.app_rating);

    }
}
