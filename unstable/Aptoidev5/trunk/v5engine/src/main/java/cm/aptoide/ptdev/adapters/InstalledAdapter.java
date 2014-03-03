package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 20-11-2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class InstalledAdapter extends CursorAdapter {

    final private String sizeString;

    public InstalledAdapter(Context context) {
        super(context, null, FLAG_REGISTER_CONTENT_OBSERVER);
        sizeString = IconSizes.generateSizeString(context);

    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) {
            return 0;
        }

        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int type = getItemViewType(cursor.getPosition());
        View v = null;
        switch (type) {
            case 0:
                v = LayoutInflater.from(context).inflate(R.layout.separator_textview, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(context).inflate(R.layout.row_app_installed, parent, false);
                break;
        }

        return v;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        int type = getItemViewType(cursor.getPosition());

        final String name = cursor.getString(cursor.getColumnIndex("name"));

        switch (type) {
            case 0:

                TextView tv = (TextView) view.findViewById(R.id.separator_label);
                tv.setText(name);

                break;
            case 1:


                AppViewHolder holder = (AppViewHolder) view.getTag();
                if (holder == null) {
                    holder = new AppViewHolder();
                    holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
//                    holder.actionIcon = (ImageView) view.findViewById(R.id.manage_icon);
                    holder.appName = (TextView) view.findViewById(R.id.app_name);
                    holder.versionName = (TextView) view.findViewById(R.id.app_version);
                    view.setTag(holder);
                }


                holder.appName.setText(Html.fromHtml(name).toString());
                String icon1 = cursor.getString(cursor.getColumnIndex("icon"));
                String iconpath = cursor.getString(cursor.getColumnIndex("iconpath"));
                if (icon1.contains("_icon")) {
                    String[] splittedUrl = icon1.split("\\.(?=[^\\.]+$)");
                    icon1 = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                }
                final String iconPath = iconpath + icon1;
                ImageLoader.getInstance().displayImage(iconPath, holder.appIcon);
                final String versionName = cursor.getString(cursor.getColumnIndex("version_name"));

                holder.versionName.setText(versionName);
                final String packageName = cursor.getString(cursor.getColumnIndex("package_name"));

//                holder.actionIcon.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        UninstallRetainFragment uninstallRetainFragment = new UninstallRetainFragment(name, packageName, versionName, iconPath);
//                        ((MainActivity) context).getSupportFragmentManager().beginTransaction().add(uninstallRetainFragment, name + "UnistallTask").commit();
//                    }
//                });

                break;

        }


    }

    public static class AppViewHolder {
        ImageView appIcon;
//        ImageView actionIcon;
        TextView appName;
        TextView versionName;
        TextView downloads;
        TextView rating;
    }
}
