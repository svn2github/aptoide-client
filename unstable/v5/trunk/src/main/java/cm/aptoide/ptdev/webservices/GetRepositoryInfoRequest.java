package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.model.json.RepositoryInfoJson;
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
public class GetRepositoryInfoRequest extends GoogleHttpClientSpiceRequest<RepositoryInfoJson> {

    private final String storeName;
    String baseUrl = "http://webservices.aptoide.com/webservices/getRepositoryInfo/%s/json";


    public GetRepositoryInfoRequest(String storeName) {
        super(RepositoryInfoJson.class);
        this.storeName = storeName;
    }

    @Override
    public RepositoryInfoJson loadDataFromNetwork() throws Exception {
        baseUrl = String.format(Locale.ENGLISH, baseUrl, storeName);

        GenericUrl url = new GenericUrl(baseUrl);

        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

}
