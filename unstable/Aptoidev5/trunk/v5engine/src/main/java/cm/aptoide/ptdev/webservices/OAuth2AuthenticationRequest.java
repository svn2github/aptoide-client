package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.webservices.exceptions.InvalidGrantException;
import cm.aptoide.ptdev.webservices.exceptions.InvalidGrantSpiceException;
import cm.aptoide.ptdev.webservices.json.OAuth;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;



import java.util.HashMap;

/**
 * Created by rmateus on 03-07-2014.
 */
public class OAuth2AuthenticationRequest extends GoogleHttpClientSpiceRequest<OAuth> {

    private String username;
    private String password;
    private LoginActivity.Mode mode;
    private String nameForGoogle;

    public OAuth2AuthenticationRequest(){
        super(OAuth.class);
    }



    @Override
    public OAuth loadDataFromNetwork() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("grant_type", "password");
        parameters.put("client_id", "Aptoide");
        parameters.put("mode", "json");

        switch (mode){
            case APTOIDE:
                parameters.put("username", username);
                parameters.put("password", password);
                break;
            case GOOGLE:
                parameters.put("authMode", "google");
                parameters.put("oauthUserName", nameForGoogle);
                parameters.put("oauthToken", password);
                break;
            case FACEBOOK:
                parameters.put("authMode", "facebook");
                parameters.put("oauthToken", password);
                break;
            case INNCLOUD:
                parameters.put("authMode", "inncloud");
                parameters.put("oauthToken", password);
                break;
        }

        if(Aptoide.getConfiguration().getExtraId().length()>0){
            parameters.put("oem_id", Aptoide.getConfiguration().getExtraId());
        }

        HttpContent content = new UrlEncodedContent(parameters);
        GenericUrl url = new GenericUrl("https://webservices.aptoide.com/webservices/3/oauth2Authentication");
        HttpRequest oauth2RefresRequest = getHttpRequestFactory().buildPostRequest(url, content);
        oauth2RefresRequest.setParser(new JacksonFactory().createJsonObjectParser());


        oauth2RefresRequest.setUnsuccessfulResponseHandler(new OAuthAccessTokenHandler());

        HttpResponse response;

        try{
            response = oauth2RefresRequest.execute();
        }catch (InvalidGrantException e){
                cancel();
                throw new InvalidGrantSpiceException(e.getError_description());
        }


        return response.parseAs(OAuth.class);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMode(LoginActivity.Mode mode) {
        this.mode = mode;
    }

    public void setNameForGoogle(String nameForGoogle) {
        this.nameForGoogle = nameForGoogle;
    }
}
