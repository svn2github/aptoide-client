package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.configuration.AccountGeneral;

/**
 * Created by rmateus on 31-12-2013.
 */
public class MyAccountActivity extends ActionBarActivity {

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);

        if(mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0){

            setContentView(R.layout.form_logout);
            final Account account = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

            ((TextView)findViewById(R.id.username)).setText(account.name);

            findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mAccountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                        @Override
                        public void run(AccountManagerFuture<Boolean> future) {
                            addAccount();
                            finish();
                        }
                    }, null);

                }
            });

        }else{
            addAccount();
            finish();
        }


    }

    private void addAccount() {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    //showMessage("Account was created");
                    if(bnd.containsKey(AccountManager.KEY_AUTHTOKEN)){
                        setContentView(R.layout.form_logout);
                        Toast.makeText(MyAccountActivity.this, "Account was created", Toast.LENGTH_LONG).show();
                        Log.d("udinic", "AddNewAccount Bundle is " + bnd);
                    }else{
                        finish();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    //showMessage(e.getMessage());
                }
            }
        }, null);
    }
}
