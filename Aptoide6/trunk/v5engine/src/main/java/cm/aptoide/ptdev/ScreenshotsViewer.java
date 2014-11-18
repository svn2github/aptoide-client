/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import cm.aptoide.ptdev.adapters.ViewPagerAdapterScreenshots;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.util.ArrayList;

public class ScreenshotsViewer extends ActionBarActivity {

	String url;
	private String[] images = new String[0];
	Context context;
    private int currentItem;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
	protected void onCreate(Bundle arg0) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.page_screenshots_viewer);

        if(arg0 == null){
            currentItem = getIntent().getIntExtra("position", 0);
        }else{
            currentItem = arg0.getInt("position", 0);
        }

        getIntent().getIntExtra("position", 0);
//		getSupportActionBar().hide();
		context = this;
		final ViewPager screenshots = (ViewPager) findViewById(R.id.screenShotsPager);
//		final CirclePageIndicator pi = (CirclePageIndicator) findViewById(R.id.indicator);
//		pi.setCentered(true);
//		pi.setSnap(true);
//		pi.setRadius(7.5f);
//		TypedValue a = new TypedValue();
//		getTheme().resolveAttribute(R.attr.custom_color, a, true);
//		pi.setFillColor(a.data);
        ArrayList<String> uri = getIntent().getStringArrayListExtra("url");
        if (uri != null) {
            images = uri.toArray(images);
        }
        if(images != null && images.length > 0){
            screenshots.setAdapter(new ViewPagerAdapterScreenshots(context,uri,true));
            screenshots.setCurrentItem(currentItem);
        }

	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", currentItem);
    }
}
