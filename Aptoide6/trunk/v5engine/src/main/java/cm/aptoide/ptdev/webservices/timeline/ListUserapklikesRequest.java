package cm.aptoide.ptdev.webservices.timeline;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OauthErrorHandler;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.ListUserapklikesRequest.ListUserapklikes;
import cm.aptoide.ptdev.webservices.timeline.json.ListapklikesJson;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserapklikesRequest extends RetrofitSpiceRequest<ListapklikesJson, ListUserapklikes> {

    private int limit;
    private long postID;

    public interface ListUserapklikes {
        @POST(WebserviceOptions.WebServicesLink+"3/listUserApkInstallLikes")
        @FormUrlEncoded
        public ListapklikesJson run(@FieldMap HashMap<String, String> args);
    }
    public ListUserapklikesRequest() {
        super(ListapklikesJson.class, ListUserapklikes.class);
    }
    public void setPostID(long id){		this.postID = id;	}
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public ListapklikesJson loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("post_id", String.valueOf(postID));
        parameters.put("limit", String.valueOf(limit));

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);


        try{
            return getService().run(parameters);
        }catch (RetrofitError e){
            OauthErrorHandler.handle(e);
        }

        return null;
    }
}
