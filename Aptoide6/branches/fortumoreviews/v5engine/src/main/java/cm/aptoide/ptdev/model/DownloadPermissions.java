package cm.aptoide.ptdev.model;

/**
 * Created by asantos on 11-08-2014.
 */
public class DownloadPermissions {

    private boolean isWiFi;
    private boolean isEthernet;
    private boolean isWiMax;
    private boolean isMobile;

    public DownloadPermissions(boolean isWiFi, boolean isEthernet, boolean isWiMax, boolean isMobile) {
        this.isWiFi = isWiFi;
        this.isEthernet = isEthernet;
        this.isWiMax = isWiMax;
        this.isMobile = isMobile;
    }

    public boolean isWiFi() {
        return isWiFi;
    }

    public boolean isEthernet() {
        return isEthernet;
    }

    public boolean isWiMax() {
        return isWiMax;
    }

    public boolean isMobile() {
        return isMobile;
    }
}
