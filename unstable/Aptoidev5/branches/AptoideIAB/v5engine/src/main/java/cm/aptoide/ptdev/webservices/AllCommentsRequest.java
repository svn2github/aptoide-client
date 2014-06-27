package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.AllCommentsJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AllCommentsRequest extends GoogleHttpClientSpiceRequest<AllCommentsJson>{

    String baseUrl = "http://webservices.aptoide.com/webservices/listApkComments";
    private String repoName;
    private String packageName;
    private String versionName;

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public AllCommentsRequest() {
        super(AllCommentsJson.class);
    }

    @Override
    public AllCommentsJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("repo", repoName);
        parameters.put("apkid", packageName);
        parameters.put("apkversion", versionName);
        parameters.put("mode", "json");

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }
}
