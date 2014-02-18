package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.RepositoryCommentsJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryCommentsRequest extends GoogleHttpClientSpiceRequest<RepositoryCommentsJson> {

    private final String storeName;
    String baseUrl = "http://webservices.aptoide.com/webservices/listRepositoryComments/%s/json";


    public ListRepositoryCommentsRequest(String storeName) {
        super(RepositoryCommentsJson.class);
        this.storeName = storeName;
    }

    @Override
    public RepositoryCommentsJson loadDataFromNetwork() throws Exception {
        baseUrl = String.format(Locale.ENGLISH, baseUrl, storeName);

        GenericUrl url = new GenericUrl(baseUrl);
        Log.d("Aptoide-Request", url.toString());
        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

        request.setConnectTimeout(10000);
        request.setReadTimeout(10000);
        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

}
