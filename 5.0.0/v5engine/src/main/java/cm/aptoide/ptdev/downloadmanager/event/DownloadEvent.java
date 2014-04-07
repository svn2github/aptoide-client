package cm.aptoide.ptdev.downloadmanager.event;

import cm.aptoide.ptdev.downloadmanager.state.StatusState;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 01-07-2013
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class DownloadEvent {


    public long getId() {
        return id;
    }

    private final long id;
    private StatusState mStatusState;


    public DownloadEvent(long id, StatusState mStatusState){
        this.id= id;
        this.mStatusState = mStatusState;
    }


    public StatusState getmStatusState() {
        return mStatusState;
    }
}
