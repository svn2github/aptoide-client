/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoidetv.pt;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ScreenshotsViewer extends Activity {

    public static final String SCREEN = "image";

    @Override
	protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.page_screenshots_viewer);

        String imagepath = getIntent().getStringExtra(SCREEN);

        ImageView screen = (ImageView) findViewById(R.id.screenshot);

        Picasso.with(this)
                .load(imagepath)
                .error(getResources().getDrawable(R.drawable.default_background))
                .into(screen);
	}

}
