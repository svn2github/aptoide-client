package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.IabPurchaseStatusJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;


/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchaseStatusRequest extends GoogleHttpClientSpiceRequest<IabPurchaseStatusJson> {

    private int apiVersion;
    private String token;
    private int orderId;

    public IabPurchaseStatusRequest() {
        super(IabPurchaseStatusJson.class);
    }

    @Override
    public IabPurchaseStatusJson loadDataFromNetwork() throws Exception {
        String baseUrl = "http://webservices.aptoide.com/webservices/processInAppBilling/iabpurchasestatus/"+apiVersion+"/options=(orderid="+orderId+";token="+token+")";



        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);
        setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
