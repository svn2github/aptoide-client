package com.aptoide.openiab.webservices;

import android.util.Log;

import com.aptoide.openiab.webservices.json.IabAvailableJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

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

        String baseUrl = "http://webservices.aptoide.com/webservices/processInAppBilling/iabavailable/"+apiVersion+"/options=(package="+packageName+";)";
        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);

        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

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
