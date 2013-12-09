package cm.aptoide.ptdev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    String[] mTitle;
    String[] mSubTitle;
    int[] mIcon;
    LayoutInflater inflater;

    public MenuListAdapter(Context context) {
        this.context = context;
        mTitle = new String[] { context.getString(R.string.my_account), context.getString(R.string.rollback), context.getString(R.string.scheduled_downloads), context.getString(R.string.facebook), context.getString(R.string.twitter)};
        mSubTitle = new String[] { context.getString(R.string.logout), context.getString(R.string.rollback_sum), context.getString(R.string.setting_schdwninstallsum) , context.getString(R.string.find_us_on_facebook), context.getString(R.string.follow_twitter)};
        mIcon = new int[] { android.R.drawable.ic_menu_edit, R.drawable.ic_action_time, R.drawable.ic_drawer_schedule, R.drawable.ic_action_facebook, R.drawable.ic_action_twitter};
    }

    @Override
    public int getCount() {
        return mTitle.length;
    }

    @Override
    public Object getItem(int position) {
        return mTitle[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // Declare Variables
        TextView txtTitle;
        TextView txtSubTitle;
        ImageView imgIcon;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.row_drawer_list, parent,false);

        // Locate the TextViews in drawer_list_item.xml
        txtTitle = (TextView) itemView.findViewById(R.id.title);
        txtSubTitle = (TextView) itemView.findViewById(R.id.subtitle);

        // Locate the ImageView in drawer_list_item.xml
        imgIcon = (ImageView) itemView.findViewById(R.id.icon);

        // Set the results into TextViews
        txtTitle.setText(mTitle[position]);
        txtSubTitle.setText(mSubTitle[position]);

        // Set the results into ImageView
        imgIcon.setImageResource(mIcon[position]);

        return itemView;
    }

}