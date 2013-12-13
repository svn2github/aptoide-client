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

    private String name;

    private Drawable pathIcon;

    private String timestamp;

    private String version;


    public RollBackItem(String name, Drawable pathIcon, String timestamp, String version){
        this.name = name;
        this.version = version;
        this.pathIcon = pathIcon;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public Drawable getPathIcon() {
        return pathIcon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

}
