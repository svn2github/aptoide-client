package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.SearchManager;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.webservices.json.SearchJson;

/**
 * Created by asantos on 18-07-2014.
 */
public class SearchAdapterU extends ArrayAdapter<SearchJson.Results.Apks> {

    final private String sizeString;
    private final LayoutInflater mInflater;
    private Context mContext;

    public SearchAdapterU(Context context, List<SearchJson.Results.Apks> objects) {
        super(context, 0, objects);
        sizeString = IconSizes.generateSizeString(context);
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.row_app_search_result_other, parent, false);
        } else {
            view = convertView;
        }

        UAppViewHolder holder = (UAppViewHolder) view.getTag();

        SearchJson.Results.Apks item = getItem(position);

        String name = item.getName();
        int count = getCount();
        if (holder == null) {
            holder = new UAppViewHolder();
            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
            holder.appNameplusversion = (TextView) view.findViewById(R.id.app_name);
            view.setTag(holder);
        }

        holder.appNameplusversion.setText(Html.fromHtml(name).toString() + " - "+ item.getVername());
        String icon1 = item.getIconhd();

        if (icon1 == null) {
            icon1 = item.getIcon();
        }

        if (icon1.contains("_icon")) {
            String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
            icon1 = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
        }
        ImageLoader.getInstance().displayImage(icon1, holder.appIcon);

        return view;
    }
    public static class UAppViewHolder {
        ImageView appIcon;
        TextView appNameplusversion;
    }
/*
    public void showPopup(View v, long id) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.setOnMenuItemClickListener(new MenuListener(mContext, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class MenuListener implements PopupMenu.OnMenuItemClickListener {

        Context context;
        long id;

        MenuListener(Context context, long id) {
            this.context = context;
            this.id = id;


        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int i = menuItem.getItemId();

            if (i == R.id.menu_install) {
                ((SearchManager) context).installApp(id);
                Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }*/
}