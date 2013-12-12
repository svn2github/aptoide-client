package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.CheckUserCredentialsJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created by brutus on 09-12-2013.
 */
public class CheckUserCredentialsRequest extends GoogleHttpClientSpiceRequest<CheckUserCredentialsJson> {

    String baseUrl = "http://webservices.aptoide.com/webservices/checkUserCredentials/";
            //"http://www.aptoide.com/webservices/checkUserCredentials/";

    private String user;
    private String password;
    private String repo;

    private boolean registerDevice;

    private String deviceId;
    private String model;
    private String sdk;
    private String density;
    private String cpu;
    private String screenSize;
    private String openGl;

    public CheckUserCredentialsRequest() {
        super(CheckUserCredentialsJson.class);
    }

    @Override
    public CheckUserCredentialsJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("user", user);
        parameters.put("passhash", password);
        if(repo != null) {
            parameters.put("repo", repo);
        }

        if(registerDevice) {
            parameters.put("device_id", deviceId);
            parameters.put("model", model);
            parameters.put("sdk", sdk);
            parameters.put("density", density);
            parameters.put("cpu", cpu);
            parameters.put("screen_size", screenSize);
            parameters.put("open_gl", openGl);
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
}
