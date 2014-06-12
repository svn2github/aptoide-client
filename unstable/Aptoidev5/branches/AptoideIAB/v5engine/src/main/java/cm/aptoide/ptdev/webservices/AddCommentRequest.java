package cm.aptoide.ptdev.webservices;

import android.content.Context;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AddCommentRequest extends GoogleHttpClientSpiceRequest<GenericResponse>{

    String baseUrl = "http://webservices.aptoide.com/webservices/addApkComment";
    private Context context;
    private String token;
    private String repo;
    private String packageName;
    private String apkversion;
    private String text;

    private String answearTo;

    public void setToken(String token) {
        this.token = token;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setApkversion(String apkversion) {
        this.apkversion = apkversion;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnswearTo(String answearTo) { this.answearTo = answearTo; }

    public AddCommentRequest(Context context) {
        super(GenericResponse.class);
        this.context = context;
    }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("token", token);
        parameters.put("repo", repo);
        parameters.put("apkid", packageName);
        parameters.put("apkversion", apkversion);
        parameters.put("text", text);
        parameters.put("lang", AptoideUtils.getMyCountry(context)+"_"+AptoideUtils.getMyCountryCode(context));
        if(answearTo != null) {
            parameters.put("answerto", answearTo);
        }

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setConnectTimeout(30000);
        request.setReadTimeout(30000);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

}
