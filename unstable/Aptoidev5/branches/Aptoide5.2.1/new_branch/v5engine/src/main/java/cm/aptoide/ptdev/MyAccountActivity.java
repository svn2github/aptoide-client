package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.services.RabbitMqService;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.plus.PlusClient;

/**
 * Created by rmateus on 31-12-2013.
 */
public class MyAccountActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private AccountManager mAccountManager;
    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {

        }
    };
    private PlusClient mPlusClient;

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            uiLifecycleHelper.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            uiLifecycleHelper.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            uiLifecycleHelper.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            uiLifecycleHelper.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            uiLifecycleHelper = new UiLifecycleHelper(this, statusCallback);
            uiLifecycleHelper.onCreate(savedInstanceState);
        }
        mPlusClient = new PlusClient.Builder(this, this, this).build();
        mAccountManager = AccountManager.get(this);

        if (mAccountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType()).length > 0) {

            setContentView(R.layout.form_logout);
            final Account account = mAccountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];

            ((TextView) findViewById(R.id.username)).setText(account.name);

            findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlurryAgent.logEvent("My_Account_Clicked_On_Logout_Button");

                    if (Build.VERSION.SDK_INT >= 8) {
                        Session session = new Session(MyAccountActivity.this);
                        Session.setActiveSession(session);
                        if (Session.getActiveSession() != null) {
                            Session.getActiveSession().closeAndClearTokenInformation();
                        }
                    }

                    if (mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    sharedPreferences.edit().remove("queueName").commit();
                    stopService(new Intent(MyAccountActivity.this, RabbitMqService.class));
                    ContentResolver.setIsSyncable(account, Constants.WEBINSTALL_SYNC_AUTHORITY, 0);
                    ContentResolver.setSyncAutomatically(account, Constants.WEBINSTALL_SYNC_AUTHORITY, false);
                    if(Build.VERSION.SDK_INT>=8){
                        ContentResolver.removePeriodicSync(account, Constants.WEBINSTALL_SYNC_AUTHORITY, new Bundle());
                    }
                    mAccountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                        @Override
                        public void run(AccountManagerFuture<Boolean> future) {
                            //addAccount();
                            finish();
                        }
                    }, null);

                }
            });

        } else {
            addAccount();
            finish();
        }

        getSupportActionBar().setTitle(getString(R.string.sign_out));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 8) mPlusClient.connect();
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 8) mPlusClient.disconnect();
        FlurryAgent.onEndSession(this);
    }

    private void addAccount() {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    //showMessage("Account was created");
                    if (bnd.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                        setContentView(R.layout.form_logout);
//                        Log.d("udinic", "AddNewAccount Bundle is " + bnd);
                    } else {
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    //showMessage(e.getMessage());
                }
            }
        }, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
