package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.*;
import cm.aptoide.ptdev.*;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.model.Collection;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import static cm.aptoide.ptdev.utils.AptoideUtils.withSuffix;

/**
 * Created by rmateus on 28-01-2014.
 */
public class HomeLayoutAdapter extends BaseAdapter {

    private final Activity context;
    private final int bucketSize;
    private final String iconSize;
    private boolean mWasEndedAlready;
    private ArrayList<Collection> list;
    private boolean b;

    public HomeLayoutAdapter(Activity context, ArrayList<Collection> list, boolean b) {
        this.context = context;
        this.list = list;
        this.b=b;
        bucketSize = (int) (getScreenWidthInDip() / 120);
        iconSize = IconSizes.generateSizeString(context);

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Collection getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = list.get(position).isExpanded()?0:1;
        Log.d("Aptoide-HomeLayout", "viewType is " + viewType);
        return viewType;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        if(convertView==null){
            v = LayoutInflater.from(context).inflate(R.layout.page_collection, parent, false);
        }else{
            v = convertView;
        }

        final Collection collection = getItem(position);

        if (b) {
            v.findViewById(R.id.more).setVisibility(View.VISIBLE);
            v.findViewById(R.id.separatorLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, MoreEditorsChoiceActitivy.class);
                    Log.d("Aptoide-HomeLayout", String.valueOf(collection.getParentId()));
                    i.putExtra("parentId", collection.getParentId());
                    context.startActivity(i);
                }
            });

            v.findViewById(R.id.separatorLayout).setClickable(true);

        }else{
            if(collection.isHasMore()){
                ImageView iv = (ImageView) v.findViewById(R.id.action);

                if(collection.isExpanded2()){
                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute( R.attr.icCollapseDrawable, outValue, true );
                    iv.setImageResource(outValue.resourceId);
                }else{
                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute( R.attr.icExpandDrawable, outValue, true );
                    iv.setImageResource(outValue.resourceId);
                }

                v.findViewById(R.id.action).setVisibility(View.VISIBLE);
                v.findViewById(R.id.separatorLayout).setOnClickListener(new AnimationClickListener(iv, v, position, collection.getParentId()));
                v.findViewById(R.id.separatorLayout).setClickable(true);
            } else {
                v.findViewById(R.id.action).setVisibility(View.GONE);
                v.findViewById(R.id.separatorLayout).setOnClickListener(null);
                v.findViewById(R.id.separatorLayout).setClickable(false);
            }
        }
        TextView tv = (TextView) v.findViewById(R.id.collectionName);

        String name = list.get(position).getName();
        String categoryName;

        if (list.get(position).getWeeks() > -1) {
            categoryName = list.get(position).getWeeks() + " weeks ago";

        } else {

            try {
                categoryName = context.getString(EnumCategories.getCategoryName(list.get(position).getParentId()));
                Log.d("HomeLayoutAdapter-categ", "Category Name: " + categoryName);
            } catch (Exception e) {
                categoryName = name;
                Log.d("HomeLayoutAdapter-categ", "Untranslated Category Name: " + categoryName);
            }
        }
        tv.setText(categoryName);
