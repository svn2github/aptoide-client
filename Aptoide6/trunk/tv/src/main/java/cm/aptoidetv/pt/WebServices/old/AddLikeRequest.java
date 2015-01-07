package cm.aptoidetv.pt.WebServices.old;

import android.content.Context;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoidetv.pt.R;
import cm.aptoidetv.pt.SecurePrefs.SecurePreferences;
import cm.aptoidetv.pt.WebServices.old.json.GenericResponseV2;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
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
        GenericResponseV2 addApkLike(@FieldMap HashMap<String, String> apkversion);
    }

    public AddLikeRequest(Context activity) {
        super(GenericResponseV2.class, Webservice.class);
        repo= activity.getString(R.string.defaultstorename);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setpackagename(String packageName) {
        this.packageName = packageName;
    }

    public void setApkversion(String apkversion) {
        this.apkversion = apkversion;
    }

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

        try{
            response = getService().addApkLike(parameters);
        }catch (RetrofitError error){
            OauthErrorHandler.handle(error);
        }

        return response;

    }
}
