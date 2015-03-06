package cm.aptoide.ptdev.webservices;

import android.content.Context;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;







import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AddCommentRequest extends RetrofitSpiceRequest<GenericResponseV2, AddCommentRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/addApkComment")
        @FormUrlEncoded
        GenericResponseV2 addComment(@FieldMap HashMap<String, String> args);
    }

    String baseUrl = WebserviceOptions.WebServicesLink + "3/addApkComment";
    private Context context;
    private String token;
    private String repo;
    private String packageName;
    private String apkversion;
    private String text;

    private String answearTo;

    public void setToken(String token) {
        this.token = token;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setApkversion(String apkversion) {
        this.apkversion = apkversion;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnswearTo(String answearTo) { this.answearTo = answearTo; }

    public AddCommentRequest(Context context) {
        super(GenericResponseV2.class, Webservice.class);
        this.context = context;
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {

//        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");

        parameters.put("repo", repo);
        parameters.put("apkid", packageName);
        parameters.put("apkversion", apkversion);
        parameters.put("text", text);
        parameters.put("lang",AptoideUtils.getMyCountryCode(context));
        if(answearTo != null) {
            parameters.put("answerto", answearTo);
        }

//        HttpContent content = new UrlEncodedContent(parameters);

//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        token = SecurePreferences.getInstance().getString("access_token", "empty");

        parameters.put("access_token", token);
//        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

//        request.setConnectTimeout(30000);
//        request.setReadTimeout(30000);

//        request.setParser(new JacksonFactory().createJsonObjectParser());

//        return request.execute().parseAs( getResultType() );

        GenericResponseV2 responseV2 = null;

        try{
            responseV2 = getService().addComment(parameters);
        }catch (RetrofitError error){
            OauthErrorHandler.handle(error);
        }


        return responseV2;

    }

}
