package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 09-10-2014.
 */
public class FriendsListAdapter extends BaseAdapter {

	private final List<ListUserFriendsJson.Friend> list;
	protected Context ctx;

    public FriendsListAdapter(Context ctx, ListUserFriendsJson list) {
        this.list = list.getFriends();
        this.ctx = ctx;
    }


    @Override
    public int getCount() {
	    if (list == null) return 0;
        return list.size();
    }

    @Override
    public ListUserFriendsJson.Friend getItem(int position) { return list.get(position); }

	@Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        public ImageView avatar;
        public TextView personName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

	    ViewHolder holder;

	    if(convertView == null){
		    convertView = LayoutInflater.from(ctx).inflate(R.layout.row_facebook_friends_on_timeline, null);
			holder = new ViewHolder();
		    holder.personName = (TextView) convertView.findViewById(R.id.user_name);
			holder.avatar = (ImageView) convertView.findViewById(R.id.user_avatar);

		    convertView.setTag(holder);
	    }else{
		    holder = (ViewHolder)convertView.getTag();
	    }


        ListUserFriendsJson.Friend friend = getItem(position);

	    holder.personName.setText(friend.getUsername());
        ImageLoader.getInstance().displayImage(friend.getAvatar(), holder.avatar);

        return convertView;
    }
}
