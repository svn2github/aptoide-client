package openiab.webservices;

import android.util.Log;

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
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import openiab.webservices.json.IabSimpleResponseJson;


public class PaypalPurchaseAuthorizationRequest extends GoogleHttpClientSpiceRequest<IabSimpleResponseJson> {

    private String token;
    private String authToken;

    public PaypalPurchaseAuthorizationRequest() {
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
        String baseUrl = WebserviceOptions.WebServicesLink + "3/productPurchaseAuthorization";

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

}
