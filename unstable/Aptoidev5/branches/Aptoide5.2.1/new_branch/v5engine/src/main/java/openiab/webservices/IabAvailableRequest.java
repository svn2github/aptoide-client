package openiab.webservices;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;
import openiab.webservices.json.IabAvailableJson;

public class IabAvailableRequest extends BaseRequest<IabAvailableJson> {
    public IabAvailableRequest() {
        super(IabAvailableJson.class);
    }

    @Override
    public IabAvailableJson loadDataFromNetwork() throws Exception {
        GenericUrl url = getURL();

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabavailable");
        parameters.put("mode","json");
        parameters.put("package",packageName);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }
}
