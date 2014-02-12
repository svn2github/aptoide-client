/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.ScreenshotsViewer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

public class ViewPagerAdapterScreenshots extends PagerAdapter {

	private Context context;
	ImageLoader imageLoader;
	ArrayList<String> url;
	private String hashCode;
	private boolean hd;
	DisplayImageOptions options;

	public ViewPagerAdapterScreenshots(Context context, ArrayList<String> imagesurl, String hashCode, boolean hd) {

		this.context=context;
		imageLoader = ImageLoader.getInstance();

		this.url=imagesurl;
		this.hd=hd;


		options = new DisplayImageOptions.Builder()
		 .displayer(new FadeInBitmapDisplayer(1000))
		 .resetViewBeforeLoading()
		 .cacheOnDisc()

		 .build();
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {

		final View v = LayoutInflater.from(context).inflate(R.layout.row_item_screenshots_big, null);
		final ProgressBar pb = (ProgressBar) v.findViewById(R.id.screenshots_loading_big);

        String icon;
        if (hd) {
            Log.d("Aptoide-Screenshots", "Icon is hd: " + url.get(position));

            if (url.get(position).contains("_screen")) {
                icon = url.get(position).split("\\|")[1];
                Log.d("Aptoide-Screenshots" , "Icon is : " + icon);
            } else {
                icon = url.get(position);
            }

        } else {
            icon = screenshotToThumb(url.get(position));
        }


		imageLoader.displayImage(icon, (ImageView) v.findViewById(R.id.screenshot_image_big), options, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String uri, View view) {
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String uri, View v, FailReason failReason) {
                ((ImageView) v.findViewById(R.id.screenshot_image_big)).setImageResource(android.R.drawable.ic_delete);
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String uri, View v, Bitmap loadedImage) {
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String uri, View v) {
            }
        });
		container.addView(v);
		if(!hd){
			v.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Intent i = new Intent(context, ScreenshotsViewer.class);
					i.putStringArrayListExtra("url", url);
					i.putExtra("position", position);
					i.putExtra("hashCode", hashCode+".hd");
					context.startActivity(i);
				}
			});
		}
		return v;

	}
	@Override
	public int getCount() {
		return url.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0.equals(arg1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);

	}

    protected String screenshotToThumb(String string) {

        String[] splitedString = string.split("/");
        StringBuilder db = new StringBuilder();
        for (int i = 0; i != splitedString.length - 1; i++) {
            db.append(splitedString[i]);
            db.append("/");
        }
        db.append("thumbs/mobile/");
        db.append(splitedString[splitedString.length - 1]);

        return db.toString();
    }







}
