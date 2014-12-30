package cm.aptoidetv.pt.Model.Settingitem;

import cm.aptoidetv.pt.Model.BindInterface;

/**
 * Created by asantos on 26-12-2014.
 */
public abstract class SettingItem implements BindInterface {
    @Override
    public boolean isEditorsChoice() {
        return false;
    }
    @Override
    public String getVersion() {
        return null;
    }
    @Override
    public String getDownloads() {
        return null;
    }
    @Override
    public String getDownloadUrl() {
        return null;
    }
}
