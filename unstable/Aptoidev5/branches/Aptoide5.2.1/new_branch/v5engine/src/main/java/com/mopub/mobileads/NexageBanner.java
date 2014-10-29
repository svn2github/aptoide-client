package com.mopub.mobileads;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.nexage.android.DeviceLocation;
import com.nexage.android.NexageAdManager;
import com.nexage.android.NexageAdView;
import com.nexage.android.NexageAdViewListener;

public class NexageBanner extends CustomEventBanner implements NexageAdViewListener {

	private NexageAdView nexageAdView;
	private CustomEventBannerListener mBannerListener;
	private static final String TAG = "Nexage MoPub Banner Adapter";

	public static final String LOCATION_KEY = "location";
	public static final String POSITION_KEY = "position";

	@Override
	protected void loadBanner(Context ctx, CustomEventBannerListener customEventBannerListener,
			final Map<String, Object> localParams, Map<String, String> serverParams) {
        
		mBannerListener = customEventBannerListener;
        Log.v(TAG, "MoPub calling Nexage for a Banner ad with position: " + (String) serverParams.get(POSITION_KEY));

		if (!(ctx instanceof Activity)) {
			mBannerListener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
			return;
		}

		// Create your ad View in here, populate it, and make the ad request.
		NexageAdManager.setIsMediation(true);

		Location location = extractLocation(localParams);
		if (location != null) {
			DeviceLocation myDeviceLocationImplementation = new DeviceLocation() {

				@Override
				public Location getLocation() {
					return (Location) localParams.get(LOCATION_KEY);
				}
			};
			
			NexageAdManager.setLocationAwareness(myDeviceLocationImplementation);
		}

		nexageAdView = new NexageAdView((String) serverParams.get(POSITION_KEY), ctx);
		Log.d(TAG, "New Nexage Banner View ID is " + nexageAdView.toString());
		nexageAdView.setListener(this);
		nexageAdView.setRefreshInterval(0);
		nexageAdView.rollover();
	}

	private Location extractLocation(Map<String, Object> localExtras) {
		Object location = localExtras.get(LOCATION_KEY);
		if (location instanceof Location) {
			return (Location) location;
		}
		return null;
	}

	@Override
	protected void onInvalidate() {
        Log.v(TAG, "MoPub calling Nexage to invalidate the current banner view");
        
        if (nexageAdView != null) {
    		Log.d(TAG, "Destroying Nexage Banner View ID is " + nexageAdView.toString());
            Log.v(TAG, "Nexage cleaning up the view and listeners");
            nexageAdView.setEnabled(false);
            nexageAdView.setListener(null);
            nexageAdView = null;
            mBannerListener = null;
        }
	}

	@Override
	public void onReceiveAd(NexageAdView adView) {
		Log.v(TAG, "Got a new Nexage Ad");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage has an Ad");
            mBannerListener.onBannerLoaded(nexageAdView);
        }
	}

	@Override
	public void onFailedToReceiveAd(NexageAdView adView) {
		Log.v(TAG, "No Ad from Nexage");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage don't have an Ad");
            mBannerListener.onBannerFailed(NETWORK_NO_FILL);
        }
	}

	@Override
	public void onPresentScreen(NexageAdView adView) {
        Log.v(TAG, "Banner Ad expanded from Nexage");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage expanded an Ad");
            mBannerListener.onBannerClicked();
            mBannerListener.onBannerExpanded();
        }
	}

	@Override
	public void onDismissScreen(NexageAdView adView) {
        Log.v(TAG, "Banner Ad collapsed from Nexage");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage collapsed an expanded Ad");
            mBannerListener.onBannerCollapsed();
        }
	}

	@Override
	public void onHide(NexageAdView adView) {
	}

	@Override
	public void onResize(NexageAdView adView, int width, int height) {
        Log.v(TAG, "Banner Ad resized from Nexage");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage has resized an MRAID Ad");
            mBannerListener.onBannerClicked();
            mBannerListener.onBannerExpanded();
        }
	}
    
    @Override
	public void onResizeClosed(NexageAdView adView) {
        Log.v(TAG, "Banner Ad collapsed on a resized ad from Nexage");
        
        if (nexageAdView != null && mBannerListener != null) {
            Log.v(TAG, "Notifying MoPub that Nexage has closed an MRAID resized Ad");
            mBannerListener.onBannerCollapsed();
        }
	}
}
