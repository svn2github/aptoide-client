package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceArrayAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson.Friend;

/**
 * Created by asantos   on 09-10-2014.
 */
public class InviteFriendsListAdapter extends ArrayAdapter<Friend> {
    private Context ctx;

    public InviteFriendsListAdapter(Context context, ArrayList<Friend> items) {
        super(context, 0, items);
        this.ctx = context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final View v;
	    ViewHolder holder;

	    if(convertView == null){
		    convertView = LayoutInflater.from(ctx).inflate(R.layout.row_facebook_invite_friends, null);
			holder = new ViewHolder();
		    holder.name = (CheckedTextView) convertView.findViewById(R.id.username);
			holder.avatarImage = (ImageView) convertView.findViewById(R.id.user_avatar);

		    convertView.setTag(holder);
	    }else{
		    holder = (ViewHolder)convertView.getTag();
	    }
        v = convertView;

        Friend friend = getItem(position);

        holder.name.setChecked(((ListView)parent).isItemChecked(position));
	    holder.name.setText(friend.getUsername());
        ImageLoader.getInstance().displayImage(friend.getAvatar(), holder.avatarImage);
        return v;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{
        public CheckedTextView name;
        public ImageView avatarImage;
    }
}
