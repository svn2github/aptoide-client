package cm.aptoidetv.pt;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import cm.aptoidetv.pt.Model.Error;
import cm.aptoidetv.pt.SecurePrefs.SecurePreferences;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.old.AptoideUtils;
import cm.aptoidetv.pt.WebServices.old.CheckUserCredentialsRequest;
import cm.aptoidetv.pt.WebServices.old.Errors;
import cm.aptoidetv.pt.WebServices.old.OAuth2AuthenticationRequest;
import cm.aptoidetv.pt.WebServices.old.RabbitMqService;
import cm.aptoidetv.pt.WebServices.old.json.CheckUserCredentialsJson;
import cm.aptoidetv.pt.WebServices.old.json.OAuth;
import retrofit.RetrofitError;

public class LoginActivity extends AccountAuthenticatorActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener, ProgressDialogFragment.OnCancelListener {
    public static final String LOGIN_USER_LOGIN 	= "usernameLogin";

    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Aptoide account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Aptoide account";

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private boolean showPassword = true;
    private CheckBox registerDevice;
    private boolean hasQueue;
    private CheckUserCredentialsRequest request;
    private boolean fromPreviousAptoideVersion;
    private boolean removeAccount;

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Aptoide-", "On Connected");

        final String serverId = "928466497334-7aqsaffv18r3k1ebthkchfi3nibft5vq.apps.googleusercontent.com";
        new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final String token = GoogleAuthUtil.getToken(AppTV.getContext(), mPlusClient.getAccountName(), "oauth2:server:client_id:" + serverId + ":api_scope:" + Scopes.PLUS_LOGIN);
                    final String username = mPlusClient.getAccountName();
                    final String name = mPlusClient.getCurrentPerson().getDisplayName();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            submit(Mode.GOOGLE, username, token, name);
                            //Toast.makeText(Aptoide.getContext(), token, Toast.LENGTH_SHORT).show();
                        }
                    });

                }catch (UserRecoverableAuthException e) {
                    startActivityForResult(e.getIntent(), 90);
                }catch (Exception e) {

                    if (mPlusClient != null && mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AppTV.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();

                            DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                            if (pd != null) {
                                pd.dismissAllowingStateLoss();
                            }
                        }
                    });
                }
            }
        }).start();


        mConnectionProgressDialog.dismiss();
    }

    @Override
    public void onDisconnected() {
        DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
        if (pd != null) {
            pd.dismissAllowingStateLoss();
        }
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
            }else{
                mConnectionProgressDialog.dismiss();
            }
        }

        onDisconnected();
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

    @Override
    public void onCancel() {
        if (request != null) {
            if(spiceManager.isStarted())spiceManager.cancel(request);
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
    private SpiceManager spiceManager = new SpiceManager(HttpService.class);

    EditText password_box;
    CheckBox checkShowPass;
    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");

                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(final GraphUser user, Response response) {
                        if (session == Session.getActiveSession() && user != null) {

                            try {

                                if (removeAccount && mAccountManager.getAccountsByType(AppTV.getConfiguration().getAccountType()).length > 0) {
                                    mAccountManager.removeAccount(mAccountManager.getAccountsByType(AppTV.getConfiguration().getAccountType())[0], new AccountManagerCallback<Boolean>() {
                                        @Override
                                        public void run(AccountManagerFuture<Boolean> future) {
                                            submit(Mode.FACEBOOK, user.getProperty("email").toString(), session.getAccessToken(), null);
                                        }
                                    }, new Handler(Looper.getMainLooper()));
                                } else {
                                    submit(Mode.FACEBOOK, user.getProperty("email").toString(), session.getAccessToken(), null);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AppTV.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();

                                        DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                                        if (pd != null) {
                                            pd.dismissAllowingStateLoss();
                                        }

                                    }
                                });
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session.closeAndClearTokenInformation();
                        }
                    }
                });
                request.executeAsync();
            }
        }
    };



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_AppCompat);
        super.onCreate(savedInstanceState);

        String activityTitle = getString(R.string.login_or_register);

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            Bundle b = getIntent().getBundleExtra(ARG_OPTIONS_BUNDLE);

            if (b != null && b.getBoolean(OPTIONS_FASTBOOK_BOOL, false)) {
                activityTitle = getString(R.string.social_timeline);

                if (b.getBoolean(OPTIONS_LOGOUT_BOOL, false)) {
                    setContentView(R.layout.page_timeline_logout_and_login);
                    removeAccount = true;
                } else {
                    setContentView(R.layout.page_timeline_not_logged_in);
                }

            } else {
                initLogin(savedInstanceState);
            }*/
            initLogin(savedInstanceState);
            uiLifecycleHelper = new UiLifecycleHelper(this, statusCallback);
            uiLifecycleHelper.onCreate(savedInstanceState);

            mPlusClient = new PlusClient.Builder(this, this, this).build();

            LoginButton fbButton = (LoginButton) findViewById(R.id.fb_login_button);
            fbButton.setReadPermissions(Arrays.asList("email", "user_friends"));
