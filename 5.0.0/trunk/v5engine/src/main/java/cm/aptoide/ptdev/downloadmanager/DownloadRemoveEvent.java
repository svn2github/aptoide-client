package cm.aptoide.ptdev.downloadmanager;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-07-2013
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class DownloadRemoveEvent {
    private final long id;

    public DownloadRemoveEvent(long id) {
        this.id=id;
    }

    public long getId() {
        return id;
    }
}
