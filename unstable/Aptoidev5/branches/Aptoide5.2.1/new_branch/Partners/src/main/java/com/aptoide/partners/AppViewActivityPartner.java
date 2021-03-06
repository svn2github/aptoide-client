package com.aptoide.partners;

import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.InstalledApkEvent;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.OnMultiVersionClick;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import com.adsdk.sdk.banner.AdView;
import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rmateus on 19-03-2014.
 */
public class AppViewActivityPartner extends cm.aptoide.ptdev.AppViewActivity {


    private AdView mAdView;

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");

    }

    private DialogInterface.OnDismissListener defaultDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {

        }
    };

    @Produce
    @Override
    public RatingEvent publishRating() {
        return super.publishRating();
    }

    @Produce
    @Override
    public SpecsEvent publishSpecs() {
        return super.publishSpecs();
    }


//    @Produce
//    @Override
//    public RelatedAppsEvent publishRelatedApps() {
//        return super.publishRelatedApps();
//    }

    @Produce
    @Override
    public DetailsEvent publishDetails() {
        return super.publishDetails();
    }

    @Override
    public DialogInterface.OnDismissListener getOnDismissListener() {

        if(Aptoide.getConfiguration() instanceof AptoideConfigurationPartners){
            if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getMultistores()){
                return super.getOnDismissListener();
            }else{
                return defaultDismissListener;
            }
        }else{
            return defaultDismissListener;
        }

    }

    @Subscribe
    @Override
    public void onRefresh(AppViewRefresh event) {
        super.onRefresh(event);
    }


    @Subscribe
    @Override
    public void onDownloadEventUpdate(DownloadEvent download) {
        super.onDownloadEventUpdate(download);
    }

    @Override
    @Subscribe
    public void onDownloadUpdate(Download download){
        super.onDownloadUpdate(download);
    }


    @Override
    @Subscribe
    public void onDownloadStatusUpdate(Download download) {
        super.onDownloadStatusUpdate(download);
    }
    @Override
    @Subscribe
    public void onInstalledEvent(InstalledApkEvent event) {
        super.onInstalledEvent(event);
    }

    @Override
    @Subscribe
    public void onUnInstalledEvent(UnInstalledApkEvent event) {
        super.onUnInstalledEvent(event);
    }

    @Override
    @Subscribe
    public void onSpinnerItemClick(OnMultiVersionClick event) {
        super.onSpinnerItemClick(event);
    }


    @Override
    public void loadPublicity() {

        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getShowAds()) {

            if (TextUtils.isEmpty(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getAdUnitId())) {
                super.loadPublicity();
//                Log.d("AppViewActivityPartner", "load mopub");
            } else {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.advertisement);
                layout.setVisibility(View.VISIBLE);
                layout.removeAllViews();

                mAdView = new AdView(this, "http://my.mobfox.com/request.php", ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getAdUnitId(), true, true);

                mAdView.setAdspaceWidth(320); // Optional, used to set the custom size of banner placement. Without setting it, the SDK will use default size of 320x50 or 300x50 depending on device type.

                mAdView.setAdspaceHeight(50);

                mAdView.setAdspaceStrict(false); // Optional, tells the server to only supply banner ads that are exactly of the desired size. Without setting it, the server could also supply smaller Ads when no ad of desired size is available.

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                layout.addView(mAdView, params);


                HashMap<String, String> adTypeArgs = new HashMap<String, String>();
                adTypeArgs.put("type", "mobfox");
                FlurryAgent.logEvent("AppView_Load_Publicity", adTypeArgs);
//                Log.d("AppViewActivityPartner", "load mobfox");
            }
        }

    }

    @Override
    public void destroyPublicity() {
        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getShowAds()) {

            if (TextUtils.isEmpty(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getAdUnitId())) {
                super.destroyPublicity();
//                Log.d("AppViewActivityPartner", "destroy mopub");
            } else {
                if(mAdView!=null) mAdView.release();
//                Log.d("AppViewActivityPartner", "destroy mobfox");
            }

        }
    }

    @Override
    public void loadGetLatest(boolean showLatestString) {
        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getSearchStores()){
            super.loadGetLatest(showLatestString);
        }
    }

    @Override
    public boolean isMultipleStores() {
        return ((AptoideConfigurationPartners)Aptoide.getConfiguration()).getMultistores();
    }

    @Override
    public void setShareButton() {
        if(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getShowTimeline()){
            super.setShareButton();
        }
    }

        @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

}
