package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.IabSkuDetailsJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by j-pac on 19-02-2014.
 */
public class IabSkuDetailsRequest extends GoogleHttpClientSpiceRequest<IabSkuDetailsJson> {

    private String apiVersion;
    private String token;
    private String packageName;
    private List<String> skuList = new ArrayList<String>();

    public IabSkuDetailsRequest() {
        super(IabSkuDetailsJson.class);
    }

    @Override
    public IabSkuDetailsJson loadDataFromNetwork() throws Exception {

        StringBuilder skus = new StringBuilder();
        for(String sku : skuList){
            skus.append(sku);
            skus.append(";");
        }

        String baseUrl = "http://webservices.aptoide.com/webservices/processInAppBilling/iabskudetails/"+apiVersion+"/options=(package="+packageName+";skulist=("+skus+");token="+token+")";

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

    public List<String> getSkuList() {
        return skuList;
    }

    public void addToSkuList(String sku) {
        skuList.add(sku);
    }
}