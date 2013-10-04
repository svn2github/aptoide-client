/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import android.os.Bundle;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;


public class HWSpecActivity extends SherlockActivity /*SherlockActivity */{


	private HWSpecifications specs ;
	private TextView sdkVer;
	private TextView screenSize;
	private TextView esglVer;
    private TextView cpuAbi;
    private TextView screenCode;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		AptoideThemePicker.setAptoideTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.hwspecs);
//		getSupportActionBar().hide();

		sdkVer= (TextView) findViewById(R.id.sdkver);
		screenSize = (TextView) findViewById(R.id.screenSize);
		esglVer = (TextView) findViewById(R.id.esglVer);
        cpuAbi = (TextView) findViewById(R.id.cpuAbi);
        screenCode = (TextView) findViewById(R.id.screenCode);

		sdkVer.setText(HWSpecifications.getSdkVer()+"");
		screenSize.setText(HWSpecifications.getScreenSize(this)+"");
		esglVer.setText(HWSpecifications.getGlEsVer(this));
        cpuAbi.setText(HWSpecifications.getCpuAbi() + " " + HWSpecifications.getCpuAbi2());
        screenCode.setText(HWSpecifications.getNumericScreenSize(this) + "/" + HWSpecifications.getDensityDpi(this));


	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == android.R.id.home) {
//			finish();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}

