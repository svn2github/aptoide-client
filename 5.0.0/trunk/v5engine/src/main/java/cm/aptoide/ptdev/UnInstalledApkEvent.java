package cm.aptoide.ptdev;

/**
 * Created by rmateus on 13-12-2013.
 */
public class UnInstalledApkEvent {

    private String packageName;

    public UnInstalledApkEvent(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
