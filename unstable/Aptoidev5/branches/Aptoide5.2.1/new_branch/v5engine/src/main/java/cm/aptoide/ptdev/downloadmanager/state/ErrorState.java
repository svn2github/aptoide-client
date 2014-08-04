package cm.aptoide.ptdev.downloadmanager.state;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.DownloadManager;
import cm.aptoide.ptdev.downloadmanager.EnumDownloadFailReason;


/**
 * The error state represents the status of a download object when it has failed to download.
 * @author Edward Larsson (edward.larsson@gmx.com)
 */
public class ErrorState extends StatusState {

	/** The error message of this state. */
	private EnumDownloadFailReason mErrorMessage;

	/**
	 * @return The error message of this state.
	 */
	public EnumDownloadFailReason getErrorMessage() {
		return mErrorMessage;
	}

	/**
	 * Construct an error state with a message.
	 * @param downloadInfo The downloadInfo associated with this state.
	 * @param errorMessage The error message of this state.
	 */
	public ErrorState(DownloadInfo downloadInfo, EnumDownloadFailReason errorMessage) {
		super(downloadInfo);
		mErrorMessage = errorMessage;
	}

	@Override
	public void download() {
		mDownloadInfo.changeStatusState(new ActiveState(mDownloadInfo));
	}

	@Override
	public void changeFrom() {
        manager.removeFromErrorList(mDownloadInfo);
	}

	@Override
	public boolean changeTo() {

        if (manager.addToErrorList(mDownloadInfo)) {
			mDownloadInfo.setStatusState(this);
			return true;
		}

		return false;
	}

	@Override
	public void pause() {
		//do nothing, not active
	}

	@Override
	public int getQueuePosition() {
		return Integer.MAX_VALUE;
	}

    @Override
    public EnumState getEnumState() {
        return EnumState.ERROR;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public StatusState getShallowCopy() {
		return new ErrorState(null, mErrorMessage);
	}
}
