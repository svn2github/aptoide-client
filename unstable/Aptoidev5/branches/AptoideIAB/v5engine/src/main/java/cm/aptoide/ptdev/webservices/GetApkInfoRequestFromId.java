package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfoRequestFromId extends GoogleHttpClientSpiceRequest<GetApkInfoJson> {


    private String repoName;
    private String packageName;
    private String versionName;
    private String token;
    private Context context;

    public void setAppId(String appId) {
        this.appId = appId;
    }

    private String appId;


    public GetApkInfoRequestFromId(Context context) {
        super(GetApkInfoJson.class);
        this.context = context;
    }

    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception {


        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("cmtlimit", "5"));
        options.add(new WebserviceOptions("payinfo", "true"));
        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));


        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        String baseUrl = "https://webservices.aptoide.com/webservices/3/getApkInfo";
        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("identif", "id:" + appId);
        parameters.put("options", sb.toString());
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        if (token!=null) {
            parameters.put("access_token", token);
            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
        }
        request.setParser(new JacksonFactory().createJsonObjectParser());

        HttpResponse response;
        try{
            response = request.execute();
        } catch (EOFException e){

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Connection", "close");
            request.setHeaders(httpHeaders);
            response = request.execute();
        }

        return response.parseAs(getResultType());
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public class WebserviceOptions {
        String key;
        String value;


        private WebserviceOptions(String key,String value) {
            this.value = value;
            this.key = key;
        }

        /**
         * Returns a string containing a concise, human-readable description of this
         * object. Subclasses are encouraged to override this method and provide an
         * implementation that takes into account the object's type and data. The
         * default implementation is equivalent to the following expression:
         * <pre>
         *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
         * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
         * {@code toString} method</a>
         * if you intend implementing your own {@code toString} method.
         *
         * @return a printable representation of this object.
         */
        @Override
        public String toString() {
            return key+"="+value;    //To change body of overridden methods use File | Settings | File Templates.
        }

    }

}
