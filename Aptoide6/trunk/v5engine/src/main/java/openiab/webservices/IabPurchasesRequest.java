package openiab.webservices;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.EOFException;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import openiab.webservices.json.IabPurchasesJson;

public class IabPurchasesRequest extends BaseRequest<IabPurchasesJson> {

    private String type;

    public IabPurchasesRequest() {
        super(IabPurchasesJson.class);
    }

    @Override
    public IabPurchasesJson loadDataFromNetwork() throws Exception {
        GenericUrl url = getURL();

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabpurchases");

        parameters.put("package",packageName);
        parameters.put("purchasetype",type);

        token = SecurePreferences.getInstance().getString("access_token", null);

        parameters.put("access_token",token);
        parameters.put("mode","json");
        HttpContent content = new UrlEncodedContent(parameters);

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

        return response.parseAs(getResultType());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
