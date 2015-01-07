package cm.aptoidetv.pt;


import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.HashMap;
import java.util.Locale;

import cm.aptoidetv.pt.Dialogs.ProgressDialogFragment;
import cm.aptoidetv.pt.Model.Error;
import cm.aptoidetv.pt.SecurePrefs.SecurePreferences;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.old.AptoideUtils;
import cm.aptoidetv.pt.WebServices.old.CheckUserCredentialsRequest;
import cm.aptoidetv.pt.WebServices.old.CreateUserRequest;
import cm.aptoidetv.pt.WebServices.old.Errors;
import cm.aptoidetv.pt.WebServices.old.json.CheckUserCredentialsJson;
import cm.aptoidetv.pt.WebServices.old.json.OAuth;

/**
 * Created by rmateus on 30-12-2013.
 */
public class SignUpActivity extends ActionBarActivity{
/*  private String TAG = "SignUp";
    private String mAccountType;*/
    private SpiceManager spiceManager = new SpiceManager(HttpService.class);
    private EditText passBox;
    private EditText emailBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_AppCompat);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_create_user);


        findViewById(R.id.submitCreateUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.register));
        emailBox = (EditText) findViewById(R.id.email_box);
        passBox = (EditText) findViewById(R.id.password_box);
        passBox.setTransformationMethod(new PasswordTransformationMethod());
//        passBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//
//                if (hasFocus) {
//                    showPassword = false;
//                    passBox.setTransformationMethod(null);
//                    passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordRes, null);
//                } else {
//                    showPassword = true;
//                    passBox.setTransformationMethod(new PasswordTransformationMethod());
//                    passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordRes, null);
//                }
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
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
//        createUserRequest.setName(nameBox.getAvatar().toString());
        createUserRequest.setPass(passBox.getText().toString());

        new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(createUserRequest, new RequestListener<OAuth>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                Toast.makeText(AppTV.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
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
                            SharedPreferences.Editor editor =PreferenceManager
                                    .getDefaultSharedPreferences(SignUpActivity.this)
                                    .edit();
                            if (null != checkUserCredentialsJson.getQueue()) {
                                //hasQueue = true;
                                editor.putString("queueName", checkUserCredentialsJson.getQueue());
                            }
                            if (null != (checkUserCredentialsJson.getAvatar())) {
                                editor.putString("useravatar", checkUserCredentialsJson.getAvatar());
                            }

                            if (null != (checkUserCredentialsJson.getRepo())) {
                                editor.putString("userRepo", checkUserCredentialsJson.getRepo());
                            }

                            if (null != checkUserCredentialsJson.getUsername()) {
                                editor.putString("username", checkUserCredentialsJson.getUsername());
                            }

                            editor.putString(LoginActivity.LOGIN_USER_LOGIN, emailBox.getText().toString());
                            editor.putString("loginType", LoginActivity.Mode.APTOIDE.name());
                            editor.commit();

                            SharedPreferences preferences = SecurePreferences.getInstance();
                            preferences.edit().putString("access_token", oAuth.getAccess_token()).commit();
                            preferences.edit().putString("devtoken",checkUserCredentialsJson.getToken()).commit();

                            Bundle data = new Bundle();
                            data.putString(AccountManager.KEY_ACCOUNT_NAME, emailBox.getText().toString());
                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, AppTV.getConfiguration().getAccountType());
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
                            for (Error error : checkUserCredentialsJson.getErrors()) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home || i == R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
