package cm.aptoide.ptdev.events;

/**
 * Created by rmateus on 23-10-2014.
 */
public class SocialTimelineEvent {

    private boolean isRefresh;

    public SocialTimelineEvent(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }


    public boolean isRefresh() {
        return isRefresh;
    }
}
