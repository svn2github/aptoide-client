package com.aptoide.openiab.webservices;

import android.util.Log;

import com.aptoide.openiab.webservices.json.IabSimpleResponseJson;
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

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;


/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchaseAuthorizationRequest extends GoogleHttpClientSpiceRequest<IabSimpleResponseJson> {

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
    private String authToken;

    public IabPurchaseAuthorizationRequest() {
        super(IabSimpleResponseJson.class);
    }

    @Override
    public IabSimpleResponseJson loadDataFromNetwork() throws Exception {


        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("reqType", "rest"));

        if(authToken!=null){
            options.add(new WebserviceOptions("authToken", authToken));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        //String baseUrl = "http://dev.aptoide.com/webservices/productPurchaseAuthorization/"+token+"/1/options="+sb.toString();
        String baseUrl = "http://webservices.aptoide.com/webservices/3/productPurchaseAuthorization";

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("mode","json");
        parameters.put("reqType","rest");
        parameters.put("payType","1");
        parameters.put("authToken",authToken);

        token = SecurePreferences.getInstance().getString("access_token", null);

        parameters.put("access_token",token);

        HttpContent content = new UrlEncodedContent(parameters);

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

  

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
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
