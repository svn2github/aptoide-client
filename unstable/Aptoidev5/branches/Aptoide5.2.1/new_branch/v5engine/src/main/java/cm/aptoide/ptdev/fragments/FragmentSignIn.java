package cm.aptoide.ptdev.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.flurry.android.FlurryAgent;
import com.google.api.client.util.Data;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.HashMap;
import java.util.Locale;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Configs;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.OAuth2AuthenticationRequest;
import cm.aptoide.ptdev.webservices.exceptions.InvalidGrantSpiceException;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import cm.aptoide.ptdev.webservices.json.OAuth;

/**
 * Created by rmateus on 22-10-2014.
 */
public class FragmentSignIn extends Fragment {

    public static final String LOGIN_MODE_ARG = "loginMode";
    public static final String LOGIN_USERNAME_ARG = "loginUsername";
    public static final String LOGIN_PASSWORD_OR_TOKEN_ARG = "loginPasswordOrToken";
    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);


    LoginActivity.Mode mode;
    private String username;
    private String password;
    private CheckUserCredentialsRequest request;
    private Callback callback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = LoginActivity.Mode.values()[getArguments().getInt(LOGIN_MODE_ARG, 0)];
        username = getArguments().getString(LOGIN_USERNAME_ARG);
        password = getArguments().getString(LOGIN_PASSWORD_OR_TOKEN_ARG);

    }

    @Override
    public void onResume() {
        super.onResume();
        submit(mode, username, password, "");
    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_signing_in, container, false);
    }

    public void submit(final LoginActivity.Mode mode, final String username, final String passwordOrToken, final String nameForGoogle) {

        //final String userName = ((EditText) findViewById(R.id.username)).getAvatar().toString();
        //final String userPass = ((EditText) findViewById(R.id.password)).getAvatar().toString();

        final String accountType = Aptoide.getConfiguration().getAccountType();

        OAuth2AuthenticationRequest oAuth2AuthenticationRequest = new OAuth2AuthenticationRequest();

        oAuth2AuthenticationRequest.setPassword(passwordOrToken);
        oAuth2AuthenticationRequest.setUsername(username);
        oAuth2AuthenticationRequest.setMode(mode);
        oAuth2AuthenticationRequest.setNameForGoogle(nameForGoogle);



        spiceManager.execute(oAuth2AuthenticationRequest, new RequestListener<OAuth>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

                String error;

                if(spiceException.getCause() instanceof InvalidGrantSpiceException && spiceException.getCause().getMessage().equals("Invalid username and password combination")){
                    error = getString(R.string.error_AUTH_1);
                } else {
                    error = getString(R.string.error_occured);
                }

                Session session = Session.getActiveSession();

                if (session != null && session.isOpened()) {
                    session.closeAndClearTokenInformation();
                }

                Toast.makeText(Aptoide.getContext(), error, Toast.LENGTH_SHORT).show();
                onError();

                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }
            }

            @Override
            public void onRequestSuccess(final OAuth oAuth) {

                getUserInfo(oAuth, username, mode, accountType, passwordOrToken);

            }
        });

    }

    private void getUserInfo(final OAuth oAuth, final String username, final LoginActivity.Mode mode, final String accountType, final String passwordOrToken) {
        request = new CheckUserCredentialsRequest();


        String deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        request.setRegisterDevice(true);

        request.setSdk(String.valueOf(AptoideUtils.HWSpecifications.getSdkVer()));
        request.setDeviceId(deviceId);
        request.setCpu(AptoideUtils.HWSpecifications.getCpuAbi() + "," + AptoideUtils.HWSpecifications.getCpuAbi2());
        request.setDensity(String.valueOf(AptoideUtils.HWSpecifications.getNumericScreenSize(getActivity())));
        request.setOpenGl(String.valueOf(AptoideUtils.HWSpecifications.getGlEsVer(getActivity())));
        request.setModel(Build.MODEL);
        request.setScreenSize(Filters.Screen.values()[AptoideUtils.HWSpecifications.getScreenSize(getActivity())].name().toLowerCase(Locale.ENGLISH));

        request.setToken(oAuth.getAccess_token());

        spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {

            @Override
            public void onRequestFailure(SpiceException e) {

                Session session = Session.getActiveSession();

                if (session != null && session.isOpened()) {
                    session.closeAndClearTokenInformation();
                }

                Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                onError();

                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }
            }

            @Override
            public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {

                android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");

                if (pd != null) {
                    pd.dismissAllowingStateLoss();
                }


                if ("OK".equals(checkUserCredentialsJson.getStatus())) {
                    SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit();

                    if (!Data.isNull(checkUserCredentialsJson.getQueue())) {
                        //hasQueue = true;
                        preferences.putString("queueName", checkUserCredentialsJson.getQueue());
                    }
                    if (!Data.isNull(checkUserCredentialsJson.getAvatar())) {
                        preferences.putString("useravatar", checkUserCredentialsJson.getAvatar());
                    }

                    if (!Data.isNull(checkUserCredentialsJson.getRepo())) {
                        preferences.putString("userRepo", checkUserCredentialsJson.getRepo());
                    }

                    if (!Data.isNull(checkUserCredentialsJson.getUsername())) {
                        preferences.putString("username", checkUserCredentialsJson.getUsername());
                    }


                    if(checkUserCredentialsJson.getSettings() != null ){
                        boolean timeline = checkUserCredentialsJson.getSettings().getTimeline().equals("active");
                        preferences.putBoolean(Preferences.TIMELINE_ACEPTED_BOOL, timeline);
                    }


                    preferences.putString(Configs.LOGIN_USER_LOGIN, username);

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
                    data.putString(AccountManager.KEY_PASSWORD, passwordOrToken);

                    final Intent res = new Intent();
                    res.putExtras(data);
                    finishLogin(res);
                } else {
                    final HashMap<String, Integer> errorsMapConversion = Errors.getErrorsMap();
                    Integer stringId;
                    String message;
                    for (cm.aptoide.ptdev.model.Error error : checkUserCredentialsJson.getErrors()) {
                        stringId = errorsMapConversion.get( error.getCode() );
                        if(stringId != null) {
                            message = getString( stringId );
                        } else {
                            message = error.getMsg();
                        }
                        onError();

                        Toast.makeText(Aptoide.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void onError() {
        callback = (Callback) getParentFragment();
        callback.loginError();
    }


    private void finishLogin(Intent intent) {

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(AccountManager.KEY_PASSWORD);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        String accountType = Aptoide.getConfiguration().getAccountType();
        String authTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
        AccountManager.get(getActivity()).addAccount(accountType, authTokenType, new String[]{"timelineLogin"}, intent.getExtras(), getActivity(), new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {

            }
        }, new Handler(Looper.getMainLooper()));





        /*
        ContentResolver wiResolver = getContentResolver();
        wiResolver.setIsSyncable(account, STUB_PROVIDER_AUTHORITY, 1);
        wiResolver.setSyncAutomatically(account, STUB_PROVIDER_AUTHORITY, true);

        if(Build.VERSION.SDK_INT >= 8) {
            wiResolver.addPeriodicSync(account, STUB_PROVIDER_AUTHORITY, new Bundle(), WEB_INSTALL_POLL_FREQUENCY);
        }
        */


        getActivity().startService(new Intent(getActivity(), RabbitMqService.class));
        ContentResolver.setSyncAutomatically(account, Aptoide.getConfiguration().getUpdatesSyncAdapterAuthority(), true);
        if(Build.VERSION.SDK_INT >= 8) ContentResolver.addPeriodicSync(account, Aptoide.getConfiguration().getUpdatesSyncAdapterAuthority(), new Bundle(), 43200);
        ContentResolver.setSyncAutomatically(account, Aptoide.getConfiguration(). getAutoUpdatesSyncAdapterAuthority(), true);
        callback = (Callback) getParentFragment();
        if(callback!=null) callback.loginEnded();

    }

    public interface Callback{
        public void loginEnded();
        public void loginError();
    }
}
