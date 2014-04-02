package com.aptoide.partners;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.*;

/**
 * Created by rmateus on 21-03-2014.
 */
public class SplashDialogFragment extends DialogFragment {


    private ImageView imageSplash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash, container, false);
    }

    private ImageLoadingListener listener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String uri, View v) {
            getView().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String uri, View v, FailReason failReason) {
            Log.e("Start-onLoadingFailed", "Failed to load splashscreen");
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                saveSplashscreenImageToSDCard("splashscreen_land.png");
            }else{
                saveSplashscreenImageToSDCard("splashscreen.png");
            }
            startTimer();
        }

        @Override
        public void onLoadingComplete(String uri, View v, Bitmap loadedImage) {
            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
            startTimer();
        }

        @Override
        public void onLoadingCancelled(String uri, View v) {
            Log.e("Start-loading splash image", "Image canceled");
            if(isAdded()){
                dismiss();
            }

        }
    };

    private void startTimer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isAdded()){
                    dismissAllowingStateLoss();
                }
            }
        }, 3000);
    }

    private DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(300))
            .cacheOnDisc(true)
            .build();


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageSplash = (ImageView) view.findViewById(R.id.splashscreen);



        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

            ImageLoader.getInstance().displayImage(((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getSplashscreenLand(), imageSplash, options, listener);
        }else{
            ImageLoader.getInstance().displayImage(((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getSplashscreen(), imageSplash, options, listener);
        }


//        String url =  ((AptoideConfigurationPartners) AptoidePartner.getConfiguration()).getSplashscreen();
//        Log.e("Aptoide-Image-Url", url);

    }

    private void saveSplashscreenImageToSDCard(String fileName) {
        // imageSplash.setImageResource(R.drawable.splashscreen_land);
        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;
        Bitmap bbicon = null;
        try {
            bbicon = BitmapFactory.decodeStream(getActivity().getAssets().open(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String extStorageDirectory = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/.aptoide_settings";
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
