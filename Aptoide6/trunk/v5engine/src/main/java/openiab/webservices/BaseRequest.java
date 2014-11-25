package openiab.webservices;


import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.ListRelatedApkRequest;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 15-09-2014.
 */
public abstract class BaseRequest<E, Y> extends RetrofitSpiceRequest<E, Y> {
    protected String apiVersion;
    protected String token;
    protected String packageName;

    public BaseRequest(Class<E> clazz, Class<Y> retrofitedInterfaceClass) {
        super(clazz, retrofitedInterfaceClass);
    }





//    protected GenericUrl getURL(){
//        String baseUrl = WebserviceOptions.WebServicesLink+"3/processInAppBilling";
//        return new GenericUrl(baseUrl);
//    }


    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
