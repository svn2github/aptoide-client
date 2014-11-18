package openiab.webservices;

import com.google.api.client.http.GenericUrl;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 15-09-2014.
 */
public abstract class BaseRequest<E> extends GoogleHttpClientSpiceRequest<E> {
    protected String apiVersion;
    protected String token;
    protected String packageName;

    protected GenericUrl getURL(){
        String baseUrl = WebserviceOptions.WebServicesLink+"3/processInAppBilling";
        return new GenericUrl(baseUrl);
    }

    public BaseRequest(Class<E> clazz) {
        super(clazz);
    }

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
