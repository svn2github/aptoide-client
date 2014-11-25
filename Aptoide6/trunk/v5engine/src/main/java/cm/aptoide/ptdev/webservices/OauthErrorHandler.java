package cm.aptoide.ptdev.webservices;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;


import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.OAuth;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by rmateus on 24-11-2014.
 */
public class OauthErrorHandler {

    public interface OauthService{
        @POST("/3/oauth2Authentication")
        @FormUrlEncoded
        OAuth authenticate(@FieldMap HashMap<String, String> args);
    }

    public static void handle(RetrofitError error) {

        switch (error.getKind()){

            case NETWORK:
            case CONVERSION:
            case UNEXPECTED:
            case HTTP:


                if(error.getResponse().getStatus() == 401) {
                    AccountManager accountManager = AccountManager.get(Aptoide.getContext());

                    Account account = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
                    String refreshToken = "";
                    try {
                        refreshToken = accountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }

                    HashMap<String, String> parameters = new HashMap<String, String>();
                    parameters.put("grant_type", "refresh_token");
                    parameters.put("client_id", "Aptoide");
                    parameters.put("refresh_token", refreshToken);

                    OAuth oAuth = new RestAdapter.Builder().setEndpoint("http://webservices.aptoide.com/webservices").build().create(OauthService.class).authenticate(parameters);

                    SharedPreferences preferences = SecurePreferences.getInstance();
                    preferences.edit().putString("access_token", oAuth.getAccess_token()).commit();

                }

                break;
        }

        throw error;
    }
}
