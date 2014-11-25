package cm.aptoide.ptdev.webservices;

import android.accounts.AccountManager;
import android.util.Log;







import java.io.IOException;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.exceptions.InvalidGrantException;
import cm.aptoide.ptdev.webservices.json.OAuth;

/**
 * Created by rmateus on 02-07-2014.
 */
//public class OAuthAccessTokenHandler implements HttpUnsuccessfulResponseHandler {
//
//
//
//    int retries = 1;
//
//
//    public OAuthAccessTokenHandler() {
//
//    }
//
//    @Override
//    public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {
//        Log.d("AptoideCenas", "HandleResponse");
//
//        if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED && retries > 0) {
//            retries--;
//
//                OAuth responseJson = response.parseAs(OAuth.class);
//
//                if(responseJson.getError().equals("invalid_grant")){
//                    throw new InvalidGrantException(responseJson.getError_description());
//                }
//
//
//            return true;
//        } else {
//            return false;
//        }
//    }
//}
