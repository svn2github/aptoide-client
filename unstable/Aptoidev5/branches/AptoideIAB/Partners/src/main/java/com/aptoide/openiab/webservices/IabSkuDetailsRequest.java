package com.aptoide.openiab.webservices;

import android.util.Log;

import com.aptoide.openiab.webservices.json.IabSkuDetailsJson;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;


/**
 * Created by j-pac on 19-02-2014.
 */
public class IabSkuDetailsRequest extends GoogleHttpClientSpiceRequest<IabSkuDetailsJson> {

    private String apiVersion;
    private String token;
    private String mnc;
    private String mcc;

    private String simcc;

    private String packageName;
    private List<String> skuList = new ArrayList<String>();
    private String oemid;

    public IabSkuDetailsRequest() {
        super(IabSkuDetailsJson.class);
    }



    @Override
    public IabSkuDetailsJson loadDataFromNetwork() throws Exception {

        StringBuilder skus = new StringBuilder();
        for(String sku : skuList){
            skus.append(sku);
            skus.append(",");
        }

        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("package", packageName));
        options.add(new WebserviceOptions("token", token));
        if(mnc!=null)options.add(new WebserviceOptions("mnc", mnc));
        if(mcc!=null)options.add(new WebserviceOptions("mcc", mcc));
        options.add(new WebserviceOptions("oemid", oemid));
        if(simcc!=null)options.add(new WebserviceOptions("simcc", simcc.toUpperCase(Locale.ENGLISH)));
        //options.add(new WebserviceOptions("simcc", "MN"));


        options.add(new WebserviceOptions("skulist", skus.toString()));



        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");


        //String baseUrl = "http://dev.aptoide.com/webservices/processInAppBilling/iabskudetails/"+apiVersion+"/options="+sb.toString();
        String baseUrl = "https://webservices.aptoide.com/webservices/3/processInAppBilling";

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("mode","json");
        parameters.put("skulist",skus.toString());
        parameters.put("package",packageName);
        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabskudetails");

        token = SecurePreferences.getInstance().getString("access_token", null);

        parameters.put("access_token",token);

        if(mcc!=null)parameters.put("mcc",mcc);
        if(mnc!=null)parameters.put("mnc",mnc);
        if(simcc!=null)parameters.put("simcc", simcc.toUpperCase(Locale.ENGLISH));

        HttpContent content = new UrlEncodedContent(parameters);


        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-InappBillingRequest", baseUrl);
        setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

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

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public void setSimcc(String simcc) {
        this.simcc = simcc;
    }

    public void setOemid(String oemid) {
        this.oemid = oemid;
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