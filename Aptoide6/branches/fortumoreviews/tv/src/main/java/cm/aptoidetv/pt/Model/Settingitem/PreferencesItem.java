package cm.aptoidetv.pt.Model.Settingitem;

import android.content.Context;
import android.content.Intent;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.CardPresenter;
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
    public void startActivity(Context context) {
        context.startActivity(new Intent(context, SettingsWHeaders.class));
    }

    @Override
    public void setImage(int iconWidth, int iconHeight, CardPresenter.PicassoImageCardViewTarget picassoImageCardViewTarget) {
       AppTV.getPicasso()
                    .load(R.drawable.settings)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .centerInside()
                    .resize(iconWidth,iconHeight)
                            //   .error(mDefaultCardImage)
                    .into(picassoImageCardViewTarget);
    }
}
