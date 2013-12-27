package cm.aptoide.ptdev.model;

import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: tdeus
 * Date: 10/18/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class RollBackItem {

    public enum Action {
        INSTALLING("Installing"),
        UNINSTALLING("Unistalling"),
        UPDATING("Updating"),
        INSTALLED("Installed"),
        UNINSTALLED("Unistalled"),
        UPDATED("Updated");

        private String action;

        private Action(String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    private final String md5;

    private String name;

    private String pathIcon;

    private String timestamp;

    private String version;

    private String packageName;

    private Action action;


    public RollBackItem(String name, String packageName, String version, String pathIcon, String timestamp, String md5, Action action){
        this.name = name;
        this.packageName = packageName;
        this.version = version;
        this.pathIcon = pathIcon;
        this.timestamp = timestamp;
        this.md5 = md5;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public String getIconPath() { return pathIcon; }

    public String getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getMd5() { return md5; }

    public String getPackageName() {
        return packageName;
    }

    public Action getAction() { return action; }
}
