package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoide.ptdev.model.Error;

/**
 * Created by rmateus on 01-07-2014.
 */
public class OAuth {


    @Key
    private String access_token;
    @Key
    private String refresh_token;

    @Key
    private String error_description;

    @Key
    private List<Error> errors;

    @Key
    private String status;

    public String getStatus() {
        return status;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    public List<Error> getError() {
        return errors;
    }



    public String getError_description() {
        return error_description;
    }
}
