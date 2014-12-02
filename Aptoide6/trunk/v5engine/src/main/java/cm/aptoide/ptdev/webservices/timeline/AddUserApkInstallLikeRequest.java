package cm.aptoide.ptdev.webservices.timeline;


import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 24-09-2014.
 */
public class AddUserApkInstallLikeRequest extends RetrofitSpiceRequest<GenericResponse, AddUserApkInstallLikeRequest.AddUserApkInstallLike> {
    public static final String LIKE = "like";
    public static final String DISLIKE = "dislike";
    public static final String UNLIKE = "unlike";
    private long postID;
    private String like;

    public void setPostId(long postID) {	this.postID = postID;	}
    public void setLike(String s) {	this.like = s;	}

    public AddUserApkInstallLikeRequest() { super(GenericResponse.class, AddUserApkInstallLike.class);   }


    public interface AddUserApkInstallLike {
        @POST(WebserviceOptions.WebServicesLink+"3/addUserApkInstallLike")
        @FormUrlEncoded
        public GenericResponse run(@FieldMap HashMap<String, String> args);
    }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);
        parameters.put("id", String.valueOf(postID));
        parameters.put("like", like);

        GenericResponse response = getService().run(parameters);

        return response;

    }

}
