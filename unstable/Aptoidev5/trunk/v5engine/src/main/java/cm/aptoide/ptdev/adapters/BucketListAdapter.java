package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.os.Debug;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A bucket adapter presenting rows of buckets.
 * 
 * @author Scythe
 * 
 * @param <T>
 */
public abstract class BucketListAdapter<T> extends ArrayAdapter<T> {

    private static final String TAG = "BucketListAdapter";
    private final boolean DEBUG = true;

    protected Activity ctx;
    protected Integer bucketSize;

    /**
     * Basic constructor, takes an Activity context and the list of elements.
     * Assumes a 1 column view by default.
     * 
     * @param ctx
     *            The Activity context.
     * @param elements
     *            The list of elements to present.
     */
    public BucketListAdapter(Activity ctx, List<T> elements) {
        this(ctx, elements, 1);
    }

    /**
     * Extended constructor, takes an Activity context, the list of elements and
     * the exact number of columns.
     * 
     * @param ctx
     *            The Activity context.
     * @param elements
     *            The list of elements to present.
     * @param bucketSize
     *            The exact number of columns.
     * 
     */
    public BucketListAdapter(Activity ctx, List<T> elements, Integer bucketSize) {
        super(ctx, 0, elements);

        //this.elements = elements;
        this.ctx = ctx;
        this.bucketSize = bucketSize;
    }

    /**
     * Calculates the required number of columns based on the actual screen
     * width (in DIP) and the given minimum element width (in DIP).
     * 
     * @param minBucketElementWidthDip
     *            The minimum width in DIP of an element.
     */
    public void enableAutoMeasure(float minBucketElementWidthDip) {
        float screenWidth = getScreenWidthInDip();

        if (minBucketElementWidthDip >= screenWidth) {
            bucketSize = 1;
        } else {
            bucketSize = (int) (screenWidth / minBucketElementWidthDip);;
        }
    }
    
    public int getBucketSize() {
        return bucketSize;
    }

    @Override
    public int getCount() {
        return (super.getCount() + bucketSize - 1) / bucketSize;
        //return (elements.size() + bucketSize - 1) / bucketSize;
    }

    @Override
    public T getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getCount() - 1)
            return 0;
        return 1;
    }

    @Override
    public View getView(final int bucketPosition, View convertView, ViewGroup parent)
    {



        final LinearLayout bucket;
        if (convertView != null) {
            bucket = (LinearLayout)convertView;
            
            if (DEBUG) {
                Log.i(TAG, "Reusing bucket view of type " + getItemViewType(bucketPosition));
            }
        } else {
            bucket = new LinearLayout(ctx);
            bucket.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.MATCH_PARENT));
            bucket.setOrientation(LinearLayout.HORIZONTAL);
            
            if (DEBUG) {
                Log.i(TAG, "Instantiating new bucket view");
            }
        }
        
        int j = 0;
        final int childCount = bucket.getChildCount();
        for (int i = (bucketPosition * bucketSize); i < ((bucketPosition * bucketSize) + bucketSize); i++) {
            FrameLayout bucketElementFrame;
            if (j < childCount) {
                bucketElementFrame = (FrameLayout)bucket.getChildAt(j);
                
                if (DEBUG) {
                    Log.i(TAG, "Reusing bucketElementFrame view with " + childCount + " childs");
                }
                
                if (i < super.getCount()) {
                    bindBucketElement(i, getItem(i), bucketElementFrame.getChildAt(0), bucketElementFrame);
                    if (DEBUG) {
                        Log.i(TAG, "Reusing element view");
                    }
                }
            } else {
                bucketElementFrame = new FrameLayout(ctx);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);

                bucketElementFrame.setLayoutParams(layoutParams);

                if (i < super.getCount()) {
                    View view = bindBucketElement(i, getItem(i), null, bucketElementFrame);
                    bucketElementFrame.addView(view);
                }
                
                bucket.addView(bucketElementFrame);
            }
            j++;
        }

        return bucket;
    }

    /**
     * Extending classes should return a bucket-element with this method. Each
     * row in the list contains bucketSize total elements.
     * 
     * @param position
     *            The absolute, global position of the current item.
     * @param currentElement
     *            The current element for which the View should be constructed
     * @param convertView
     *            The old view to reuse, if possible.
     * @return The View that should be presented in the corresponding bucket.
     */
    protected abstract View bindBucketElement(final int position,
                                              T currentElement, View convertView, ViewGroup parent);

    protected float getScreenWidthInDip() {
        WindowManager wm = ctx.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth_in_pixel = dm.widthPixels;
        float screenWidth_in_dip = screenWidth_in_pixel / dm.density;

        return screenWidth_in_dip;
    }
}
