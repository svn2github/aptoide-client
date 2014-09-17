package openiab.webservices;

import android.text.TextUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import openiab.webservices.json.IabPurchaseStatusJson;


/**
 * Created by j-pac on 19-02-2014.
 */
public class IabPurchaseStatusRequest extends BaseRequest<IabPurchaseStatusJson> {
    private int orderId;
    private int productId;
    private int payType;
    private double taxRate;
    private double price;
    private String currency;
    private String payKey;
    private String developerPayload;
    private String simcc;
    private String repo;
    private boolean rest;

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


        GenericUrl url = getURL();

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

        request.setParser(new JacksonFactory().createJsonObjectParser());

        HttpResponse response;
        try{
            response = request.execute();
        } catch (EOFException e ){
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Connection", "close");
            request.setHeaders(httpHeaders);
            response = request.execute();
        }

        return response.parseAs(getResultType());
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public void setRest(boolean rest) {
        this.rest = rest;
    }
}
