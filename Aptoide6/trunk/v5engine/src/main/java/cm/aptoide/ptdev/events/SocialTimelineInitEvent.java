package cm.aptoide.ptdev.events;

/**
 * Created by rmateus on 24-10-2014.
 */
public class SocialTimelineInitEvent {

    private boolean isRefresh;

    public SocialTimelineInitEvent(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }


    public boolean isRefresh() {
        return isRefresh;
    }

}
