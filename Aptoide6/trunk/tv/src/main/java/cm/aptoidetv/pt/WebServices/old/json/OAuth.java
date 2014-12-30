package cm.aptoidetv.pt.WebServices.old.json;


import java.util.List;
import cm.aptoidetv.pt.Model.Error;

/**
 * Created by rmateus on 01-07-2014.
 */
public class OAuth {



    public String access_token;

    public String refresh_token;


    public String error_description;


    public List<Error> errors;


    public String status;

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
