package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;

import java.util.ArrayList;
import java.util.List;

public class MenuListAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
//    String[] mTitle;
//    String[] mSubTitle;
//    int[] mIcon;
    List<Object> mItems = new ArrayList<Object>();

    public MenuListAdapter(Context context) {
        this.context = context;

        TypedArray myAccountTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[] {R.attr.icMyAccountDrawable});
        int myAccountRes = myAccountTypedArray.getResourceId(0, 0);
        myAccountTypedArray.recycle();
        mItems.add(new Item(context.getString(R.string.my_account), myAccountRes));

        TypedArray rollbackTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[] {R.attr.icRollbackDrawable});
        int rollbackRes = rollbackTypedArray.getResourceId(0, 0);
        rollbackTypedArray.recycle();
        mItems.add(new Item(context.getString(R.string.rollback), rollbackRes));

//        TypedArray scheduleTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[] {R.attr.icScheduledDrawable});
//        int scheduleRes = scheduleTypedArray.getResourceId(0, 0);
//        scheduleTypedArray.recycle();
//        mItems.add(new Item(context.getString(R.string.scheduled_downloads), scheduleRes));

//        TypedArray excludedUpdatesTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[] {R.attr.icExcludedUpdatesDrawable});
//        int excludedUpdatesRes = excludedUpdatesTypedArray.getResourceId(0, 0);
//        excludedUpdatesTypedArray.recycle();
//        mItems.add(new Item(context.getString(R.string.excluded_updates), excludedUpdatesRes));

        mItems.add(new Category(context.getString(R.string.social_networks)));
        mItems.add(new Item(context.getString(R.string.facebook), R.drawable.ic_action_facebook));
        mItems.add(new Item(context.getString(R.string.twitter), R.drawable.ic_action_twitter));
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
        return position;
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
                v = LayoutInflater.from(context).inflate(R.layout.menu_row_category,
                        parent, false);
            }

            ((TextView) v).setText(((Category) item).mTitle);

        } else {
            if (v == null) {
                v = LayoutInflater.from(context).inflate(R.layout.menu_row_item,
                        parent, false);
            }

            TextView tv = (TextView) v;
            tv.setText(((Item) item).mTitle);
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    ((Item) item).mIconRes, 0, 0, 0);
        }

//        v.setTag(R.id.mdActiveViewPosition, position);
//
//        if (position == mActivePosition) {
//            mMenuDrawer.setActiveView(v, position);
//        }

        return v;
    }



    private static final class Item {

        String mTitle;
        int mIconRes;

        Item(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    private static final class Category {

        String mTitle;

        Category(String title) {
            mTitle = title;
        }
    }

}