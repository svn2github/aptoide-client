package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.IabConsumeJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;


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
        String baseUrl = "http://webservices.aptoide.com/webservices/processInAppBilling/iabconsume/"+apiVersion+"/options=(package="+packageName+";purchasetoken="+purchaseToken+";token="+token+")";
        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);
        setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
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
