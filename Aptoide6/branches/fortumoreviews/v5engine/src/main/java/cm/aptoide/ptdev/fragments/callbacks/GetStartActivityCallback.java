package cm.aptoide.ptdev.fragments.callbacks;

import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.events.SocialTimelineEvent;
import cm.aptoide.ptdev.events.SocialTimelineInitEvent;

public interface GetStartActivityCallback {

    public SpiceManager getSpiceManager();
    public String getSponsoredCache();
    public void matureUnlock();
    public void matureLock();

    public void timelineCallback();
    public void updateTimelineBadge();




    SocialTimelineEvent produceTimelineEvent();

    SocialTimelineInitEvent produceInitEvent();
}
