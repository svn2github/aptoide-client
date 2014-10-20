package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.views.BadgeView;

import java.util.ArrayList;
import java.util.List;

public class MenuListAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
//    String[] mTitle;
//    String[] mSubTitle;
//    int[] mIcon;
    List<Object> mItems = new ArrayList<Object>();

    public MenuListAdapter(Context context, List<Object> mItems) {
        this.context = context;
        this.mItems = mItems;

    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((Id)mItems.get(position)).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof Item ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position) instanceof Item;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Object item = getItem(position);

        if (item instanceof Category) {
            if (v == null) {
                v = LayoutInflater.from(context).inflate(R.layout.menu_row_category, parent, false);
            }

            ((TextView) v).setText(((Category) item).mTitle);

        } else {
            if (v == null) {
                v = LayoutInflater.from(context).inflate(R.layout.menu_row_item, parent, false);
            }

            TextView tv = (TextView) v.findViewById(R.id.menu_item);

            tv.setText(((Item) item).mTitle);
            tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);

            BadgeView badgeView = new BadgeView(context, tv);
            badgeView.setText(context.getString(R.string.new_string));
            badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            badgeView.setBadgeBackgroundColor(Color.parseColor("#A4C639"));
            if(((Item) item).isNew) {
                badgeView.show(false);
            }else{
                badgeView.hide(false);
            }

        }

//        v.setTag(R.id.mdActiveViewPosition, position);
//
//        if (position == mActivePosition) {
//            mMenuDrawer.setActiveView(v, position);
//        }

        return v;
    }



    public static class Item implements Id {

        String mTitle;
        int mIconRes;
        long id;


        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }

        public boolean isNew() {
            return isNew;
        }

        boolean isNew;

        public Item(String title, int iconRes, long id) {
            mTitle = title;
            mIconRes = iconRes;
            this.id = id;

        }


        @Override
        public long getId() {
            return id;
        }
    }

    public static class Category implements Id {

        String mTitle;

        public Category(String title) {
            mTitle = title;
        }

        @Override
        public long getId() {
            return 0;
        }
    }

    public interface Id{
        public long getId();
    }

}