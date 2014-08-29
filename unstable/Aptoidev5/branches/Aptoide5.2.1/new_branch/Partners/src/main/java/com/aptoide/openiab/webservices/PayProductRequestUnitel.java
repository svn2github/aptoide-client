package com.aptoide.openiab.webservices;

import android.text.TextUtils;
import android.util.Log;

import com.aptoide.openiab.webservices.json.IabPurchaseStatusJson;
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

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;

/**
 * Created by j-pac on 21-02-2014.
 */
public class PayProductRequestUnitel extends GoogleHttpClientSpiceRequest<IabPurchaseStatusJson> {

    private String apiVersion;
    private String token;
    private String productId;

    private String oemId;
    private String repo;
    private String payType;
    private String imsi;
    private String developerPayload;
    private String price;
    private String currency;


    public PayProductRequestUnitel() {
        super(IabPurchaseStatusJson.class);
    }

    @Override
    public IabPurchaseStatusJson loadDataFromNetwork() throws Exception {

        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();

        options.add(new WebserviceOptions("imsi", imsi));

        options.add(new WebserviceOptions("oemid", oemId));
        options.add(new WebserviceOptions("developerPayload", developerPayload));


        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");
        //String baseUrl = "http://webservices.aptoide.com/webservices/payProduct/3/"+token+"/"+productId+"/billing/"+ payType +"/options="+sb.toString()+"/json";
        String baseUrl = "https://webservices.aptoide.com/webservices/3/payProduct";
        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);



        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("mode","json");
        parameters.put("developerPayload",developerPayload);

        parameters.put("productid",productId);
        parameters.put("apiversion",apiVersion);
        parameters.put("reqType","billing");
        parameters.put("payType",payType);
        parameters.put("repo",repo);

        parameters.put("price",price);
        parameters.put("currency",currency);

        parameters.put("imsi",imsi);
        parameters.put("oemid",oemId);

        token = SecurePreferences.getInstance().getString("access_token", null);


        parameters.put("access_token", token);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setParser(new JacksonFactory().createJsonObjectParser());
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

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


    public void setPayType(String id) {
        this.payType = id;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }



    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public class WebserviceOptions {
        String key;
        String value;


        private WebserviceOptions(String key,String value) {
            this.value = value;
            this.key = key;
        }

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. Subclasses are encouraged to override this method and provide an
         * implementation that takes into account the object's type and data. The
         * default implementation is equivalent to the following expression:
         * <pre>
         *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
         * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
         * {@code toString} method</a>
         * if you intend implementing your own {@code toString} method.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            return key+"="+value;    //To change body of overridden methods use File | Settings | File Templates.
        }

    }
}
