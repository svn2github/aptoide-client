package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

/**
 * Created by j-pac on 30-05-2014.
 */
public class AddApkCommentVoteRequest extends RetrofitSpiceRequest<GenericResponseV2, AddApkCommentVoteRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/addApkCommentVote")
        @FormUrlEncoded
        GenericResponseV2 addApkCommentVote(@FieldMap HashMap<String, String> args);
    }

    String baseUrl = WebserviceOptions.WebServicesLink + "3/addApkCommentVote";
    public enum CommentVote {
        up, down;
    }

    private String token;
    private String repo;
    private int cmtid;
    private CommentVote vote;

    public AddApkCommentVoteRequest() {
        super(GenericResponseV2.class, Webservice.class);
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {
//        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("repo", repo);
        parameters.put("cmtid", String.valueOf(cmtid));
        parameters.put("vote", vote.name());
        parameters.put("mode", "json");
        token = SecurePreferences.getInstance().getString("access_token", "empty");

        parameters.put("access_token", token);

        RestAdapter adapter = new RestAdapter.Builder().setConverter(new JacksonConverter()).setEndpoint("http://").build();


        setService(adapter.create(Webservice.class));

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);


//        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
//
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
        GenericResponseV2 responseV2 = null;
        try{
            responseV2 = getService().addApkCommentVote(parameters);
        }catch (RetrofitError error){
            OauthErrorHandler.handle(error);
        }

        return responseV2;
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
