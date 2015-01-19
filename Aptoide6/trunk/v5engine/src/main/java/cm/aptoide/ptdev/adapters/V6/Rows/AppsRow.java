package cm.aptoide.ptdev.adapters.V6.Rows;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.adapters.V6.Displayable;
import cm.aptoide.ptdev.adapters.V6.Holders.AppViewHolder;
import cm.aptoide.ptdev.adapters.V6.Holders.HeaderViewHolder;
import cm.aptoide.ptdev.webservices.Response;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by asantos on 09-12-2014.
 */
public class AppsRow implements Displayable {

    private final Context context;
    public String header;
    public String widgetid;
    public List<Response.ListApps.Apk> apks = new ArrayList<Response.ListApps.Apk>(3);
    public String widgetrefid;
    //private Picasso picasso;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean enabled = true;

    public AppsRow(Context context) {
        //picasso = Picasso.with(context);
        //picasso.setIndicatorsEnabled(true);
        this.context = context;
    }

    public void addItem(Response.ListApps.Apk apk){
        apks.add(apk);
    }


    @Override
    public String toString() {
        return "Row:" + header + " enabled: " + String.valueOf(enabled);
    }

    @Override
    public int getViewType() {
        return apks.size();
    }

    @Override
    public long getHeaderId() {

        if(enabled){
            return Math.abs(header.hashCode());
        }else{
            return -1;
        }

    }


    @Override
    public void bindView(RecyclerView.ViewHolder vh) {

        AppViewHolder viewHolder = (AppViewHolder) vh;

        if(!apks.isEmpty()) {
            viewHolder.name.setText(apks.get(0).name);
            String icon = apks.get(0).icon;
            viewHolder.version.setText(context.getString(R.string.X_download_number, withSuffix(String.valueOf(apks.get(0).downloads))));
            if(apks.get(0).rating !=null ){
                viewHolder.app_rating.setRating(apks.get(0).rating.floatValue());
            }else{
                viewHolder.app_rating.setRating(0                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           );
            }

            if (icon.contains("_icon")) {
                String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                icon = splittedUrl[0] +"_"+ Aptoide.iconSize + "." + splittedUrl[1];
            }

            ImageLoader.getInstance().displayImage(icon, viewHolder.icon);
            viewHolder.itemView.setClickable(true);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, Aptoide.getConfiguration().getAppViewActivityClass());
                    i.putExtra("fromRelated", true);
                    i.putExtra("md5sum", apks.get(0).md5sum);
                    i.putExtra("repoName", apks.get(0).store_name);
                    i.putExtra("download_from", "recommended_apps");
                    context.startActivity(i);
                }
            });



        }

        //picasso.load(icon).into(itemViewHolder.icon);



    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder) {
        viewHolder.tv.setText(header);
    }
}
