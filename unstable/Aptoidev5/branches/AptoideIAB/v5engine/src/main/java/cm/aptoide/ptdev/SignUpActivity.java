package cm.aptoide.ptdev;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.CreateUserRequest;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by rmateus on 30-12-2013.
 */
public class SignUpActivity extends ActionBarActivity{

    private String TAG = "SignUp";
    private String mAccountType;
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
                        showPassword=false;
                        passBox.setTransformationMethod(null);
                        passBox.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordRes, null);
                    }else{
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }

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
        spiceManager.execute(createUserRequest, new RequestListener<CreateUserJson>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null) pd.dismiss();
            }

            @Override
            public void onRequestSuccess(CreateUserJson createUserJson) {
                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null) pd.dismiss();
                if("OK".equals(createUserJson.getStatus())){
                    Intent data = new Intent();
                    data.putExtra("password", passBox.getText().toString());
                    data.putExtra("username", emailBox.getText().toString());
                    setResult(RESULT_OK, data);
                    finish();
                }else{
                    AptoideUtils.toastError(createUserJson.getErrors());
                }
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

        if (pass.contains("!") || pass.contains("@") || pass.contains("#") || pass.contains("$") || pass.contains("#") || pass.contains("*")) {

            hasNumber = true;

        }

        for(char c : pass.toCharArray()){
            if(Character.isLetter(c) && !hasLetter){
                hasLetter = true;
            }else if(Character.isDigit(c)&& !hasNumber){
                hasNumber = true;
            }
        }

        return hasNumber&&hasLetter;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (i == R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
