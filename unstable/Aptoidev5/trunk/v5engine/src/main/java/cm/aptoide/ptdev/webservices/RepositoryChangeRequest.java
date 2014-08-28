package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class RepositoryChangeRequest extends GoogleHttpClientSpiceRequest<RepositoryChangeJson> {


    String baseUrl = "https://webservices.aptoide.com/webservices/listRepositoryChange";
    private String repos;
    private String hashes;

    public RepositoryChangeRequest() {
        super(RepositoryChangeJson.class);
    }

    public void setRepos(String repos){
        this.repos = repos;
    }

    public void setHashes(String hashes){
        this.hashes = hashes;
    }

    @Override
    public RepositoryChangeJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("repo", repos);
        parameters.put("hash", hashes);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }
}
