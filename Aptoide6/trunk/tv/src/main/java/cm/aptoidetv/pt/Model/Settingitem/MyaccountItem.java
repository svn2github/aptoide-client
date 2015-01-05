package cm.aptoidetv.pt.Model.Settingitem;

import android.content.Context;
import android.content.Intent;

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
    public String getImage() {
        return "http://pool.img.aptoide.com/apps/e8ab34654c33b2e54713db0a4c0e5fc9_icon.png";
    }

    @Override
    public void startActivity(Context context) {
        context.startActivity(new Intent(context, MyAccountActivity.class));
    }
}
