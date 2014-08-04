package cm.aptoide.ptdev.events;

/**
 * Created by rmateus on 15-04-2014.
 */
public class OnMultiVersionClick {

    private String repoName;
    private String package_name;
    private String versionName;
    private int versionCode;
    private int downloads;

    public OnMultiVersionClick(String repoName, String package_name, String versionName, int versionCode, int downloads) {
        this.repoName = repoName;
        this.package_name = package_name;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.downloads = downloads;
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

    public int getDownloads() {
        return downloads;
    }
}
