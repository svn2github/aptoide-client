package com.aptoide.partners;

import android.widget.Toast;
import cm.aptoide.ptdev.InstalledApkEvent;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by rmateus on 19-03-2014.
 */
public class AppViewActivity extends cm.aptoide.ptdev.AppViewActivity {


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


    @Produce
    @Override
    public RelatedAppsEvent publishRelatedApps() {
        return super.publishRelatedApps();
    }

    @Produce
    @Override
    public DetailsEvent publishDetails() {
        return super.publishDetails();
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
    public void loadPublicity() {
        Toast.makeText(this, "loading ads from partners", Toast.LENGTH_LONG).show();
        //super.loadPublicity();
    }

    @Override
    public void destroyPublicity() {
        //super.destroyPublicity();
    }
}
