package com.aptoide.openiab.webservices;

import android.util.Log;
import com.aptoide.openiab.webservices.json.PayProductJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

/**
 * Created by j-pac on 21-02-2014.
 */
public class PayProductRequest extends GoogleHttpClientSpiceRequest<PayProductJson> {

    private String apiVersion;
    private String token;
    private String productId;

    private String oemId;
    private String repo;

    public PayProductRequest() {
        super(PayProductJson.class);
    }

    @Override
    public PayProductJson loadDataFromNetwork() throws Exception {
        String options = "options=(";
        if(oemId != null) {
            options += "oemid=" + oemId + ";";
        }
        if(repo != null) {
            options += "repo=" + repo;
        }
        options += ")";

        String baseUrl = "https://webservices.aptoide.com/webservices/payProduct/requesttype/paymenttype/"+apiVersion+"/"+token+"/"+productId+"/"+options;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOemId() {
        return oemId;
    }

    public void setOemId(String oemId) {
        this.oemId = oemId;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }
}
