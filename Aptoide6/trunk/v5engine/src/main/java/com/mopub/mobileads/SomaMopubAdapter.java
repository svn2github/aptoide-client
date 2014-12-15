/*
package com.mopub.mobileads;

import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.mopub.mobileads.CustomEventBanner;

import com.smaato.soma.AdDownloaderInterface;
import com.smaato.soma.AdListenerInterface;
import com.smaato.soma.BannerStateListener;
import com.smaato.soma.BannerView;
import com.smaato.soma.BaseView;
import com.smaato.soma.ReceivedBannerInterface;
import com.smaato.soma.bannerutilities.constant.BannerStatus;
import com.smaato.soma.exception.ClosingLandingPageFailed;

public class SomaMopubAdapter extends CustomEventBanner {
    private static BannerView mBanner;
    */
/*
    * (non-Javadoc)
    * @see
    * com.mopub.mobileads.CustomEventBanner#loadBanner(android.content.Context,
    * com.mopub.mobileads.CustomEventBanner.CustomEventBannerListener,
    * java.util.Map, java.util.Map)
    *//*

    @Override
    public void loadBanner(Context context,
                           final CustomEventBannerListener customEventBannerListener,
                           Map<String, Object> localExtras,
                           Map<String, String> serverExtras) {
        try {
            if (mBanner == null) {
                mBanner = new BannerView(context);
                mBanner.addAdListener(new AdListenerInterface() {
                    @Override
                    public void onReceiveAd(AdDownloaderInterface arg0,
                                            ReceivedBannerInterface arg1) {
                        if (arg1.getStatus() == BannerStatus.ERROR) {
                            Log.e("Smaato", "NO_FILL");
                            customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                        } else {
                            Log.e("Smaato ", "Ad available");
                            customEventBannerListener.onBannerLoaded(mBanner);
                        }
                    }
                });
                mBanner.setBannerStateListener(new BannerStateListener() {
                    @Override
                    public void onWillOpenLandingPage(BaseView arg0) {
                        Log.d("MoPub Smaato Mediation adapter", "Banner Clicked");
                    }
                    @Override
                    public void onWillCloseLandingPage(BaseView arg0) throws
                            ClosingLandingPageFailed {
                        mBanner.asyncLoadNewBanner();
                        Log.d("MoPub Smaato Mediation adapter", "Banner Clicked");
                    }
                });
            }
            int publisherId = Integer.parseInt(serverExtras.get("publisherId"));
            int adSpaceId = Integer.parseInt(serverExtras.get("adSpaceId"));
            mBanner.getAdSettings().setPublisherId(publisherId);
            mBanner.getAdSettings().setAdspaceId(adSpaceId);

            mBanner.asyncLoadNewBanner();
        } catch (Throwable e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to load banner");
        }
    }
    */
/*
    * (non-Javadoc)
    * @see com.mopub.mobileads.CustomEventBanner#onInvalidate()
    *//*

    @Override
    public void onInvalidate() {
    }
}*/
