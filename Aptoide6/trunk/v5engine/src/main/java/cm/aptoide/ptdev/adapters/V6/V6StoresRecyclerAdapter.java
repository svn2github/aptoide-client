package cm.aptoide.ptdev.adapters.V6;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.HashMap;
import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.V6.Holders.HeaderViewHolder;
import cm.aptoide.ptdev.adapters.V6.Holders.StoreViewHolder;

/**
 * Created by asantos on 09-12-2014.
 */
public class V6StoresRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<HeaderViewHolder>{

    public Context getContext() {
        return context;
    }

    protected final Context context;
    public final List<Displayable> list;
    public final HashMap<String, Integer> placeholders = new HashMap<>();

    public V6StoresRecyclerAdapter(Context context, List<Displayable> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getViewType();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate;
        inflate = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
        return new StoreViewHolder(inflate, viewType, context);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        list.get(position).bindView(holder);
    }

    @Override
    public long getHeaderId(int position) {
        return list.get(position).getHeaderId();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {

        View inflate = LayoutInflater.from(context).inflate(R.layout.home_separator, viewGroup, false);



        return new HeaderViewHolder(inflate);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
        list.get(position).onBindHeaderViewHolder(viewHolder);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public HashMap<String, Integer> getPlaceholders() {
        return placeholders;
    }

}
