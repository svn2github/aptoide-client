package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.*;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreEditorsChoice;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.model.Collection;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 28-01-2014.
 */
public class HomeLayoutAdapter extends BaseAdapter {

    private final Context context;
    private boolean mWasEndedAlready;
    private ArrayList<Collection> list;
    private boolean b;

    public HomeLayoutAdapter(Context context, ArrayList<Collection> list, boolean b) {
        this.context = context;
        this.list = list;
        this.b=b;
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
            v = LayoutInflater.from(context).inflate(R.layout.collection, parent, false);
        }else{
            v = convertView;
        }

        final Collection collection = getItem(position);

        if (b) {
            v.findViewById(R.id.more).setVisibility(View.VISIBLE);
            v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, MoreEditorsChoice.class);
                    Log.d("Aptoide-HomeLayout", String.valueOf(collection.getParentId()));
                    i.putExtra("parentId", collection.getParentId());
                    context.startActivity(i);
                }
            });
        }else{
            if(collection.isHasMore()){
                v.findViewById(R.id.more).setVisibility(View.VISIBLE);
                v.findViewById(R.id.more).setOnClickListener(new AnimationClickListener(v, position, collection.getParentId()));
            }else{
                v.findViewById(R.id.more).setVisibility(View.GONE);
            }
        }
        TextView tv = (TextView) v.findViewById(R.id.collectionName);
        tv.setText(list.get(position).getName());
        GridLayout gl = (GridLayout) v.findViewById(R.id.collectionList);



        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gl.getLayoutParams();
        layoutParams.bottomMargin = collection.getMarginBottom();

        Log.d("Aptoide-GridView", "Margin is " + layoutParams.bottomMargin);
        gl.setLayoutParams(layoutParams);
        gl.setColumnCount(3);

        Log.d("Aptoide-GridView", "Orientation is " + context.getResources().getConfiguration().orientation);



        gl.removeAllViews();



        for (HomeItem item : list.get(position).getAppsList()) {
            Log.d("Aptoide-HomeLayout", "Adding item" + item.getName());
            FrameLayout tvChild = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.row_app_home, gl, false);
            ((TextView) tvChild.findViewById(R.id.app_name)).setText(item.getName());
            ImageView icon = (ImageView) tvChild.findViewById(R.id.app_icon);
            ImageLoader.getInstance().displayImage(item.getIcon(), icon);

            gl.addView(tvChild);
        }


        return v;
    }

    private class AnimationClickListener implements View.OnClickListener {
        private final int position;
        private final int parentId;
        private final View view;

        public AnimationClickListener(View v, int position, int parentid) {

            this.position = position;
            this.parentId = parentid;
            this.view = v;
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
                int sizeToGrow = collection.getAppsList().size() / 3;

                if (collection.getAppsList().size() % 3 != 0) {
                    sizeToGrow++;
                }

                Log.d("Aptoide-HomeLayout", "Size to grow is " + sizeToGrow + " " + collection.getAppsList().size());
                int maxMargin = (int) ((190) * 1.5 * sizeToGrow);

                Log.d("Aptoide-HomeLayout", "MaxMargin  is " + maxMargin);

                int mMarginStart = (int) (-maxMargin + 190 * 1.5);

                Log.d("Aptoide-HomeLayout", "MarginStart  is " + mMarginStart);

                collection.setMarginBottom(mMarginStart);

                notifyDataSetChanged();

            }

            final GridLayout toolbar = (GridLayout) view.findViewById(R.id.collectionList);

            Animation anim = new ExpandAnimation(collection, toolbar, 500, collection.isExpanded());
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
             * @param view The layout we want to animate
             * @param duration The duration of the animation, in ms
             */
            public ExpandAnimation(Collection collection, View view, int duration, boolean expanded) {
                this.collection = collection;

                setDuration(duration);
                mAnimatedView = view;
                mViewLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();

                // decide to show or hide the view



                int sizeToGrow = collection.getAppsList().size() / 3;
                if(collection.getAppsList().size() % 3 != 0){
                    sizeToGrow++;
                }

                int maxMargin = (int) ((190)*1.5 * sizeToGrow);

                if( !expanded ){
                    mMarginStart = (int) ((int)  -maxMargin + 190*1.5);
                    mMarginEnd = 0;
                }else{
                    mMarginEnd = (int) ((190)*1.5) - maxMargin;
                    mMarginStart = 0;
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
                        collection.getAppsList().addAll(new Database(Aptoide.getDb()).getCollectionFeatured(parentId,3));
                        notifyDataSetChanged();
                    }
                    collection.setExpanded(!collection.isExpanded());
                    mWasEndedAlready = true;
                }
            }
        }
    }




}
