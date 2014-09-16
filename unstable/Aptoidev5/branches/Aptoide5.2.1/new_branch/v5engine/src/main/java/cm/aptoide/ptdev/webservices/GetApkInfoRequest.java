package cm.aptoide.ptdev.webservices;

import android.content.Context;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by asantos on 07-08-2014.
 */
public abstract class  GetApkInfoRequest extends GoogleHttpClientSpiceRequest<GetApkInfoJson> {

    protected String repoName;
    protected String packageName;
    protected String versionName;
    protected String token;
    protected Context context;
    public GetApkInfoRequest(Context context) {
        super(GetApkInfoJson.class);
        this.context = context;
    }

    protected abstract ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options);
    protected abstract HashMap<String, String > getParameters();
    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception{
        ArrayList<WebserviceOptions> options = getoptions();
        token = SecurePreferences.getInstance().getString("access_token", null);
        fillWithExtraOptions(options);
        HashMap<String, String > parameters = getParameters();
        parameters.put("options", buildOptions(options));
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);
        HttpRequest request = getHttpRequestFactory().buildPostRequest(
                new GenericUrl( WebserviceOptions.WebServicesLink+"3/getApkInfo"),
                content);
        if (token!=null) {
            parameters.put("access_token", token);
            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
        }
        request.setParser(new JacksonFactory().createJsonObjectParser());
        request.setReadTimeout(5000);
        HttpResponse response;
        try{
            response = request.execute();
        } catch (EOFException e){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Connection", "close");
            request.setHeaders(httpHeaders);
            response = request.execute();
        }
        return response.parseAs(getResultType());
    };

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    protected ArrayList<WebserviceOptions> getoptions(){
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("cmtlimit", "5"));
        options.add(new WebserviceOptions("payinfo", "true"));
        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));
        return options;
    }

    protected String buildOptions(ArrayList<WebserviceOptions> options){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");
        return sb.toString();
    }
}
