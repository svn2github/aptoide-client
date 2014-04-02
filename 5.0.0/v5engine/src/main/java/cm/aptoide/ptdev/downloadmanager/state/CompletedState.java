package cm.aptoide.ptdev.downloadmanager.state;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;

/**
 * The completed state represents the status of a download object when it has finished downloading.
 * @author Edward Larsson (edward.larsson@gmx.com)
 */
public class CompletedState extends StatusState {

	/**
	 * Construct a completed state.
	 * @param downloadInfo The downloadInfo associated with this state.
	 */
	public CompletedState(DownloadInfo downloadInfo) {
		super(downloadInfo);
	}

	@Override
	public void download() {
        mDownloadInfo.changeStatusState(new ActiveState(mDownloadInfo));
	}

	@Override
	public void changeFrom() {

        manager.removeFromCompletedList(mDownloadInfo);
	}

	@Override
	public boolean changeTo() {
		if (manager.addToCompletedList(mDownloadInfo)) {
			mDownloadInfo.setStatusState(this);
			return true;
		}

		return false;
	}

	@Override
	public void pause() {
		//do nothing
	}

	@Override
	public int getQueuePosition() {
		return Integer.MAX_VALUE;
	}

    @Override
    public EnumState getEnumState() {
        return EnumState.COMPLETE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public StatusState getShallowCopy() {
		return new CompletedState(null);
	}
}
