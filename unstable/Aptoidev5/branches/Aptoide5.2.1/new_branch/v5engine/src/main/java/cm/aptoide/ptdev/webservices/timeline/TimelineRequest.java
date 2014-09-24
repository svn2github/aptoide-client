package cm.aptoide.ptdev.webservices.timeline;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;

/**
 * Created by asantos on 24-09-2014.
 */
public abstract class TimelineRequest<E> extends GoogleHttpClientSpiceRequest<E> {
    public TimelineRequest(Class<E> i) {    super(i);    }
    protected abstract String GetURL();
    protected abstract HashMap<String, String > fillWithExtraOptions(HashMap<String, String > parameters);
    @Override
    public E loadDataFromNetwork() throws Exception {
        GenericUrl url= new GenericUrl(GetURL());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        fillWithExtraOptions(parameters);

        String token = SecurePreferences.getInstance().getString("access_token", null);

        HttpContent content = new UrlEncodedContent(parameters);
        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        if (token!=null) {
            parameters.put("access_token", token);
            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
        }

        request.setConnectTimeout(20000);
        request.setReadTimeout(20000);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }
}
