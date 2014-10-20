package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceArrayAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson.Friend;

/**
 * Created by asantos   on 09-10-2014.
 */
public class InviteFriendsListAdapter extends MultiChoiceArrayAdapter<Friend> {
    private Context ctx;

    public InviteFriendsListAdapter(Bundle savedInstanceState, Context context, ArrayList<Friend> items) {
        super(savedInstanceState, context, 0, items);
        this.ctx = context;
    }

    boolean allSelected;

    public void selectAll() {
        allSelected = !allSelected;
        for (int i = 0; i < this.getCount(); ++i) {
            setItemChecked(i, allSelected);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public View getViewImpl(final int position, View convertView, ViewGroup parent){
        final View v;
	    ViewHolder holder;

	    if(convertView == null){
		    convertView = LayoutInflater.from(ctx).inflate(R.layout.row_facebook_invite_friends, null);
			holder = new ViewHolder();
		    holder.name = (TextView) convertView.findViewById(R.id.username);
			holder.avatarImage = (ImageView) convertView.findViewById(R.id.user_avatar);

		    convertView.setTag(holder);
	    }else{
		    holder = (ViewHolder)convertView.getTag();
	    }
        v = convertView;

        ListUserFriendsJson.Friend friend = getItem(position);

	    holder.name.setText(friend.getUsername());
        ImageLoader.getInstance().displayImage(friend.getAvatar(), holder.avatarImage);
        return v;
    }

    public static class ViewHolder{
        public TextView name;
        public ImageView avatarImage;
    }
}
