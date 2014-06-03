package cm.aptoide.ptdev.model;

/**
 * Created by rmateus on 14-04-2014.
 */
public class MultiStoreItem {

    final String version;
    String name;
    int versionCode;
    private String packageName;

    public MultiStoreItem(String version, String name, int versionCode, String packageName) {
        this.name = name;
        this.version = version;
        this.versionCode = versionCode;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @Override
    public String toString() {
        return name + " - " + version;
    }

    public String getVersion() {
        return version;
    }

    public String getPackageName() {
        return packageName;
    }
}
