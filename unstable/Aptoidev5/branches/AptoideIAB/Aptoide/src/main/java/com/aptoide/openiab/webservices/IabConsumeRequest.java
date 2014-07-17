package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.IabConsumeJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.io.EOFException;
import java.util.HashMap;

import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;


/**
 * Created by j-pac on 20-02-2014.
 */
public class IabConsumeRequest extends GoogleHttpClientSpiceRequest<IabConsumeJson> {

    private String apiVersion;
    private String token;
    private String packageName;
    private String purchaseToken;

    public IabConsumeRequest() {
        super(IabConsumeJson.class);
    }

    @Override
    public IabConsumeJson loadDataFromNetwork() throws Exception {
//        String baseUrl = "http://dev.aptoide.com/webservices/processInAppBilling/iabconsume/"+apiVersion+"/options=(package="+packageName+";purchasetoken="+purchaseToken+";token="+token+")";
        String baseUrl = "http://webservices.aptoide.com/webservices/3/processInAppBilling";

        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabconsume");
        parameters.put("purchasetoken",purchaseToken);
        parameters.put("access_token",token);
        parameters.put("mode","json");
        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url,  content);
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

        request.setParser(new JacksonFactory().createJsonObjectParser());

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

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
