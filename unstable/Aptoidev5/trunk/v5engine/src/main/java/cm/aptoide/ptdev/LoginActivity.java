package cm.aptoide.ptdev;

import android.accounts.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.CheckUserCredentialsRequest;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;


import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by brutus on 09-12-2013.
 */
public class LoginActivity extends ActionBarActivity {

    private Login login = new Login();

    private AccountManager accountManager;
    private AccountAuthenticatorResponse authenticatorResponse;

    private EditText usernameField;
    private EditText passwordField;
    private CheckBox showPasswordCheck;

    private TextView loggedUsername;

    SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);

    @Override
    protected void onCreate(Bundle icicle) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(icicle);


        accountManager = AccountManager.get(this);

        Log.d("LoginActivity-before", "length: " + accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length);

        if (accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length == 0) {
            setContentView(R.layout.form_login);
            Log.d("LoginActivity-login", "length: " + accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length);

            usernameField = (EditText) findViewById(R.id.username);
            passwordField = (EditText) findViewById(R.id.password);
            showPasswordCheck = (CheckBox) findViewById(R.id.show_login_passwd);
            passwordField = (EditText) findViewById(R.id.password);
            showPasswordCheck = (CheckBox) findViewById(R.id.show_login_passwd);
            showPasswordCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        passwordField.setTransformationMethod(null);
                    } else {
                        passwordField.setTransformationMethod(new PasswordTransformationMethod());
                    }
                }
            });
            showPasswordCheck.setEnabled(true);

            findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        login(usernameField.getText().toString().trim(), AptoideUtils.Algorithms.computeSHA1sum(passwordField.getText().toString().trim()));

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });

            Bundle extras = getIntent().getExtras();

            if (extras != null && extras.containsKey(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)) {
                authenticatorResponse = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
            }
        } else {
            setContentView(R.layout.form_logout);
            Log.d("LoginActivity-logout", "length: " + accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length);

            loggedUsername = (TextView) findViewById(R.id.username);
            loggedUsername.setText(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0].name);

            findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }


        getSupportActionBar().setTitle(getString(R.string.login));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    public void login(final String username, final String password) {

        if (username.length() > 0 && password.length() > 0) {

            android.support.v4.app.DialogFragment pd = AptoideDialog.pleaseWaitDialog();
            pd.setCancelable(false);
            pd.show(getSupportFragmentManager(), "pleaseWaitDialogRemove");

            CheckUserCredentialsRequest checkUserCredentialsRequest = new CheckUserCredentialsRequest().setUser(username).setPassword(password);


            if (false) { //isCheckBox checked)
                checkUserCredentialsRequest.setDeviceId(AptoideUtils.HWSpecifications.getDeviceId(this))
                        .setModel(AptoideUtils.HWSpecifications.TERMINAL_INFO)
                        .setSdk(Integer.toString(AptoideUtils.HWSpecifications.getSdkVer()))
                        .setDensity(Integer.toString(AptoideUtils.HWSpecifications.getDensityDpi(this)))
                        .setCpu(AptoideUtils.HWSpecifications.getCpuAbi() + "," + AptoideUtils.HWSpecifications.getCpuAbi2())
                        .setScreenSize(Integer.toString(AptoideUtils.HWSpecifications.getScreenSize(this)))
                        .setOpenGl(AptoideUtils.HWSpecifications.getGlEsVer(this));
                checkUserCredentialsRequest.setRegisterDevice(true);
            }

            spiceManager.execute(checkUserCredentialsRequest, new RequestListener<CheckUserCredentialsJson>() {
                @Override
                public void onRequestFailure(SpiceException e) {
                    android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialogRemove");
                    pd.dismiss();

                    Toast toast = Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 30);
                    toast.show();

                    if (getIntent().hasExtra("login")) {
                        Intent result = new Intent();
                        setResult(RESULT_CANCELED, result);
                    }
                    finish();
                }

                @Override
                public void onRequestSuccess(CheckUserCredentialsJson checkUserCredentialsJson) {
                    if ("OK".equals(checkUserCredentialsJson.getStatus())) {

                        Account account = new Account(username, AccountGeneral.ACCOUNT_TYPE);
                        boolean accountCreated = accountManager.addAccountExplicitly(account, password, null);

                        if (accountCreated) {
                            accountManager.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE, checkUserCredentialsJson.getToken());
                            accountManager.setPassword(account, password);

                            if (authenticatorResponse != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE);
                                authenticatorResponse.onResult(bundle);
                            }

                            if (getIntent().hasExtra("login")) {
                                Intent result = new Intent();
                                setResult(RESULT_OK, result);
                            }
                            finish();

                        } else {
                            Toast toast = Toast.makeText(LoginActivity.this, "error creating account", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 30);
                            toast.show();
                        }

                    } else {
                        Toast toast = Toast.makeText(LoginActivity.this, checkUserCredentialsJson.getErrors().get(0), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 30);
                        toast.show();
                    }

                    android.support.v4.app.DialogFragment pd = (android.support.v4.app.DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialogRemove");
                    pd.dismiss();
                }
            });


        } else {
            Toast toast = Toast.makeText(this, getString(R.string.check_your_credentials), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 30);
            toast.show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
}



