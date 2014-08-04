package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

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
    private String error;

    public String getAccess_token() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_description() {
        return error_description;
    }
}
