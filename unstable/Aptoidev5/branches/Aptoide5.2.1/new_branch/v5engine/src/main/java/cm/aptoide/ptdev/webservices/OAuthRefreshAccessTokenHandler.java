package cm.aptoide.ptdev.webservices;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;

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
    private String refreshToken;
    AccountManager accountManager;
    int retries = 1;


    public OAuthRefreshAccessTokenHandler(HashMap<String, String> currentParameters, HttpRequestFactory httpRequestFactory) {
        this.currentParameters = currentParameters;
        this.httpRequestFactory = httpRequestFactory;
        accountManager = AccountManager.get(Aptoide.getContext());

    }

    @Override
    public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {
        if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED && retries > 0) {

            Account account = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
            try {
                this.refreshToken = accountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }

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


            SecurePreferences preferences = SecurePreferences.getInstance();

            preferences.edit().putString("access_token", responseJson.getAccess_token()).commit();

            currentParameters.put("access_token", responseJson.getAccess_token());

            return true;
        } else {
            return false;
        }
    }
}
