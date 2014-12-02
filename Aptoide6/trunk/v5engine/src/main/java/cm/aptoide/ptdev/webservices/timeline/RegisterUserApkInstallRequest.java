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
public class RegisterUserApkInstallRequest extends RetrofitSpiceRequest<GenericResponse, RegisterUserApkInstallRequest.RegisterUserApkInstall>{
    private int appId; // app_id
    public void setAppId(int id){
        this.appId = id;
    }

    public interface RegisterUserApkInstall {
        @POST(WebserviceOptions.WebServicesLink+"3/registerUserApkInstall")
        @FormUrlEncoded
        public GenericResponse run(@FieldMap HashMap<String, String> args);
    }

    public RegisterUserApkInstallRequest() {
        super(GenericResponse.class, RegisterUserApkInstall.class);
    }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("appid", String.valueOf(appId));

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        return getService().run(parameters);
    }
}