package cm.aptoide.ptdev;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import cm.aptoide.ptdev.configuration.AccountGeneral;

/**
 * Created by brutus on 09-12-2013.
 */
public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private Context context;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d("Account-Authenticator", "Adding new account");

        Bundle bundle = new Bundle();

        if(AccountManager.get(context).getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length == 0) {
            Intent i = new Intent(context, LoginActivity.class);
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            i.putExtra(AccountGeneral.AUTHTOKEN_TYPE, authTokenType);
            bundle.putParcelable(AccountManager.KEY_INTENT, i);

        } else {
            bundle.putInt(AccountManager.KEY_ERROR_CODE, 11/*ERROR_CODE_ONE_ACCOUNT_ALLOWED*/);
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, context.getString(R.string.one_account_allowed));

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.one_account_allowed), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        String authToken = AccountManager.get(context).peekAuthToken(account, authTokenType);


        Log.d("Account_Authenticator", "AuthToken = " + authToken);

        Bundle result = new Bundle();

        if(authToken != null) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // When an authToken isn't stored, the CheckUserCredentials class should be called
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountGeneral.ACCOUNT_TYPE, account.type);
        intent.putExtra(AccountGeneral.AUTHTOKEN_TYPE, authTokenType);
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;

    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
