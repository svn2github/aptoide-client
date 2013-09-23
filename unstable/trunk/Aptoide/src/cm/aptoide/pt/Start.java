package cm.aptoide.pt;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.*;

public class Start extends SherlockActivity {


	private String LOCAL_PATH = AptoideConfiguration.getInstance().getPathCache();

	/**
	 * The thread to process splash screen events
	 */
	private Thread mSplashThread = new Thread();
	ImageView imageSplash;

	private ImageLoadingListener listener = new ImageLoadingListener() {

		@Override
		public void onLoadingStarted(String uri, View v) { }

		@Override
		public void onLoadingFailed(String uri, View v, FailReason failReason) {
			Log.e("Start-onLoadingFailed","Failed to load splashscreen");
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				saveSplashscreenImageToSDCard("splashscreen_land.png");
			}else{
				saveSplashscreenImageToSDCard("splashscreen.png");
			}
			showSplash();
		}

		@Override
		public void onLoadingComplete(String uri, View v, Bitmap loadedImage) {
			showSplash();
		}

		@Override
		public void onLoadingCancelled(String uri, View v) {	}
	};


	private DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(300))
            .cacheOnDisc()
            .build();


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		AptoideThemePicker.setAptoideTheme(this);
		super.onCreate(savedInstanceState);
		if(ApplicationAptoide.SPLASHSCREEN == null){
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
		}else{
            File file = new File(LOCAL_PATH + "/icons");
            if (!file.exists()) {
                file.mkdirs();
            }
            // Splash screen view
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.splash);
            imageSplash = (ImageView) findViewById(R.id.splashscreen);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                ImageLoader.getInstance().displayImage(ApplicationAptoide.SPLASHSCREENLAND, imageSplash, options, listener);
            }else{
                ImageLoader.getInstance().displayImage(ApplicationAptoide.SPLASHSCREEN, imageSplash, options, listener);
            }
		}
	}

	private void showSplash() {
		final Start sPlashScreen = this;

		// The thread to wait for splash screen events
		mSplashThread =  new Thread(){
			@Override
			public void run(){
				try {
					synchronized(this){
						// Wait given period of time or exit on touch
						wait(3000);
					}
				}
				catch(InterruptedException ex){
				}

				finish();

				// Run next activity
				Intent intent = new Intent();
				intent.setClass(sPlashScreen, MainActivity.class);
				startActivity(intent);
				//                stop();
			}
		};

		mSplashThread.start();
	}

	/**
	 * Processes splash screen touch events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent evt)
	{
		if(ApplicationAptoide.PARTNERID != null){
			if(evt.getAction() == MotionEvent.ACTION_DOWN)
			{
				synchronized(mSplashThread){
					mSplashThread.notifyAll();
				}
			}
		}
		return true;
	}

	private void saveSplashscreenImageToSDCard(String fileName) {
		// imageSplash.setImageResource(R.drawable.splashscreen_land);
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		Bitmap bbicon = null;
		try {
				bbicon = BitmapFactory.decodeStream(getAssets().open(fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String extStorageDirectory = Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/.aptoide";
		File wallpaperDirectory = new File(extStorageDirectory);

		OutputStream outStream = null;
		File file = new File(wallpaperDirectory, fileName);
		// to get resource name
		// getResources().getResourceEntryName(R.drawable.icon);

		if (file.exists()) {
			try {
				imageSplash.setImageBitmap(BitmapFactory
						.decodeStream(new FileInputStream(file)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {

			try {
				outStream = new FileOutputStream(file);
				bbicon.compress(Bitmap.CompressFormat.PNG, 100,outStream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
				Log.e("Start-loading splash image", "Image error");
			} finally {
				try {
					outStream.flush();
					outStream.close();
					imageSplash.setImageBitmap(BitmapFactory
							.decodeStream(new FileInputStream(file)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
