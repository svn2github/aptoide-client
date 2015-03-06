package cm.aptoide.ptdev.parser.events;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 25-11-2013
 * Time: 11:37
 * To change this template use File | Settings | File Templates.
 */
public class StopParseEvent {

    public long getRepoId() {
        return repoId;
    }

    private long repoId;

    public StopParseEvent(long repoId) {
        this.repoId = repoId;
    }
}
