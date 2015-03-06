package cm.aptoide.ptdev.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import cm.aptoide.ptdev.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;


import java.util.List;

public class GalleryPagerAdapter extends PagerAdapter {

    private Activity activity;
    private List<String> imagesUrl;
    ImageLoader imageLoader;
    DisplayImageOptions options;

    public GalleryPagerAdapter(Activity activity, List<String> imagesUrl) {

        this.activity = activity;
        this.imagesUrl = imagesUrl;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
                .cacheOnDisc()
                .build();
    }

    @Override
    public int getCount() {
        return imagesUrl.size();
    }

    @Override
    public Object instantiateItem(View collection, int position) {

        ImageView imageView = new ImageView(activity);

//        imageView.setBackgroundResource(drawableIDs[position]);
        imageLoader.displayImage(imagesUrl.get(position), imageView);


        ((ViewPager) collection).addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((ImageView) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }


    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }


}