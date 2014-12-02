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
public class ChangeUserApkInstallStatusRequest extends RetrofitSpiceRequest<GenericResponse, ChangeUserApkInstallStatusRequest.ChangeUserApkInstallStatus> {

    public static final String STATUSACTIVE = "active";
    public static final String STATUSHIDDEN = "hidden";

    private long postID;
    public void setPostId(long id){this.postID = id;}
    private String status;

    public interface ChangeUserApkInstallStatus{
        @POST(WebserviceOptions.WebServicesLink+"3/changeUserApkInstallStatus")
        @FormUrlEncoded
        public GenericResponse run(@FieldMap HashMap<String, String> args );
    }

    public void setPostStatus(String status){this.status = status;}

    public ChangeUserApkInstallStatusRequest() {  super(GenericResponse.class, ChangeUserApkInstallStatus.class);  }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("id", String.valueOf(postID));
        parameters.put("status", status);

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        return getService().run(parameters);
    }
}
