package com.aptoide.openiab.webservices;

import android.util.Log;

import com.aptoide.openiab.webservices.json.IabPurchasesJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchasesRequest extends GoogleHttpClientSpiceRequest<IabPurchasesJson> {


    private String apiVersion;
    private String token;
    private String packageName;
    private String type;

    public IabPurchasesRequest() {
        super(IabPurchasesJson.class);
    }

    @Override
    public IabPurchasesJson loadDataFromNetwork() throws Exception {
        String baseUrl = "http://webservices.aptoide.com/webservices/processInAppBilling/iabpurchases/"+apiVersion+"/options=(package="+packageName+";purchasetype="+type+";token="+token+")";

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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
