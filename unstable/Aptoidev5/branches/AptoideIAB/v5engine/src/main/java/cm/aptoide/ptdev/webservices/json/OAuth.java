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

    public String getAccess_token() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }
}
