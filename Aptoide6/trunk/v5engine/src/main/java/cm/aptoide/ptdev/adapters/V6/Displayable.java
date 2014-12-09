package cm.aptoide.ptdev.adapters.V6;

import android.support.v7.widget.RecyclerView;

import cm.aptoide.ptdev.adapters.V6.Holders.HeaderViewHolder;

/**
 * Created by asantos on 09-12-2014.
 */
public interface Displayable {
    int getViewType();

    long getHeaderId();

    void bindView(RecyclerView.ViewHolder viewHolder);

    void onBindHeaderViewHolder(HeaderViewHolder viewHolder);
}
