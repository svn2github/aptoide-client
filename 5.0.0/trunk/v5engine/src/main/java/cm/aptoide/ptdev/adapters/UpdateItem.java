package cm.aptoide.ptdev.adapters;

/**
 * Created by rmateus on 17-03-2014.
 */
public class UpdateItem {

    private boolean update;
    private long id;
    private String versionName;
    private String name;
    private String icon;

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isUpdate() {
        return update;
    }

    public long getId() {
        return id;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }


}
