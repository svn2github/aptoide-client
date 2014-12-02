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
public class AddUserApkInstallCommentRequest extends RetrofitSpiceRequest<GenericResponse, AddUserApkInstallCommentRequest.AddUserApkInstallComment> {
    private long postID;
    private String comment;

    public void setPostID(long id){		this.postID = id;	}
    public void setComment(String comment) {    this.comment = comment; }

    public AddUserApkInstallCommentRequest() {     super(GenericResponse.class, AddUserApkInstallComment.class);   }

    public interface AddUserApkInstallComment{
        @POST(WebserviceOptions.WebServicesLink+"3/addUserApkInstallComment")
        @FormUrlEncoded
        public GenericResponse run(@FieldMap HashMap<String, String> args);
    }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("id", String.valueOf(postID));
        parameters.put("text", comment);

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        return getService().run(parameters);
    }
}