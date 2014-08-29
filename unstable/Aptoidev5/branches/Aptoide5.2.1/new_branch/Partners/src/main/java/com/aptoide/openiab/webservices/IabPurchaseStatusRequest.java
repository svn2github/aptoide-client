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
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchaseStatusRequest extends GoogleHttpClientSpiceRequest<IabPurchaseStatusJson> {

    private int apiVersion;
    private String token;
    private int orderId;
    private boolean rest;

    private int productId;
    private int payType;
    private double taxRate;
    private double price;
    private String currency;
    private String payKey;
    private String developerPayload;
    private String simcc;
    private String repo;

    public IabPurchaseStatusRequest() {
        super(IabPurchaseStatusJson.class);
    }

    @Override
    public IabPurchaseStatusJson loadDataFromNetwork() throws Exception {


        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("token", token));


//        if(!rest){
//            options.add(new WebserviceOptions("orderid", token));
//        }else{
//            if(developerPayload!=null && !developerPayload.isEmpty()) options.add(new WebserviceOptions("developerPayload", developerPayload));
//            options.add(new WebserviceOptions("paykey", payKey));
//            options.add(new WebserviceOptions("productID", String.valueOf(productId)));
//            options.add(new WebserviceOptions("payType", String.valueOf(payType)));
//            options.add(new WebserviceOptions("taxRate", String.valueOf(taxRate)));
//            options.add(new WebserviceOptions("price", String.valueOf(price)));
//            options.add(new WebserviceOptions("currency", currency));
//            options.add(new WebserviceOptions("reqType", "rest"));
//            if(simcc!=null)options.add(new WebserviceOptions("simcc", simcc));
//
//
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("(");
//        for(WebserviceOptions option: options){
//            sb.append(option);
//            sb.append(";");
//        }
//        sb.append(")");




        //String baseUrl = "http://dev.aptoide.com/webservices/processInAppBilling/iabpurchasestatus/"+apiVersion+"/options="+sb.toString();
        String baseUrl = "https://webservices.aptoide.com/webservices/3/processInAppBilling";


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("mode","json");
        parameters.put("apiversion", String.valueOf(apiVersion));
        parameters.put("reqtype","iabpurchasestatus");
        parameters.put("paykey",payKey);
        parameters.put("payreqtype", "rest");
        parameters.put("paytype", String.valueOf(payType));

        parameters.put("repo", repo);
        parameters.put("taxrate", String.valueOf(taxRate));
        parameters.put("productid", String.valueOf(productId));
        parameters.put("price", String.valueOf(price));

        token = SecurePreferences.getInstance().getString("access_token", null);

        parameters.put("access_token",token);
        parameters.put("currency",currency);
        parameters.put("simcc",simcc);

        if(developerPayload!=null && !TextUtils.isEmpty(developerPayload)) parameters.put("developerpayload", developerPayload);



        HttpContent content = new UrlEncodedContent(parameters);


        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);
        //setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
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

        return response.parseAs(getResultType());    }

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

    public void setRest(boolean rest) {
        this.rest = rest;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getPayType() {
        return payType;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setPayKey(String payKey) {
        this.payKey = payKey;
    }

    public String getPayKey() {
        return payKey;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public void setSimcc(String simcc) {
        this.simcc = simcc;
    }

    public void setRepo(String repo) {
        this.repo = repo;
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
