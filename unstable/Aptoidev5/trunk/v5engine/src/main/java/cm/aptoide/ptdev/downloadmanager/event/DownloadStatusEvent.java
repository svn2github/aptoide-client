package cm.aptoide.ptdev.downloadmanager.event;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 01-07-2013
 * Time: 12:22
 * To change this template use File | Settings | File Templates.
 */
public class DownloadStatusEvent implements Serializable{


    private long id;

    public DownloadStatusEvent(long id) {

        this.id = id;
    }

    public long getId() {
        return id;
    }
}
