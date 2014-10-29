package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.mdotm.android.listener.MdotMAdEventListener;
import com.mdotm.android.model.MdotMAdRequest;
import com.mdotm.android.utils.MdotMAdSize;
import com.mdotm.android.utils.MdotMLogger;
import com.mdotm.android.view.MdotMAdView;

import java.util.Map;

/**
 * Created by rmateus on 29-10-2014.
 */
public class MdotMAdBannerAdapter extends CustomEventBanner implements
        MdotMAdEventListener {

    private CustomEventBannerListener mBannerListener;
    private MdotMAdView mMdotMAdView;

    @Override
    public void onInvalidate() {

    }

    @Override
    public void onReceiveBannerAd() {

        if(mMdotMAdView != null) {
            Log.d("MoPub",
                    "MdotM banner ad loaded successfully. Showing ad...");
            mBannerListener.onBannerLoaded(mMdotMAdView);
            // mBannerListener.setAdContentView(mMdotMAdView);
        } else {

            onFailedToReceiveBannerAd();
        }
    }

    @Override
    public void onFailedToReceiveBannerAd() {

        Log.d("MoPub", "MdotM banner ad failed to load.");
        mBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);

    }

    @Override
    public void onBannerAdClick() {

        Log.d("MoPub", "MdotM banner ad clicked.");
        mBannerListener.onBannerClicked();

    }

    @Override
    public void onDismissScreen() {

    }

    @Override
    public void onReceiveInterstitialAd() {

    }

    @Override
    public void onFailedToReceiveInterstitialAd() {

        MdotMLogger.i(this, "Failed to receive interstitial ad");
    }

    @Override
    public void onInterstitialAdClick() {

        MdotMLogger.i(this, " interstitial AdClick");
    }

    @Override
    public void onInterstitialDismiss() {

        MdotMLogger.i(this, "interstitial dismiss");
    }

    @Override
    public void onLeaveApplicationFromBanner() {

        MdotMLogger.i(this, "banner LeaveApplication");
        mBannerListener.onLeaveApplication();
    }

    @Override
    public void onLeaveApplicationFromInterstitial() {

        MdotMLogger.i(this, "interstitial  LeaveApplication");
    }

    @Override
    public void willShowInterstitial() {

        MdotMLogger.i(this, "will show interstitial");
    }

    @Override
    public void didShowInterstitial() {

        MdotMLogger.i(this, "did show interstitial");
    }

    @Override
    protected void loadBanner(
            Context context,
            CustomEventBannerListener customEventBannerListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {

        Log.i("MoPub", "Server extra banner" + serverExtras.toString());
        Log.i("MoPub", "local extra banner " + localExtras.toString());
        mBannerListener = customEventBannerListener;

        Activity activity = null;
        if(context instanceof Activity) {
            activity = (Activity) context;
        } else {
            // You may also pass in an Activity Context in the
            // localExtras map
            // and retrieve it here.
        }

        if(activity == null) {
            mBannerListener.onBannerFailed(MoPubErrorCode.UNSPECIFIED);
            return;
        }

		/*
		 * You may also pass this String down in the serverExtras
		 * Map by specifying Custom Event Data in MoPub's web
		 * interface.
		 */
        String mdotmAppKey = "YOUR_MDOTM_APP_KEY";

        try {
            if(Build.VERSION.SDK_INT >= 7) {
                // For banner ad use the below code
                mMdotMAdView = new MdotMAdView(activity);
                MdotMAdRequest bannerRequest = new MdotMAdRequest();
                bannerRequest.setAppKey(mdotmAppKey);// add
                // your
                // MdotM
                // aap
                // key
                // here
                bannerRequest.setAdSize(MdotMAdSize.BANNER_320_50); // set
                // ad
                // size
                // Test mode 1 and for live ads set it
                // to "0"
                bannerRequest.setTestMode("1");

                // set it to true if you MdotM sdk to
                // cache the ad else set it
                // false
                bannerRequest.setEnableCaching(true);
                mMdotMAdView.loadAd(this, bannerRequest);
            } else {
                Log.e("MoPub",
                        "MdotM supports from android version 2.1 and above");
                onFailedToReceiveBannerAd();
                return;
            }
        } catch(Exception e) {
            Log.e("MoPub",
                    "MdotM supports from android version 2.1 and above");
            onFailedToReceiveBannerAd();
        }

    }

}