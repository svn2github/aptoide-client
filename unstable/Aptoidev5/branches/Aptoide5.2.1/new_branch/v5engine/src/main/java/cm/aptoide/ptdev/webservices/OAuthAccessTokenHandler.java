package cm.aptoide.ptdev.webservices;

import android.accounts.AccountManager;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;

import java.io.IOException;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.exceptions.InvalidGrantException;
import cm.aptoide.ptdev.webservices.json.OAuth;

/**
 * Created by rmateus on 02-07-2014.
 */
public class OAuthAccessTokenHandler implements HttpUnsuccessfulResponseHandler {



    int retries = 1;


    public OAuthAccessTokenHandler() {

    }

    @Override
    public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {
        if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED && retries > 0) {
            retries--;


                OAuth responseJson = response.parseAs(OAuth.class);

                if(responseJson.getError().equals("invalid_grant")){
                    throw new InvalidGrantException(responseJson.getError_description());
                }


            return true;
        } else {
            return false;
        }
    }
}
