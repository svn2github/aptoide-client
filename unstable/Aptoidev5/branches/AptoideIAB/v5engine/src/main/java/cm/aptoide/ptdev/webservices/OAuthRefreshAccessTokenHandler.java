package cm.aptoide.ptdev.webservices;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.text.TextUtils;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.OAuth;
import com.google.api.client.http.*;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by rmateus on 02-07-2014.
 */
public class OAuthRefreshAccessTokenHandler implements HttpUnsuccessfulResponseHandler {


    private final HashMap<String, String> currentParameters;
    private final HttpRequestFactory httpRequestFactory;
    private final String refreshToken;
    AccountManager accountManager;
    int retries = 1;


    public OAuthRefreshAccessTokenHandler(HashMap<String, String> currentParameters, HttpRequestFactory httpRequestFactory) {
        this.currentParameters = currentParameters;
        this.httpRequestFactory = httpRequestFactory;
        accountManager = AccountManager.get(Aptoide.getContext());
        SecurePreferences preferences = new SecurePreferences(Aptoide.getContext());
        this.refreshToken = preferences.getString("refreshToken", "");
    }

    @Override
    public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {
        if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED && retries > 0) {

            retries--;
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("grant_type", "refresh_token");
            parameters.put("client_id", "Aptoide");
            parameters.put("refresh_token", refreshToken);
            HttpContent content = new UrlEncodedContent(parameters);
            GenericUrl url = new GenericUrl("https://webservices.aptoide.com/webservices/3/oauth2Authentication");
            HttpRequest oauth2RefresRequest = httpRequestFactory.buildPostRequest(url, content);
            oauth2RefresRequest.setParser(new JacksonFactory().createJsonObjectParser());
            OAuth responseJson = oauth2RefresRequest.execute().parseAs(OAuth.class);

            Account account = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

            try {

                String currentToken = accountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
                accountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, currentToken);
                accountManager.setAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, responseJson.getAccess_token());

                currentParameters.put("access_token", responseJson.getAccess_token());

            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }


            return true;
        } else {
            return false;
        }
    }
}