//        tv.setText(Html.fromHtml(list.get(position).getName()).toString());




        //gl.setAlignmentMode();

        Log.d("Aptoide-GridView", "Orientation is " + context.getResources().getConfiguration().orientation);




        LinearLayout containerLinearLayout = (LinearLayout) v.findViewById(R.id.collectionList);
        containerLinearLayout.removeAllViews();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerLinearLayout.getLayoutParams();
        params.bottomMargin = collection.getMarginBottom();

        containerLinearLayout.setLayoutParams(params);

        containerLinearLayout.removeAllViews();


        LinearLayout rowLinearLayout = new LinearLayout(context);
        rowLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        int i = bucketSize;
        for (final HomeItem item : list.get(position).getAppsList()) {


            if(i % bucketSize == 0){
                rowLinearLayout = new LinearLayout(context);
                rowLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                containerLinearLayout.addView(rowLinearLayout);
            }

            i++;
            Log.d("Aptoide-HomeLayout", "Adding item " + item.getName() + " " + i);

            FrameLayout tvChild;
            if(b){
                tvChild = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.row_app_home, rowLinearLayout, false);
            }else{
                tvChild = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.row_app_home_more, rowLinearLayout, false);
            }
            TextView nameTv = (TextView) tvChild.findViewById(R.id.app_name);
            nameTv.setText(item.getName());
            ImageView iconIv = (ImageView) tvChild.findViewById(R.id.app_icon);
            Log.d("Home-Name", "Name length: " + item.getName().length());

            String icon = item.getIcon();
            if(icon.contains("_icon")){
                String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                icon = splittedUrl[0] + "_" + iconSize + "."+ splittedUrl[1];
            }
            ImageLoader.getInstance().displayImage(icon, iconIv);

            tvChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, AppViewActivity.class);
                    long id = item.getId();
                    i.putExtra("id", id);
                    context.startActivity(i);
                }
            });

            String category;
            try {
                int cat = Integer.parseInt(item.getCategory());
                category = context.getString(EnumCategories.getCategoryName(cat));
                Log.d("Home-categ", "Category Name: " + category);
            }catch (Exception e){
                category = item.getCategory();
                Log.d("Home-categ", "Untranslated Category Name: " + category);
            }

            if(list.get(position).getParentId()!=-1){
                TextView downloadsTv = (TextView) tvChild.findViewById(R.id.app_downloads);
                downloadsTv.setText(context.getString(R.string.X_download_number, withSuffix(item.getDownloads())));
                if(item.getName().length()>10){
                    downloadsTv.setMaxLines(1);
                }else{
                    downloadsTv.setMaxLines(2);
                }
                downloadsTv.setVisibility(View.VISIBLE);
            }else{
                TextView categoryTv = (TextView) tvChild.findViewById(R.id.app_category);
                categoryTv.setText(category);
                if(item.getName().length()>10){
                    categoryTv.setMaxLines(1);
                }else{
                    categoryTv.setMaxLines(2);
                }
                categoryTv.setVisibility(View.VISIBLE);
            }

            ImageView overflow = (ImageView) tvChild.findViewById(R.id.ic_action);;
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(v, item.getId());
                }
            });
            RatingBar rating = (RatingBar) tvChild.findViewById(R.id.app_rating);
            Log.d("Aptoide-Rating", String.valueOf(item.getRating()));
            rating.setRating(item.getRating());
            rating.setOnRatingBarChangeListener(null);
            rating.setVisibility(View.VISIBLE);




            rowLinearLayout.addView(tvChild);


        }

        return v;
    }

    protected float getScreenWidthInDip() {
        WindowManager wm = context.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth_in_pixel = dm.widthPixels;
        float screenWidth_in_dip = screenWidth_in_pixel / dm.density;
        density = dm.density;
        return screenWidth_in_dip;
    }
    private float density;

    private class AnimationClickListener implements View.OnClickListener {

        private final int position;
        private final int parentId;
        private final View view;
        private final ImageView iv;

        public AnimationClickListener(ImageView iv, View v, int position, int parentid) {

            this.position = position;
            this.parentId = parentid;
            this.view = v;
            this.iv = iv;

        }

        @Override
        public void onClick(View v) {

            final Collection collection = (Collection) getItem(position);

            int earlierRows;
            if (!collection.isExpanded()) {
                collection.getAppsList().clear();
                collection.getAppsList().addAll(new Database(Aptoide.getDb()).getCollectionFeatured(parentId,10));
            }

            if (!collection.isExpanded()) {
                int sizeToGrow = collection.getAppsList().size() / bucketSize;

                if (collection.getAppsList().size() % bucketSize != 0) {
                    sizeToGrow++;
                }

                Log.d("Aptoide-HomeLayout", "Size to grow is " + sizeToGrow + " " + collection.getAppsList().size());
                int maxMargin = (int) ((190) * density * sizeToGrow);

                Log.d("Aptoide-HomeLayout", "MaxMargin  is " + maxMargin);

                int mMarginStart = (int) (-maxMargin + 190 * density);

                Log.d("Aptoide-HomeLayout", "MarginStart  is " + mMarginStart);

                collection.setMarginBottom(mMarginStart);
                notifyDataSetChanged();

            }

            final LinearLayout toolbar = (LinearLayout) view.findViewById(R.id.collectionList);

            Animation anim = new ExpandAnimation(iv, collection, toolbar, 150, collection.isExpanded());
            toolbar.startAnimation(anim);
        }

        public class ExpandAnimation extends Animation {


            private View mAnimatedView;
            private LinearLayout.LayoutParams mViewLayoutParams;
            private int mMarginStart, mMarginEnd;
            private boolean mIsVisibleAfter = false;
            private Collection collection;

            /**
             * Initialize the animation
             * @param iv
             * @param view The layout we want to animate
             * @param duration The duration of the animation, in ms
             */
            public ExpandAnimation(ImageView iv, Collection collection, View view, int duration, boolean expanded) {
                this.collection = collection;

                setDuration(duration);
                mAnimatedView = view;
                mViewLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();

                // decide to show or hide the view



                int sizeToGrow = collection.getAppsList().size() / bucketSize;
                if(collection.getAppsList().size() % bucketSize != 0){
                    sizeToGrow++;
                }

                int maxMargin = (int) ((190)*density * sizeToGrow);

                if( !expanded ){
                    mMarginStart = (int) ((int)  -maxMargin + 190*density);
                    mMarginEnd = 0;

                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute( R.attr.icCollapseDrawable, outValue, true );
                    iv.setImageResource(outValue.resourceId);
                    collection.setExpanded2(true);
                }else{
                    mMarginEnd = (int) ((190)*density) - maxMargin;
                    mMarginStart = 0;
                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute( R.attr.icExpandDrawable, outValue, true );
                    iv.setImageResource(outValue.resourceId);
                    collection.setExpanded2(false);
                }

                mWasEndedAlready = false;
                view.setVisibility(View.VISIBLE);


            }

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);


                if (interpolatedTime < 1.0f) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAnimatedView.getLayoutParams();

                    // Calculating the new bottom margin, and setting it
                    params.bottomMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

                    // Invalidating the layout, making us seeing the changes we made
                    Log.d("Aptoide-HomeLayout", "Applying transform: bottom margin is " + params.bottomMargin + " " + mMarginStart + " " + mMarginEnd);
                    mAnimatedView.requestLayout();

                    // Making sure we didn't run the ending before (it happens!)
                } else if (!mWasEndedAlready) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAnimatedView.getLayoutParams();

                    params.bottomMargin = mMarginEnd;
                    mAnimatedView.requestLayout();
                    collection.setMarginBottom(0);
                    if(collection.isExpanded()){
                        collection.getAppsList().clear();
                        collection.getAppsList().addAll(new Database(Aptoide.getDb()).getCollectionFeatured(parentId,bucketSize));
                        notifyDataSetChanged();
                    }
                    collection.setExpanded(!collection.isExpanded());
                    mWasEndedAlready = true;
                }
            }


        }
    }


    public int getBucketSize() {
        return bucketSize;
    }


    public void showPopup(View v, long id) {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(context, v);
        popup.setOnMenuItemClickListener(new MenuListener(context, id));
        popup.inflate(R.menu.menu_actions);
        popup.show();
    }

    static class MenuListener implements android.support.v7.widget.PopupMenu.OnMenuItemClickListener{

        Context context;
        long id;

        MenuListener(Context context, long id) {
            this.context = context;
            this.id = id;


        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int i = menuItem.getItemId();

            if (i == R.id.menu_install) {
                ((MainActivity)context).installApp(id);
                Toast.makeText(context, context.getString(R.string.starting_download), Toast.LENGTH_LONG).show();
                return true;
            } else if (i == R.id.menu_schedule) {
                return true;
            } else {
                return false;
            }
        }
    }
}
