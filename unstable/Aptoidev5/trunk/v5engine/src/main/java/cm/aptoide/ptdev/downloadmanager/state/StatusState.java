package cm.aptoide.ptdev.downloadmanager.state;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.events.BusProvider;

/**
 * A StatusState is a state in which a {@link DownloadInfo} can be and helps to perform some status specific actions.
 * @author Edward Larsson (edward.larsson@gmx.com)
 */
public abstract class StatusState {

	/** The download object this state is associated with. */
	protected DownloadInfo mDownloadInfo;
    protected DownloadManager manager;

    /**
	 * Construct a status state.
	 * @param downloadObject The downloadObject associated with this state.
	 */
	protected StatusState(DownloadInfo downloadObject) {
        mDownloadInfo = downloadObject;
        this.manager = downloadObject.getDownloadManager();
    }

	/**
	 * @return a shallow copy of this status state.
	 */
	public abstract StatusState getShallowCopy();

	/**
	 * @return The download object wrapped in this statusState.
	 */
	public DownloadInfo getDownloadObject() {
		return mDownloadInfo;
	}

	/**
	 * Try to start downloading.
	 */
	public abstract void download();

	/**
	 * Open the file that is at the destination of the wrapped download object.
	 */
	public void openFile() {

	}

	/**
	 * Try to pause downloading.
	 */
	public abstract void pause();

	/**
	 * Try to change a download object's state from this status state to another.
	 * @param state The status state to change to.
	 */
	public void changeTo(StatusState state) {
        manager.updatePendingList();
		if (state.changeTo()) {
			changeFrom();
            BusProvider.getInstance().post(new DownloadStatusEvent(mDownloadInfo.getId()));
            mDownloadInfo = null;
		}
    }

	/**
	 * Change from this state.
	 */
	public abstract void changeFrom();

	/**
	 * Change to this state.
	 * @return <tt>true</tt> the change was successful, <tt>false</tt> otherwise.
	 */
	public abstract boolean changeTo();

	/**
	 * Remove a download object from the download manager.
	 */
	public void remove() {
		changeFrom();
	}

	/**
	 * @return The download object's queue position, or an empty String if the download is not queuing.
	 */
	public abstract int getQueuePosition();


    public abstract EnumState getEnumState();
}
