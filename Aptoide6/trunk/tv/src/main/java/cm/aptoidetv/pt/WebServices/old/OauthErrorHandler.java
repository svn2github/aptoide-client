package cm.aptoidetv.pt.WebServices.old;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.LoginActivity;
import cm.aptoidetv.pt.SecurePrefs.SecurePreferences;
import cm.aptoidetv.pt.WebServices.old.json.OAuth;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
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
                throw error;
            case HTTP:

                if(error.getResponse().getStatus() == 401) {
                    AccountManager accountManager = AccountManager.get(AppTV.getContext());
                    String refreshToken = "";
                    try {
                        Account account = accountManager.getAccountsByType(AppTV.getConfiguration().getAccountType())[0];
                        refreshToken = accountManager.blockingGetAuthToken(account, LoginActivity.AUTHTOKEN_TYPE_FULL_ACCESS, false);
                    } catch (OperationCanceledException | IOException | ArrayIndexOutOfBoundsException | AuthenticatorException e) {
                        e.printStackTrace();
                    }

                    HashMap<String, String> parameters = new HashMap<String, String>();
                    parameters.put("grant_type", "refresh_token");
                    parameters.put("client_id", "Aptoide");
                    parameters.put("refresh_token", refreshToken);

                    OAuth oAuth = new RestAdapter.Builder().setConverter(createConverter()).setEndpoint("http://webservices.aptoide.com/webservices").build().create(OauthService.class).authenticate(parameters);


                    SharedPreferences preferences = SecurePreferences.getInstance();
                    preferences.edit().putString("access_token", oAuth.getAccess_token()).apply();

                }

                break;
        }

        throw error;
    }


    public static Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return new JacksonConverter(objectMapper);
    }
}
