package cm.aptoidetv.pt.Model.Settingitem;

import android.content.Context;
import android.content.Intent;

import cm.aptoidetv.pt.R;
import cm.aptoidetv.pt.SettingsWHeaders;

/**
 * Created by asantos on 26-12-2014.
 */
public class PreferencesItem extends SettingItem {

    @Override
    public String getText(Context context) {
        return context.getString(R.string.preferences_text);
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.preferences);
    }

    @Override
    public String getImage() {
        return "https://www.aptoide.com/imgs/b/6/2/b62b1c9459964d3a876b04c70036b10a_ravatar_96x96.png";
    }

    @Override
    public void startActivity(Context context) {
        context.startActivity(new Intent(context, SettingsWHeaders.class));
    }
}
