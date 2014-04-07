package cm.aptoide.ptdev.events;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 26-11-2013
 * Time: 10:30
 * To change this template use File | Settings | File Templates.
 */
public class RepoErrorEvent {


    public Exception getE() {
        return e;
    }

    public long getRepoId() {
        return repoId;
    }

    private final Exception e;
    private final long repoId;

    public RepoErrorEvent(Exception e , long repoId) {
        this.e = e;
        this.repoId = repoId;
    }
}
