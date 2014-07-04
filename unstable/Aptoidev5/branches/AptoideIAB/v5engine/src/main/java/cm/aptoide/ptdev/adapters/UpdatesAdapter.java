package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 20-11-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class UpdatesAdapter extends BaseAdapter implements SimpleSectionAdapter.Sectionizer<UpdateItem>{

    final private String sizeString;
    private final Context context;
    private ArrayList<UpdateItem> items;


    public UpdatesAdapter(Context context, ArrayList<UpdateItem> items) {
        this.context = context;
        this.items = items;
        sizeString = IconSizes.generateSizeString(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public UpdateItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        View v = null;
        switch (type){
            case 0:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_installed, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_update, parent, false);
                break;
        }

        AppViewHolder holder = (AppViewHolder) v.getTag();

        if(holder==null){
            holder = new AppViewHolder();
            holder.appIcon = (ImageView) v.findViewById(R.id.app_icon);
            holder.manageIcon = (ImageView) v.findViewById(R.id.manage_icon);
            holder.appName = (TextView) v.findViewById(R.id.app_name);
            holder.versionName = (TextView) v.findViewById(R.id.app_version);
            if(type==1) holder.notsafe = (TextView) v.findViewById(R.id.update_not_safe);

            v.setTag(holder);
        }

        UpdateItem item = getItem(position);

        holder.appName.setText(Html.fromHtml(item.getName()).toString());
        String icon1 = item.getIcon();

        if(icon1.contains("_icon")){
            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
            icon1 = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(icon1,holder.appIcon);

        holder.versionName.setText(item.getVersionName());
        switch (type){
            case 1:
                final long id = getItemId(position);

                holder.manageIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Start) context).installApp(id);
                        Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                    }
                });

                if(!getItem(position).isSignature_valid()){
                    holder.notsafe.setVisibility(View.VISIBLE);
                }else{
                    holder.notsafe.setVisibility(View.GONE);
                }

                break;

            case 0:
                break;
        }

        return v;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isUpdate()?1:0;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }






    public ArrayList<Long> getUpdateIds() {

        ArrayList<Long> ids = new ArrayList<Long>();

        for(UpdateItem item : items){

                if(item.isUpdate()){
                    ids.add(item.getId());
                }

        }

        return ids;
    }



    @Override
    public String getSectionTitleForItem(UpdateItem instance) {

        if(instance.isUpdate()){
            return "0";
        }

        return "1";
    }

    public static class AppViewHolder{
        ImageView appIcon;
        ImageView manageIcon;
        TextView appName;
        TextView versionName;
        TextView notsafe;
        //TextView downloads;
        //TextView rating;
    }
}
