package cm.aptoide.ptdev;

import android.accounts.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;

import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;


import java.io.IOException;
import java.util.Locale;

/**
 * Created by brutus on 09-12-2013.
 */
public class LoginActivity extends AccountAuthenticatorActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {


    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private boolean showPassword = true;

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Aptoide-", "On Connected");


        final String serverId = "316068701674.apps.googleusercontent.com";


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final String token = GoogleAuthUtil.getToken(LoginActivity.this, mPlusClient.getAccountName(), "oauth2:server:client_id:" + serverId + ":api_scope:" + Scopes.PLUS_LOGIN);


                    final String username = mPlusClient.getAccountName();
                    final String name = mPlusClient.getCurrentPerson().getDisplayName();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            submit(Mode.GOOGLE, username, token, name);
                            //Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    if (mPlusClient != null && mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }
                    e.printStackTrace();
                } catch (UserRecoverableAuthException e) {
                    startActivityForResult(e.getIntent(), 90);
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                    if (mPlusClient != null && mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, R.string.error_occured, Toast.LENGTH_LONG).show();
                    if (mPlusClient != null && mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }
                }
            }
        }).start();


        mConnectionProgressDialog.dismiss();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mConnectionProgressDialog.isShowing()) {
            // The user clicked the sign-in button already. Start to resolve
            // connection errors. Wait until onConnected() to dismiss the
            // connection dialog.
            if (result.hasResolution()) {
                try {
                    result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    mPlusClient.connect();
                }
            }
        }

        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.g_sign_in_button && !mPlusClient.isConnected()) {
            if (mConnectionResult == null) {
                mPlusClient.connect();
                mConnectionProgressDialog.show();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        }

    }

    public enum Mode {APTOIDE, GOOGLE, FACEBOOK}

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";
    public final static String PARAM_USER_AVATAR = "USER_AVATAR";

    private final int REQ_SIGNUP = 1;

    private final String TAG = "Login";

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    EditText password_box;
    CheckBox checkShowPass;
    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {


            if (state.isOpened()) {
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        submit(Mode.FACEBOOK, user.getProperty("email").toString(), session.getAccessToken(), null);
                    }
                });
                request.executeAsync();
            }

        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);


        if (AccountManager.get(this).getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {
            finish();
            Toast.makeText(this, R.string.one_account_allowed, Toast.LENGTH_SHORT).show();
        } else {


            setContentView(R.layout.form_login);

            if (Build.VERSION.SDK_INT >= 8) {

                findViewById(R.id.g_sign_in_button).setOnClickListener(this);
                mConnectionProgressDialog = new ProgressDialog(this);
                mConnectionProgressDialog.setMessage(getString(R.string.signing_in));


                uiLifecycleHelper = new UiLifecycleHelper(this, statusCallback);
                uiLifecycleHelper.onCreate(savedInstanceState);
                mPlusClient = new PlusClient.Builder(this, this, this).build();

            }
            mAccountManager = AccountManager.get(getBaseContext());

            String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
            mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
            if (mAuthTokenType == null)
                mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

            if (accountName != null) {
                ((EditText) findViewById(R.id.username)).setText(accountName);
            }

            password_box = (EditText) findViewById(R.id.password);
            final Drawable hidePasswordRes = getResources().getDrawable(R.drawable.password_hide_blue);
            final Drawable showPasswordRes = getResources().getDrawable(R.drawable.password_hide_gray);
            password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
            password_box.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (password_box.getCompoundDrawables()[2] == null) {
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP){
                        return false;
                    }
                    if (event.getX() > password_box.getWidth() - password_box.getPaddingRight() - hidePasswordRes.getIntrinsicWidth()) {
                        if(showPassword){
                            showPassword=false;
                            password_box.setTransformationMethod(null);
                            password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
                        }else{
                            showPassword=true;
                            password_box.setTransformationMethod(new PasswordTransformationMethod());
                            password_box.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordRes, null);
                        }
                    }




                    return false;
                }
            });


            findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    submit(Mode.APTOIDE, username, password, null);
                }
            });

            TextView new_to_aptoide = (TextView) findViewById(R.id.new_to_aptoide);
            SpannableString newToAptoideString = new SpannableString(getString(R.string.new_to_aptoide));
            newToAptoideString.setSpan(new UnderlineSpan(), 0, newToAptoideString.length(), 0);
            new_to_aptoide.setText(newToAptoideString);
            new_to_aptoide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signup = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivityForResult(signup, REQ_SIGNUP);
                }
            });

            TextView forgot_password = (TextView) findViewById(R.id.forgot_password);
            SpannableString forgetString = new SpannableString(getString(R.string.forgot_passwd));
            forgetString.setSpan(new UnderlineSpan(), 0, forgetString.length(), 0);
            forgot_password.setText(forgetString);
            forgot_password.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent passwordRecovery = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.aptoide.com/account/password-recovery"));
                    startActivity(passwordRecovery);
                }
            });

            getSupportActionBar().setTitle(getString(R.string.setcredentials));
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.login_or_register));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= 8) {
            uiLifecycleHelper.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(Build.VERSION.SDK_INT>=8){
            uiLifecycleHelper.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        } else {
            if (requestCode == 90 && resultCode == RESULT_OK) {
                mPlusClient.connect();
            }
        }
        if(uiLifecycleHelper!=null){
            uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
        }


        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {

            ((EditText) findViewById(R.id.username)).setText(data.getStringExtra("username"));
            ((EditText) findViewById(R.id.password)).setText(data.getStringExtra("password"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    submit(Mode.APTOIDE, data.getStringExtra("username"), data.getStringExtra("password"), null);
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(getBaseContext());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 8) {
            uiLifecycleHelper.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 8) {
            uiLifecycleHelper.onPause();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mPlusClient != null && mPlusClient.isConnected()) {
            mPlusClient.disconnect();
        }
        spiceManager.shouldStop();
    }

    public void submit(Mode mode, final String username, final String passwordOrToken, String nameForGoogle) {

        //final String userName = ((EditText) findViewById(R.id.username)).getText().toString();
        //final String userPass = ((EditText) findViewById(R.id.password)).getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        CheckUserCredentialsRequest request = new CheckUserCredentialsRequest();

        request.setRegisterDevice(true);
        request.setMode(mode);

        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);


        request.setSdk(String.valueOf(AptoideUtils.HWSpecifications.getSdkVer()));
        request.setDeviceId(deviceId);
        request.setCpu(AptoideUtils.HWSpecifications.getCpuAbi() + "," + AptoideUtils.HWSpecifications.getCpuAbi2());
        request.setDensity(String.valueOf(AptoideUtils.HWSpecifications.getNumericScreenSize(LoginActivity.this)));
        request.setOpenGl(String.valueOf(AptoideUtils.HWSpecifications.getGlEsVer(LoginActivity.this)));
        request.setModel(Build.MODEL);
        request.setScreenSize(Filters.Screen.values()[AptoideUtils.HWSpecifications.getScreenSize(LoginActivity.this)].name().toLowerCase(Locale.ENGLISH));

        request.setUser(username);
        request.setPassword(passwordOrToken);
        request.setNameForGoogle(nameForGoogle);

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Toast.makeText(getBaseContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismiss();
                }
            }

            @Override
            public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {

                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismiss();
                }


                if ("OK".equals(checkUserCredentialsJson.getStatus())) {

                    //Toast.makeText(getBaseContext(), "Token is: " + checkUserCredentialsJson.getToken(), Toast.LENGTH_SHORT).show();

                    if(checkUserCredentialsJson.getQueue()!=null){
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                .edit()
                                .putString("queueName", checkUserCredentialsJson.getQueue())
                                .commit();
                    }
                    if(checkUserCredentialsJson.getAvatar()!=null){
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                .edit()
                                .putString("useravatar", checkUserCredentialsJson.getAvatar())
                                .commit();
                        
                    }
                    if(checkUserCredentialsJson.getUsername()!=null){
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                .edit()
                                .putString("username", checkUserCredentialsJson.getUsername())
                                .commit();
                    }

                    Bundle data = new Bundle();
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, checkUserCredentialsJson.getToken());
                    data.putString(PARAM_USER_PASS, passwordOrToken);


                    final Intent res = new Intent();
                    res.putExtras(data);
                    finishLogin(res);
                } else {
                    for (String error : checkUserCredentialsJson.getErrors()) {
                        Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

    }

    private void finishLogin(Intent intent) {
        Log.d("aptoide", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("aptoide", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d("aptoide", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        startService(new Intent(this, RabbitMqService.class));
        /*
        ContentResolver wiResolver = getContentResolver();
        wiResolver.setIsSyncable(account, STUB_PROVIDER_AUTHORITY, 1);
        wiResolver.setSyncAutomatically(account, STUB_PROVIDER_AUTHORITY, true);

        if(Build.VERSION.SDK_INT >= 8) {
            wiResolver.addPeriodicSync(account, STUB_PROVIDER_AUTHORITY, new Bundle(), WEB_INSTALL_POLL_FREQUENCY);
        }
        */

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

    public static boolean isLoggedIn(Context context) {

        AccountManager manager = AccountManager.get(context);

        return manager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length != 0;

    }


}



