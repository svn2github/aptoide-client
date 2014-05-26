package cm.aptoide.ptdev.events;

/**
 * Created by rmateus on 15-04-2014.
 */
public class OnMultiVersionClick {

    private String repoName;
    private String package_name;
    private String versionName;
    private int versionCode;

    public OnMultiVersionClick(String repoName, String package_name, String versionName, int versionCode) {
        this.repoName = repoName;
        this.package_name = package_name;
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getPackage_name() {
        return package_name;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }
}
