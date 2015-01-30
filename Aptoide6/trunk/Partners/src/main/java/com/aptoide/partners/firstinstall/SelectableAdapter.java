package com.aptoide.partners.firstinstall;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;


import com.aptoide.partners.R;
import com.aptoide.partners.drawable.FlipDrawable;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 23-01-2015.
 */
public class SelectableAdapter extends RecyclerView.Adapter<SelectableAdapter.ViewHolder> {


    public interface OnItemSelectedListener{
        void onSelect();
    }

    private OnItemSelectedListener onItemSelectedListener;


    private final ArrayList<FirstInstallRow> objects;

    public SelectableAdapter(ArrayList<FirstInstallRow> objects, final OnItemSelectedListener onItemSelectedListener) {

        this.onItemSelectedListener = onItemSelectedListener;
        this.objects = objects;
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                onItemSelectedListener.onSelect();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onChanged();
            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(R.layout.row_firstinstall_wizard, viewGroup, false);

        return new ViewHolder(v);
    }




    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {



        final FirstInstallRow row = objects.get(position);

        Log.d("Aptoide", "OnBind " + !row.isSelected());

        ImageLoader.getInstance().displayImage(row.getIcon(), viewHolder.appIcon);
        viewHolder.appName.setText(row.getAppName());
        if(row.isAnimate()){
            viewHolder.flipDrawable.flipTo(!row.isSelected());
            row.animate(false);
        }else{
            viewHolder.flipDrawable.reset(!row.isSelected());
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.flipDrawable.flip();
                row.setSelected(!row.isSelected());
                onItemSelectedListener.onSelect();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public  ImageView appIcon;
        public  TextView appName;
        public  TextView downloadsAndSize;
        public FlipDrawable flipDrawable;

        public ViewHolder(final View itemView) {
            super(itemView);
            Context context = itemView.getContext();
            Resources resources = context.getResources();
            Drawable drawable = resources.getDrawable(R.drawable.download_icon);
            appIcon = (ImageView) itemView.findViewById(R.id.appIcon);
            appName = (TextView) itemView.findViewById(R.id.appName);
            downloadsAndSize = (TextView) itemView.findViewById(R.id.downloadsAndSize);
            flipDrawable = new FlipDrawable(drawable, 200, 0, 0, resources);
            ImageView checkMarkDownload = (ImageView) itemView.findViewById(R.id.imageView1);
            checkMarkDownload.setImageDrawable(flipDrawable);

        }
    }
}
