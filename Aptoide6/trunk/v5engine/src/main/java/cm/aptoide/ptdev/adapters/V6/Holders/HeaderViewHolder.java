package cm.aptoide.ptdev.adapters.V6.Holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import cm.aptoide.ptdev.R;

/**
 * Created by asantos on 09-12-2014.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder{
    public TextView tv;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        tv = (TextView) itemView.findViewById(R.id.header);

    }
}