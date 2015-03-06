package cm.aptoidetv.pt.Model.Settingitem;

import cm.aptoidetv.pt.CardPresenter;
import cm.aptoidetv.pt.Model.BindInterface;

/**
 * Created by asantos on 26-12-2014.
 */
public abstract class SettingItem implements BindInterface {
    @Override
    public int getWidth() {
        return CardPresenter.ICON_WIDTH;
    }
    @Override
    public int getHeight() {
        return CardPresenter.ICON_HEIGHT;
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
