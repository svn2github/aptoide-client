package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryLikesRequest extends GoogleHttpClientSpiceRequest<RepositoryLikesJson> {

    private final String storeName;

    public ListRepositoryLikesRequest(String storeName) {
        super(RepositoryLikesJson.class);
        this.storeName = storeName;
    }

    @Override
    public RepositoryLikesJson loadDataFromNetwork() throws Exception {
        String baseUrl = "http://webservices.aptoide.com/webservices/listRepositoryLikes";

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("repo", storeName);
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setConnectTimeout(10000);
        request.setReadTimeout(10000);
        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

}