/*            fbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlurryAgent.logEvent("Login_Page_Clicked_On_Login_With_Facebook");
                }
            });*/
            fbButton.setOnErrorListener(new LoginButton.OnErrorListener() {
                @Override
                public void onError(FacebookException error) {
                    error.printStackTrace();
                    Toast.makeText(AppTV.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                }
            });

        mAccountManager = AccountManager.get(getBaseContext());

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AUTHTOKEN_TYPE_FULL_ACCESS;

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(activityTitle);
    }

    private void initLogin(Bundle savedInstanceState) {

        if (AptoideUtils.isLoggedIn(this)) {
            finish();
            Toast.makeText(this, R.string.one_account_allowed, Toast.LENGTH_SHORT).show();
        } else {

            setContentView(R.layout.form_login);

            findViewById(R.id.g_sign_in_button).setOnClickListener(this);
            mConnectionProgressDialog = new ProgressDialog(this);
            mConnectionProgressDialog.setMessage(getString(R.string.signing_in));

            int val = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            boolean play_installed = val == ConnectionResult.SUCCESS;

            SignInButton signInButton = (SignInButton) findViewById(R.id.g_sign_in_button);
            if (!play_installed) {
                signInButton.setVisibility(View.GONE);
            }

            String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);

            if (accountName != null) {
                ((EditText) findViewById(R.id.username)).setText(accountName);
            }

            if (PreferenceManager.getDefaultSharedPreferences(this).contains(LOGIN_USER_LOGIN)) {
                ((EditText) findViewById(R.id.username)).setText(PreferenceManager.getDefaultSharedPreferences(this).getString(LOGIN_USER_LOGIN, ""));
                fromPreviousAptoideVersion = true;
            }

            password_box = (EditText) findViewById(R.id.password);
            password_box.setTransformationMethod(new PasswordTransformationMethod());

            findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();

                    if(username.length()==0 || password.length()==0 ){
                        Toast.makeText(getApplicationContext(), R.string.fields_cannot_empty, Toast.LENGTH_LONG).show();
                        return;
                    }
                    new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");

                    submit(Mode.APTOIDE, username, password, null);
                }
            });

            TextView new_to_aptoide = (TextView) findViewById(R.id.new_to_aptoide);
            SpannableString newToAptoideString = new SpannableString(getString(R.string.new_to_aptoide, AppTV.getConfiguration().getMarketName()));
            newToAptoideString.setSpan(new UnderlineSpan(), 0, newToAptoideString.length(), 0);
            new_to_aptoide.setText(newToAptoideString);
            new_to_aptoide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signup = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivityForResult(signup, REQ_SIGNUP);
                }
            });

            registerDevice = (CheckBox) findViewById(R.id.link_my_device);

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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(uiLifecycleHelper!=null) uiLifecycleHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
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
        if (uiLifecycleHelper != null) {
            uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
        }
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            hasQueue = true;
            finishLogin(data);
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
        uiLifecycleHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlusClient != null && mPlusClient.isConnected()) {
            mPlusClient.disconnect();
        }
        spiceManager.shouldStop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void submit(final Mode mode, final String username, final String passwordOrToken, final String nameForGoogle) {
        //final String userName = ((EditText) findViewById(R.id.username)).getAvatar().toString();
        //final String userPass = ((EditText) findViewById(R.id.password)).getAvatar().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
        OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();

        oAuth2AuthenticationRequest.setPassword(passwordOrToken);
        oAuth2AuthenticationRequest.setUsername(username);
        oAuth2AuthenticationRequest.setMode(mode);
        oAuth2AuthenticationRequest.setNameForGoogle(nameForGoogle);

        spiceManager.execute(oAuth2AuthenticationRequest, new RequestListener<OAuth>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                String error=null;
                if (spiceException.getCause() instanceof RetrofitError) {
                    final RetrofitError cause = (RetrofitError) spiceException.getCause();
                    if(cause.getResponse().getStatus() == 400){
                        error = getString(R.string.error_AUTH_1);
                    }else {
                        error = getString(R.string.error_occured);
                    }
                } else {
                    error = getString(R.string.error_occured);
                }

                Session session = Session.getActiveSession();

                if (session != null && session.isOpened()) {
                    session.closeAndClearTokenInformation();
                }
                if (mPlusClient != null && mPlusClient.isConnected()) {
                    mPlusClient.clearDefaultAccount();
                    mPlusClient.disconnect();
                }
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT).show();

                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }
            }

            @Override
            public void onRequestSuccess(final OAuth oAuth) {
                if (oAuth.getStatus() != null && oAuth.getStatus().equals("FAIL")) {

                    AptoideUtils.toastError(oAuth.getError());
                    Session session = Session.getActiveSession();

                    if (session != null && session.isOpened()) {
                        session.closeAndClearTokenInformation();
                    }
                    if (mPlusClient != null && mPlusClient.isConnected()) {
                        mPlusClient.clearDefaultAccount();
                        mPlusClient.disconnect();
                    }
                    DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if (pd != null) {
                        pd.dismissAllowingStateLoss();
                    }
                } else {
                    getUserInfo(oAuth, username, mode, accountType, passwordOrToken);
                }
            }
        });
    }

    private void getUserInfo(final OAuth oAuth, final String username, final Mode mode, final String accountType, final String passwordOrToken) {
        request = new CheckUserCredentialsRequest();
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        request.setRegisterDevice(registerDevice != null && registerDevice.isChecked());

        request.setSdk(String.valueOf(AptoideUtils.HWSpecifications.getSdkVer()));
        request.setDeviceId(deviceId);
        request.setCpu(AptoideUtils.HWSpecifications.getCpuAbi() + "," + AptoideUtils.HWSpecifications.getCpuAbi2());
        request.setDensity(String.valueOf(AptoideUtils.HWSpecifications.getNumericScreenSize(this)));
        request.setOpenGl(String.valueOf(AptoideUtils.HWSpecifications.getGlEsVer(this)));
        request.setModel(Build.MODEL);
        request.setScreenSize(Filters.Screen.values()[AptoideUtils.HWSpecifications.getScreenSize(this)].name().toLowerCase(Locale.ENGLISH));

        request.setToken(oAuth.getAccess_token());

        spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {

            @Override
            public void onRequestFailure(SpiceException e) {

                Session session = Session.getActiveSession();

                if (session != null && session.isOpened()) {
                    session.closeAndClearTokenInformation();
                }

                if (mPlusClient != null && mPlusClient.isConnected()) {
                    mPlusClient.clearDefaultAccount();
                    mPlusClient.disconnect();
                }

                Toast.makeText(getBaseContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();

                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }
            }

            @Override
            public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {

                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }


                if ("OK".equals(checkUserCredentialsJson.getStatus())) {
                    SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(AppTV.getContext()).edit();

                    if (null != checkUserCredentialsJson.getQueue()) {
                        hasQueue = true;

                        preferences.putString("queueName", checkUserCredentialsJson.getQueue());
                    }
                    if (null != (checkUserCredentialsJson.getAvatar())) {
                        preferences.putString("useravatar", checkUserCredentialsJson.getAvatar());
                    }

                    if (null != (checkUserCredentialsJson.getRepo())) {
                        preferences.putString("userRepo", checkUserCredentialsJson.getRepo());
                    }

                    if (null != (checkUserCredentialsJson.getUsername())) {
                        preferences.putString("username", checkUserCredentialsJson.getUsername());
                    }

                    preferences.putString(LOGIN_USER_LOGIN, username);

                    preferences.putString("loginType", mode.name());
                    preferences.commit();

                    SharedPreferences.Editor securePreferences = SecurePreferences.getInstance().edit();
                    securePreferences.putString("access_token", oAuth.getAccess_token());
                    securePreferences.putString("devtoken",checkUserCredentialsJson.getToken());

                    securePreferences.commit();

                    Bundle data = new Bundle();
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, oAuth.getRefreshToken());
                    data.putString(PARAM_USER_PASS, passwordOrToken);

                    final Intent res = new Intent();
                    res.putExtras(data);
                    finishLogin(res);
                } else {
                    final HashMap<String, Integer> errorsMapConversion = Errors.getErrorsMap();
                    Integer stringId;
                    String message;
                    for (Error error : checkUserCredentialsJson.getErrors()) {
                        stringId = errorsMapConversion.get(error.getCode());
                        if (stringId != null) {
                            message = getString(stringId);
                        } else {
                            message = error.getMsg();
                        }

                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void finishLogin(Intent intent) {

        Log.d("aptoide", TAG + "> finishLogin");
        Log.d("pois", "has accountName: "+intent.hasExtra(AccountManager.KEY_ACCOUNT_NAME));
        Log.d("pois", "has accountPassword: "+intent.hasExtra(PARAM_USER_PASS));
        Log.d("pois", "has KEY_ACCOUNT_TYPE: "+intent.hasExtra(AccountManager.KEY_ACCOUNT_TYPE));
        Log.d("pois", "has ARG_IS_ADDING_NEW_ACCOUNT: "+intent.hasExtra(ARG_IS_ADDING_NEW_ACCOUNT));

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        Log.d("pois", "accountName: "+accountName);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        Log.d("pois", "accountPassword: "+accountPassword);
        Log.d("pois", "KEY_ACCOUNT_TYPE: "+intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
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

        if(fromPreviousAptoideVersion){
            PreferenceManager.getDefaultSharedPreferences(this).edit().remove(LOGIN_USER_LOGIN).commit();
        }

        /*
        ContentResolver wiResolver = getContentResolver();
        wiResolver.setIsSyncable(account, STUB_PROVIDER_AUTHORITY, 1);
        wiResolver.setSyncAutomatically(account, STUB_PROVIDER_AUTHORITY, true);

        if(Build.VERSION.SDK_INT >= 8) {
            wiResolver.addPeriodicSync(account, STUB_PROVIDER_AUTHORITY, new Bundle(), WEB_INSTALL_POLL_FREQUENCY);
        }
        */
        finish();
        if(registerDevice != null && registerDevice.isChecked() && hasQueue) startService(new Intent(this, RabbitMqService.class));
        ContentResolver.setSyncAutomatically(account, AppTV.getConfiguration().getUpdatesSyncAdapterAuthority(), true);
        ContentResolver.addPeriodicSync(account, AppTV.getConfiguration().getUpdatesSyncAdapterAuthority(), new Bundle(), 43200);
        ContentResolver.setSyncAutomatically(account, AppTV.getConfiguration(). getAutoUpdatesSyncAdapterAuthority(), true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home || i == R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}



