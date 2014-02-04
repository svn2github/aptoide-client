package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.os.Bundle;

import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreItem;
import cm.aptoide.ptdev.fragments.Callback;

import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceArrayAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

public class StoreAdapter extends MultiChoiceArrayAdapter<StoreItem> {

	
	private LayoutInflater inflater;
    private Callback callback;

	public StoreAdapter(Bundle savedInstanceState, Context context, ArrayList<StoreItem> items, Callback callback) {
		super(savedInstanceState, context, 0, items);
        this.callback = callback;
        inflater = LayoutInflater.from(context);


	}



	@Override
	public long getItemId(int position) {

        Log.d("Aptoide-", "Getting ItemId " + getItem(position).getId());

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

        String downloadNnr="";
        if(storeItem.getDwnNumber()!=null && storeItem.getDwnNumber().length()>0){
            downloadNnr = withSuffix(storeItem.getDwnNumber());
            storeDwn.setText(downloadNnr+" "+getContext().getString(R.string.downloads));
        }else{
            storeDwn.setText("");
        }

        if(getItemId(position)>0){
            ImageLoader.getInstance().displayImage(storeItem.getStoreAvatar(),avatarImage);
        }else{
            avatarImage.setImageResource(R.drawable.avatar_apps);
        }


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
        return callback.onActionItemClicked(mode,item);
    }

    boolean allSelected;

    public void selectAll() {

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
