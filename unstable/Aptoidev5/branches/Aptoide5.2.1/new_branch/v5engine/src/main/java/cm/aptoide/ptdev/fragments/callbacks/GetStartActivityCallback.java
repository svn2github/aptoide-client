package cm.aptoide.ptdev.fragments.callbacks;

import com.octo.android.robospice.SpiceManager;

public interface GetStartActivityCallback {

    public SpiceManager getSpiceManager();
    public String getSponsoredCache();
    public void matureUnlock();
    public void matureLock();

}
