package cm.aptoide.ptdev;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import cm.aptoide.ptdev.configuration.AccountGeneral;

/**
 * Created by rmateus on 21-10-2014.
 */
public class TestActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = new Bundle();

        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, "MyName");
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, "cm.aptoide.pt");
        AccountManager.get(this).addAccount("cm.aptoide.pt", AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, new String[]{"timelineLogin"}, bundle, this, null, new Handler());

    }
}
