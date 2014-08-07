package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class ListRelatedApkRequest extends GoogleHttpClientSpiceRequest<RelatedApkJson> {


    String baseUrl = "https://webservices.aptoide.com/webservices/2/listRelatedApks";
    private String repos;
    private int limit;
    private Context context;
    private String packageName;
    private int vercode;
    private String mode;

    public ListRelatedApkRequest(Context context) {
        super(RelatedApkJson.class);
        this.context = context;
    }

    public void setRepos(String repos) {
        this.repos = repos;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public RelatedApkJson loadDataFromNetwork() throws Exception {
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("apkid", packageName);

        if(repos!=null)options.add(new WebserviceOptions("repo", repos));
        if(mode!=null)options.add(new WebserviceOptions("type", mode));
        options.add(new WebserviceOptions("limit", String.valueOf(limit)));
        options.add(new WebserviceOptions("vercode", String.valueOf(vercode)));
        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));


        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        parameters.put("options", sb.toString());

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        Log.d("Aptoide-ApkRelated", url.toString());

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVercode(int vercode) {
        this.vercode = vercode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
