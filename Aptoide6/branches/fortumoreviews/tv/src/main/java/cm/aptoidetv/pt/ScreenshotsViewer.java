/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoidetv.pt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ScreenshotsViewer extends Activity {

    public static final String SCREEN = "image";
    private String[] images = new String[0];
    Context context;
    private int currentItem;

    @Override
	protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.page_screenshots_viewer);

        if(arg0 == null){
            currentItem = getIntent().getIntExtra("position", 0);
        }else{
            currentItem = arg0.getInt("position", 0);
        }

        getIntent().getIntExtra("position", 0);
        context = this;
        final ViewPager screenshots = (ViewPager) findViewById(R.id.screenShotsPager);

        ArrayList<String> uri = getIntent().getStringArrayListExtra("url");
        if (uri != null) {
            images = uri.toArray(images);
        }
        if(images != null && images.length > 0){
            screenshots.setAdapter(new ViewPagerAdapterScreenshots(context,uri,true));
            screenshots.setCurrentItem(currentItem);
        }

//        String imagepath = getIntent().getStringExtra(SCREEN);
//
//        ImageView screen = (ImageView) findViewById(R.id.screenshot);
//
//        Picasso.with(this)
//                .load(imagepath)
//                .error(getResources().getDrawable(R.drawable.default_background))
//                .into(screen);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", currentItem);
    }

}
