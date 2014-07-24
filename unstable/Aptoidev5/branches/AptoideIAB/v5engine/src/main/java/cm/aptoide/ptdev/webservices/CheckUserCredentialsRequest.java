package cm.aptoide.ptdev.webservices;

import android.os.Build;
import android.util.Log;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import java.util.HashMap;

/**
 * Created by brutus on 09-12-2013.
 */
public class CheckUserCredentialsRequest extends GoogleHttpClientSpiceRequest<CheckUserCredentialsJson> {

    String baseUrl = "http://webservices.aptoide.com/webservices/3/getUserInfo";
    String baseUrlWithoutSsl = "http://webservices.aptoide.com/webservices/3/getUserInfo";

    //"http://www.aptoide.com/webservices/checkUserCredentials/";

    private String user;
    private String password;
    private String repo;
    private String avatar;

    private boolean registerDevice;

    private String deviceId;
    private String model;
    private String sdk;
    private String density;
    private String cpu;
    private String screenSize;
    private String openGl;
    private String nameForGoogle;
    private LoginActivity.Mode mode;
    private String token;

    public CheckUserCredentialsRequest() {
        super(CheckUserCredentialsJson.class);
    }

    @Override
    public CheckUserCredentialsJson loadDataFromNetwork() throws Exception {


        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("access_token", token);


        Log.d("AccessToken", token);


        if(registerDevice) {
            parameters.put("device_id", deviceId);
            parameters.put("model", model);
            parameters.put("maxSdk", sdk);
            parameters.put("myDensity", density);
            parameters.put("myCpu", cpu);
            parameters.put("maxScreen", screenSize);
            parameters.put("maxGles", openGl);
        }

        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

    public String getUser() {
        return user;
    }

    public CheckUserCredentialsRequest setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public CheckUserCredentialsRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setRegisterDevice(boolean registerDevice) {
        this.registerDevice = registerDevice;
    }

    public CheckUserCredentialsRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public CheckUserCredentialsRequest setModel(String model) {
        this.model = model;
        return this;
    }

    public CheckUserCredentialsRequest setSdk(String sdk) {
        this.sdk = sdk;
        return this;
    }

    public CheckUserCredentialsRequest setDensity(String density) {
        this.density = density;
        return this;
    }

    public CheckUserCredentialsRequest setCpu(String cpu) {
        this.cpu = cpu;
        return this;
    }

    public CheckUserCredentialsRequest setScreenSize(String screenSize) {
        this.screenSize = screenSize;
        return this;
    }

    public CheckUserCredentialsRequest setOpenGl(String openGl) {
        this.openGl = openGl;
        return this;
    }

    public void setNameForGoogle(String nameForGoogle) {
        this.nameForGoogle = nameForGoogle;
    }

    public String getNameForGoogle() {
        return nameForGoogle;
    }

    public void setMode(LoginActivity.Mode mode) {
        this.mode = mode;
    }

    public LoginActivity.Mode getMode() {
        return mode;
    }

    public void setAvatar(String avatar) { this.avatar = avatar; }

    public void setToken(String token) {
        this.token = token;
    }
}
