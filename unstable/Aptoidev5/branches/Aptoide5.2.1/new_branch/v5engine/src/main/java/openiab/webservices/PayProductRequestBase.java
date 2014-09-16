package openiab.webservices;

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
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import openiab.webservices.json.IabPurchaseStatusJson;

/**
 * Created by asantos on 15-09-2014.
 */
public abstract class PayProductRequestBase extends BaseRequest<IabPurchaseStatusJson> {
    private String productId;
    private String oemId;
    private String repo;
    private String developerPayload;
    private String price;
    private String currency;

    @Override
    protected GenericUrl getURL() {
        String baseUrl = "https://webservices.aptoide.com/webservices/3/payProduct";
        return new GenericUrl(baseUrl);
    }

    public PayProductRequestBase() {
        super(IabPurchaseStatusJson.class);
    }

    protected abstract void optionsAddExtra(List<WebserviceOptions> options);
    protected abstract void parametersputExtra(Map<String, String> parameters);

    @Override
    public IabPurchaseStatusJson loadDataFromNetwork() throws Exception {

        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("oemid", oemId));
        options.add(new WebserviceOptions("developerPayload", developerPayload));
        optionsAddExtra(options);

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        GenericUrl url = getURL();

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("mode","json");
        parameters.put("developerPayload",developerPayload);
        parameters.put("productid",productId);
        parameters.put("apiversion",apiVersion);
        parameters.put("reqType","billing");
        parameters.put("repo",repo);
        parameters.put("price",price);
        parameters.put("currency",currency);
        parameters.put("oemid",oemId);
        parametersputExtra(parameters);

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

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
