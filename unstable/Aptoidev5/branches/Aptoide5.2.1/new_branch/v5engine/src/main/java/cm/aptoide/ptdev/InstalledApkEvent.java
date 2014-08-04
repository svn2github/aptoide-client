package cm.aptoide.ptdev;

import cm.aptoide.ptdev.model.InstalledPackage;

/**
 * Created by rmateus on 13-12-2013.
 */
public class InstalledApkEvent {
    private InstalledPackage apk;

    public InstalledApkEvent(InstalledPackage apk) {
        this.apk = apk;
    }

    public InstalledPackage getApk() {
        return apk;
    }
}
