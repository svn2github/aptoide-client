package cm.aptoide.ptdev.webservices.timeline;







import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OAuthRefreshAccessTokenHandler;

/**
 * Created by asantos on 24-09-2014.
 */
public abstract class TimelineRequest<E> extends RetrofitSpiceRequest<E, TimelineRequest.Webservice<E>> {


    public TimelineRequest(Class<E> i, Class<? extends TimelineRequest.Webservice<E>> e) {
        super(i, (Class<Webservice<E>>) e);
    }

    protected abstract String getUrl();

    protected abstract HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters);

    public interface Webservice<E> {
        public E run(HashMap<String, String> args);
    }

    @Override
    public E loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        fillWithExtraOptions(parameters);

        String token = SecurePreferences.getInstance().getString("access_token", "empty");


        E response = getService().run(parameters);
//        HttpContent content = new UrlEncodedContent(parameters);
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        parameters.put("access_token", token);
//        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
//
//
//        request.setConnectTimeout(20000);
//        request.setReadTimeout(20000);
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );





        return response;

    }
}
