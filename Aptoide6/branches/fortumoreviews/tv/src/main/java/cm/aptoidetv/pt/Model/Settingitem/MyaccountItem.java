package cm.aptoidetv.pt.Model.Settingitem;

import android.content.Context;
import android.content.Intent;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.CardPresenter;
import cm.aptoidetv.pt.MyAccountActivity;
import cm.aptoidetv.pt.R;

/**
 * Created by asantos on 26-12-2014.
 */
public class MyaccountItem extends SettingItem {
    @Override
    public String getText(Context context) {
        return context.getString(R.string.login_or_register);
    }

    @Override
    public String getName(Context context) {
        return context.getString(R.string.my_account);
    }

    @Override
    public void startActivity(Context context) {
        context.startActivity(new Intent(context, MyAccountActivity.class));
    }

    @Override
    public void setImage(int iconWidth, int iconHeight, CardPresenter.PicassoImageCardViewTarget picassoImageCardViewTarget) {
        AppTV.getPicasso()
                .load(R.drawable.account)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .centerInside()
                .resize(iconWidth,iconHeight)
                        //   .error(mDefaultCardImage)
                .into(picassoImageCardViewTarget);
    }
}
