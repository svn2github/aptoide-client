package cm.aptoide.ptdev.webservices;

import android.content.Context;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.AllCommentsJson;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by rmateus on 30-12-2013.
 */
public class AddLikeRequest extends RetrofitSpiceRequest<GenericResponseV2, AddLikeRequest.Webservice> {

    private String token;
    private String repo;
    private String packageName;
    private String apkversion;

    public void setLike(boolean isLike) {
        this.isLike = isLike;
    }

    private boolean isLike;

    public interface Webservice {
        @FormUrlEncoded
        @POST("/webservices.aptoide.com/webservices/3/addApkLike")
        GenericResponseV2 addApkLike(@FieldMap HashMap<String, String> apkversion );
    }

    public AddLikeRequest(Context activity) {
        super(GenericResponseV2.class, Webservice.class);
    }

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

    String baseUrl = WebserviceOptions.WebServicesLink+"3/addApkLike";
    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {

        //GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();
        token = SecurePreferences.getInstance().getString("access_token", "empty");

        parameters.put("mode", "json");
        parameters.put("access_token", token);
        parameters.put("repo", repo);
        parameters.put("like", isLike?"like":"dontLike");
        parameters.put("apkid", packageName);
        parameters.put("apkversion", apkversion);
        GenericResponseV2 response = null;

        Webservice adapter = new RestAdapter.Builder().setEndpoint("http://").build().create(getRetrofitedInterfaceClass());
        setService(adapter);

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );

        try{
            response = getService().addApkLike(parameters);
        }catch (RetrofitError error){
            OauthErrorHandler.handle(error);
        }

        return response;

    }
}
