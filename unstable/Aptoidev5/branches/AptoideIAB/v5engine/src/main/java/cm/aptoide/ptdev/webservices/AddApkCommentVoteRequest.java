package cm.aptoide.ptdev.webservices;

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
public class AddApkCommentVoteRequest extends GoogleHttpClientSpiceRequest<GenericResponseV2> {

    String baseUrl = "http://webservices.aptoide.com/webservices/2/addApkCommentVote";

    private String token;
    private String repo;
    private int cmtid;
    private String vote;

    public AddApkCommentVoteRequest() {
        super(GenericResponseV2.class);
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {
        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("token", token);
        parameters.put("repo", repo);
        parameters.put("cmtid", String.valueOf(cmtid));
        parameters.put("vote", vote);
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setCmtid(int cmtid) {
        this.cmtid = cmtid;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }
}
