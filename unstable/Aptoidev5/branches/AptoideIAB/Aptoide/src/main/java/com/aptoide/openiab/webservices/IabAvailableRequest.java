package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.IabAvailableJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabAvailableRequest extends GoogleHttpClientSpiceRequest<IabAvailableJson> {


    private String apiVersion;
    private String token;
    private String packageName;

    public IabAvailableRequest() {
        super(IabAvailableJson.class);
    }

    @Override
    public IabAvailableJson loadDataFromNetwork() throws Exception {

        //String baseUrl = "http://dev.aptoide.com/webservices/processInAppBilling/iabavailable/"+apiVersion+"/options=(package="+packageName+";)";
        String baseUrl = "https://webservices.aptoide.com/webservices/3/processInAppBilling";
        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabavailable");
        parameters.put("mode","json");
        parameters.put("package",packageName);

        HttpContent content = new UrlEncodedContent(parameters);

        Log.e("Aptoide-InappBillingRequest", baseUrl);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
