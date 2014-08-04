package cm.aptoide.ptdev;


import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Configs;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.CreateUserRequest;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import cm.aptoide.ptdev.webservices.json.OAuth;

import com.flurry.android.FlurryAgent;
import com.google.api.client.util.Data;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by rmateus on 30-12-2013.
 */
public class SignUpActivity extends ActionBarActivity{
/*  private String TAG = "SignUp";
    private String mAccountType;*/
    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private boolean showPassword = true;
    private EditText passBox;
    private EditText emailBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_create_user);


        findViewById(R.id.submitCreateUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("My_Account_Clicked_On_Sign_Up_Button");
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.register));
        emailBox = (EditText) findViewById(R.id.email_box);
        passBox = (EditText) findViewById(R.id.password_box);
        passBox.setTransformationMethod(new PasswordTransformationMethod());

        final Drawable hidePasswordRes = getResources().getDrawable(R.drawable.ic_show_password);
        final Drawable showPasswordRes = getResources().getDrawable(R.drawable.ic_hide_password);
        passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
        passBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (passBox.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_UP){
                    return false;
                }
                if (event.getX() > passBox.getWidth() - passBox.getPaddingRight() - hidePasswordRes.getIntrinsicWidth()) {
                    if(showPassword){
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("My_Account_Clicked_On_Show_Password");
                        showPassword=false;
                        passBox.setTransformationMethod(null);
                        passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordRes, null);
                    }else{
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("My_Account_Clicked_On_Hide_Password");
                        showPassword=true;
                        passBox.setTransformationMethod(new PasswordTransformationMethod());
                        passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
                    }
                }




                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }

    public final static String PARAM_USER_PASS = "USER_PASS";


    private void createAccount() {

        // Validation!

        if(emailBox.getText().toString().length()==0 || passBox.getText().toString().length()==0 ){
            Toast.makeText(getApplicationContext(), R.string.fields_cannot_empty, Toast.LENGTH_LONG).show();
            return;
        }
        String pass = passBox.getText().toString();
        if( pass.length() < 8 || !hasValidChars(pass)){
            Toast.makeText(getApplicationContext(), R.string.password_validation_text, Toast.LENGTH_LONG).show();
            return;
        }

        CreateUserRequest createUserRequest = new CreateUserRequest();


        createUserRequest.setEmail(emailBox.getText().toString());
//        createUserRequest.setName(nameBox.getText().toString());
        createUserRequest.setPass(passBox.getText().toString());

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(createUserRequest, new RequestListener<OAuth>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null) pd.dismiss();
            }

            @Override
            public void onRequestSuccess(final OAuth oAuth) {


                CheckUserCredentialsRequest request = new CheckUserCredentialsRequest();


                String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                request.setRegisterDevice(true);
                request.setSdk(String.valueOf(AptoideUtils.HWSpecifications.getSdkVer()));
                request.setDeviceId(deviceId);
                request.setCpu(AptoideUtils.HWSpecifications.getCpuAbi() + "," + AptoideUtils.HWSpecifications.getCpuAbi2());
                request.setDensity(String.valueOf(AptoideUtils.HWSpecifications.getNumericScreenSize(SignUpActivity.this)));
                request.setOpenGl(String.valueOf(AptoideUtils.HWSpecifications.getGlEsVer(SignUpActivity.this)));
                request.setModel(Build.MODEL);
                request.setScreenSize(Filters.Screen.values()[AptoideUtils.HWSpecifications.getScreenSize(SignUpActivity.this)].name().toLowerCase(Locale.ENGLISH));

                request.setToken(oAuth.getAccess_token());

                spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {

                    @Override
                    public void onRequestFailure(SpiceException e) {

                        Toast.makeText(getBaseContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();

                        android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                        if (pd != null) {
                            pd.dismissAllowingStateLoss();
                        }
                    }

                    @Override
                    public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {

                        android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                        if (pd != null) {
                            pd.dismissAllowingStateLoss();
                        }


                        if ("OK".equals(checkUserCredentialsJson.getStatus())) {
                            SharedPreferences.Editor editor =PreferenceManager
                                    .getDefaultSharedPreferences(SignUpActivity.this)
                                    .edit();
                            if (!Data.isNull(checkUserCredentialsJson.getQueue())) {
                                //hasQueue = true;
                                editor.putString("queueName", checkUserCredentialsJson.getQueue());
                            }
                            if (!Data.isNull(checkUserCredentialsJson.getAvatar())) {
                                editor.putString("useravatar", checkUserCredentialsJson.getAvatar());
                            }

                            if (!Data.isNull(checkUserCredentialsJson.getRepo())) {
                                editor.putString("userRepo", checkUserCredentialsJson.getRepo());
                            }

                            if (!Data.isNull(checkUserCredentialsJson.getUsername())) {
                                editor.putString("username", checkUserCredentialsJson.getUsername());
                            }

                            editor.putString(Configs.LOGIN_USER_LOGIN, emailBox.getText().toString());
                            editor.putString("loginType", LoginActivity.Mode.APTOIDE.name());
                            editor.commit();

                            SecurePreferences preferences = SecurePreferences.getInstance();
                            preferences.edit().putString("access_token", oAuth.getAccess_token()).commit();
                            preferences.edit().putString("devtoken",checkUserCredentialsJson.getToken()).commit();


                            Bundle data = new Bundle();
                            data.putString(AccountManager.KEY_ACCOUNT_NAME, emailBox.getText().toString());
                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, Aptoide.getConfiguration().getAccountType());
                            data.putString(AccountManager.KEY_AUTHTOKEN, oAuth.getRefreshToken());
                            data.putString(PARAM_USER_PASS, passBox.getText().toString());


                            final Intent res = new Intent();
                            res.putExtras(data);
                            setResult(RESULT_OK, res);
                            finish();
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
                                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
            }
        });





//        CheckUserCredentialsRequest request = new CheckUserCredentialsRequest();
//
//        request.setUser(userName);
//        try {
//            request.setPassword(AptoideUtils.Algorithms.computeSHA1sum(userPass));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//
//        spiceManager.execute(request, new RequestListener<CheckUserCredentialsJson>() {
//            @Override
//            public void onRequestFailure(SpiceException e) {
//                Toast.makeText(getBaseContext(), "An error ocurred.", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {
//                Bundle data = new Bundle();
//                data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
//                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
//                data.putString(AccountManager.KEY_AUTHTOKEN, checkUserCredentialsJson.getToken());
//                data.putString(PARAM_USER_PASS, userPass);
//                final Intent res = new Intent();
//                res.putExtras(data);
//                finishLogin(res);
//
//
//            }
//        });
//
//
//        new AsyncTask<String, Void, Intent>() {
//
//            String name = ((TextView) findViewById(R.id.name)).getText().toString().trim();
//            String accountName = ((TextView) findViewById(R.id.accountName)).getText().toString().trim();
//            String accountPassword = ((TextView) findViewById(R.id.accountPassword)).getText().toString().trim();
//
//            @Override
//            protected Intent doInBackground(String... params) {
//
//                Log.d("udinic", TAG + "> Started authenticating");
//
//                String authtoken = null;
//                Bundle data = new Bundle();
//                try {
//                    authtoken = sServerAuthenticate.userSignUp(name, accountName, accountPassword, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
//
//                    data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
//                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
//                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
//                    data.putString(PARAM_USER_PASS, accountPassword);
//                } catch (Exception e) {
//                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
//                }
//
//                final Intent res = new Intent();
//                res.putExtras(data);
//                return res;
//            }
//
//            @Override
//            protected void onPostExecute(Intent intent) {
//                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
//                    Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
//                } else {
//                    setResult(RESULT_OK, intent);
//                    finish();
//                }
//            }
//        }.execute();
    }

    private boolean hasValidChars(String pass) {

        return has1number1letter(pass);

    }

    private boolean has1number1letter(String pass) {
        boolean hasLetter = false;
        boolean hasNumber = false;

        for(char c : pass.toCharArray()){
            if(!hasLetter && Character.isLetter(c)){
                if(hasNumber)
                    return true;
                hasLetter = true;
            }else if(!hasNumber && Character.isDigit(c)){
                if(hasLetter)
                    return true;
                hasNumber = true;
            }
        }
        if (pass.contains("!") || pass.contains("@") || pass.contains("#") || pass.contains("$") || pass.contains("#") || pass.contains("*")) {
            hasNumber = true;
        }

        return hasNumber&&hasLetter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home || i == R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        } else if( i == R.id.menu_SendFeedBack){
            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
