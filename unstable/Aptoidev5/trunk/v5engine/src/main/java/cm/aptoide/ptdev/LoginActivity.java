package cm.aptoide.ptdev;

import android.accounts.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by brutus on 09-12-2013.
 */
public class LoginActivity extends AccountAuthenticatorActivity {



    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private final String TAG = "Login";

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_login);
        mAccountManager = AccountManager.get(getBaseContext());

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

        if (accountName != null) {
            ((EditText)findViewById(R.id.username)).setText(accountName);
        }

        findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        findViewById(R.id.new_to_aptoide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
                // and return them in setAccountAuthenticatorResult(). See finishLogin().
                Intent signup = new Intent(getBaseContext(), SignUpActivity.class);
                signup.putExtras(getIntent().getExtras());
                startActivityForResult(signup, REQ_SIGNUP);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(getBaseContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }

    public void submit() {

        final String userName = ((EditText) findViewById(R.id.username)).getText().toString();
        final String userPass = ((EditText) findViewById(R.id.password)).getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        CheckUserCredentialsRequest request = new CheckUserCredentialsRequest();

        request.setUser(userName);
        try {
            request.setPassword(AptoideUtils.Algorithms.computeSHA1sum(userPass));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Toast.makeText(getBaseContext(), "An error ocurred.", Toast.LENGTH_SHORT).show();
                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null){
                    pd.dismiss();
                }
            }

            @Override
            public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {

                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null){
                    pd.dismiss();
                }

                Toast.makeText(getBaseContext(), "Token is: " + checkUserCredentialsJson.getToken(), Toast.LENGTH_SHORT).show();


                Bundle data = new Bundle();
                data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, checkUserCredentialsJson.getToken());
                data.putString(PARAM_USER_PASS, userPass);
                final Intent res = new Intent();
                res.putExtras(data);
                finishLogin(res);


            }
        });

    }

    private void finishLogin(Intent intent) {
        Log.d("udinic", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("udinic", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d("udinic", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
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

    public static boolean isLoggedIn(Context context){

        AccountManager manager = AccountManager.get(context);

        return manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length!=0;

    }


}



