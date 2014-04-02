package cm.aptoide.ptdev.fragments.callbacks;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 22-11-2013
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class RepoCompleteEvent {
    public long getRepoId() {
        return repoId;
    }

    private final long repoId;

    public RepoCompleteEvent(long repoId) {
        this.repoId = repoId;

    }
}
