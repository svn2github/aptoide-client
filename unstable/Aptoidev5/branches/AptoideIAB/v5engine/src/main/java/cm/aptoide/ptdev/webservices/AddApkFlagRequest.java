package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created by j-pac on 30-05-2014.
 */
public class AddApkFlagRequest extends GoogleHttpClientSpiceRequest<GenericResponseV2> {

    String baseUrl = "http://webservices.aptoide.com/webservices/3/addApkFlag";

    private String token;
    private String repo;
    private String md5sum;
    private String flag;

    public AddApkFlagRequest() {
        super(GenericResponseV2.class);
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();


        parameters.put("repo", repo);
        parameters.put("md5sum", md5sum);
        parameters.put("flag", flag);
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        if (token!=null) {
            parameters.put("access_token", token);
            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
        }

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}