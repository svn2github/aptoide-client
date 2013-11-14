package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.manuelpeinado.multichoiceadapter.MultiChoiceArrayAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class StoreAdapter extends MultiChoiceArrayAdapter<StoreItem>{

	
	private LayoutInflater inflater;

	public StoreAdapter(Bundle savedInstanceState, Context context, ArrayList<StoreItem> items) {
		super(savedInstanceState, context, 0, items);
		inflater = LayoutInflater.from(context);
	}

//    public StoreAdapter(Bundle savedInstanceState, Context context, ArrayList<StoreItem> items) {
//        super(savedInstanceState, context, R.layout.row_stores, android.R.id.text1, items);
//    }

	@Override
	public long getItemId(int position) {

		return getItem(position).getId();
	}

    @Override
    public View getViewImpl(int position, View convertView, ViewGroup parent) {
        StoreItem storeItem = this.getItem(position);
        TextView storeName;
        TextView storeDwn;
        ImageView avatarImage;
        View alphaView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_stores, null);
            storeName = (TextView) convertView.findViewById(R.id.store_name);
            storeDwn = (TextView) convertView.findViewById(R.id.store_dwn_number);
            avatarImage = (ImageView) convertView.findViewById(R.id.store_avatar);
            alphaView = convertView.findViewById(R.id.view_alpha);
            convertView.setTag(new StoreViewHolder(storeName, avatarImage, storeDwn, alphaView));
        } else {
            StoreViewHolder viewHolder = (StoreViewHolder) convertView.getTag();
            storeName = viewHolder.getStoreName();
            storeDwn = viewHolder.getStoreDwn();
            avatarImage = viewHolder.getAvatarImage();
            alphaView = viewHolder.getAlphaView();
        }

        storeName.setText(storeItem.getName());
        storeDwn.setText(storeItem.getDwnNumber());

        ImageLoader.getInstance().displayImage(storeItem.getStoreAvatar(),avatarImage);

        alphaView.setBackgroundResource(storeItem.getTheme().getStoreAlphaColor());
        Log.d("Aptoide-", "BindView " + storeItem.getName());
        return convertView;

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_reload) {
            Toast.makeText(getContext(), "Reloading...", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_discard) {
            Toast.makeText(getContext(), "Removing...", Toast.LENGTH_SHORT).show();
            return true;
        } else if( id == R.id.menu_select_all){
            selectAll();
        }

        return false;
    }

    boolean allSelected;

    private void selectAll() {

        allSelected = !allSelected;
        for (int i = 0; i < this.getCount(); ++i) {
            setItemChecked(i, allSelected);
        }
    }

    public static class StoreViewHolder{

        private TextView storeName, storeDwn;

        private ImageView avatarImage;
        private View alphaView;

        public StoreViewHolder(TextView name, ImageView avatar, TextView storeDwn, View alphaView) {
			this.storeName = name;
            this.storeDwn = storeDwn;
			this.avatarImage = avatar;
            this.alphaView = alphaView;
		}
		


		public TextView getStoreName() {
			return storeName;
		}
		
		public ImageView getAvatarImage() {
			return avatarImage;
		}

        public TextView getStoreDwn() {
            return storeDwn;
        }

        public View getAlphaView() {
            return alphaView;
        }
    }
	
}
