package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.ListRecomended;
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

public class ListUserbasedApkRequest extends GoogleHttpClientSpiceRequest<ListRecomended> {


    String baseUrl = WebserviceOptions.WebServicesLink + "3/listUserBasedApks";

    private Context context;
    private String token;
    private int limit;


    public ListUserbasedApkRequest(Context context) {
        super(ListRecomended.class);
        this.context = context;
    }


    @Override
    public ListRecomended loadDataFromNetwork() throws Exception {
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");

        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));

        if(limit>0)options.add(new WebserviceOptions("limit", String.valueOf(limit)));
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

        token = SecurePreferences.getInstance().getString("access_token", null);

        if (token!=null) {
            parameters.put("access_token", token);
            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
        }

        Log.d("Aptoide-ApkUserBased", url.toString());
        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }



    public void setLimit(int limit) {
        this.limit = limit;
    }



}
