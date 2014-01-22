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

        int[] attrs = new int[] {
                R.attr.icMyAccountDrawable /* index 0 */,
                R.attr.icRollbackDrawable /* index 1 */,
                R.attr.icScheduledDrawable /* index 2 */,
                R.attr.icExcludedUpdatesDrawable /* index 3 */
        };

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs);

        int myAccountRes = typedArray.getResourceId(0, R.drawable.ic_action_accounts_dark);
        mItems.add(new Item(context.getString(R.string.my_account), myAccountRes, 0));

        int rollbackRes = typedArray.getResourceId(1, R.drawable.ic_action_time_dark);
        mItems.add(new Item(context.getString(R.string.rollback), rollbackRes, 1));

        TypedArray scheduleTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[]{R.attr.icScheduledDrawable});
        int scheduleRes = scheduleTypedArray.getResourceId(0, 0);
        scheduleTypedArray.recycle();
        mItems.add(new Item(context.getString(R.string.setting_schdwntitle), scheduleRes, 2));

        TypedArray excludedUpdatesTypedArray = context.getTheme().obtainStyledAttributes(Aptoide.getThemePicker().getAptoideTheme(context), new int[]{R.attr.icExcludedUpdatesDrawable});
        int excludedUpdatesRes = excludedUpdatesTypedArray.getResourceId(0, 0);
        excludedUpdatesTypedArray.recycle();
        mItems.add(new Item(context.getString(R.string.excluded_updates), excludedUpdatesRes, 3));

        mItems.add(new Category(context.getString(R.string.social_networks)));
        mItems.add(new Item(context.getString(R.string.facebook), R.drawable.ic_action_facebook, 4));
        mItems.add(new Item(context.getString(R.string.twitter), R.drawable.ic_action_twitter, 5));

        typedArray.recycle();
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



    private static final class Item implements Id {

        String mTitle;
        int mIconRes;
        long id;

        Item(String title, int iconRes,long  id) {
            mTitle = title;
            mIconRes = iconRes;
            this.id = id;
        }

        @Override
        public long getId() {
            return id;
        }
    }

    private static final class Category implements Id {

        String mTitle;

        Category(String title) {
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