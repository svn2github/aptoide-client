package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 24-09-2014.
 */
public class RegisterUserApkInstallRequest extends TimelineRequest<GenericResponse> {
    private int appId; // app_id
    public void setAppId(int id){
        this.appId = id;
    }


    public interface RegisterUserApkInstall extends TimelineRequest.Webservice<GenericResponse>{
    }

    public RegisterUserApkInstallRequest() {
        super(GenericResponse.class, RegisterUserApkInstall.class);
    }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/registerUserApkInstall";
    }
    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("appid", String.valueOf(appId));
        return parameters;
    }
}