package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.preferences.SecurePreferences;
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

    String baseUrl = "https://webservices.aptoide.com/webservices/3/addApkCommentVote";
    public enum CommentVote {
        up, down;
    }

    private String token;
    private String repo;
    private int cmtid;
    private CommentVote vote;

    public AddApkCommentVoteRequest() {
        super(GenericResponseV2.class);
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {
        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("repo", repo);
        parameters.put("cmtid", String.valueOf(cmtid));
        parameters.put("vote", vote.name());
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        token = SecurePreferences.getInstance().getString("access_token", null);


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

    public void setCmtid(int cmtid) {
        this.cmtid = cmtid;
    }

    public void setVote(CommentVote vote) {
        this.vote = vote;
    }
}
