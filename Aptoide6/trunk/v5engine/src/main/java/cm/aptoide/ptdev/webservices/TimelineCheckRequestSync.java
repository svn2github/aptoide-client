package cm.aptoide.ptdev.webservices;

import android.preference.PreferenceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.TimelineActivityJson;

/**
 * Created by rmateus on 23-10-2014.
 */
public class TimelineCheckRequestSync {


    public static TimelineActivityJson getRequest(String type) throws IOException {

            GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink + "3/checkUserApkInstallsActivity");
            HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();

            HashMap<String, String> parameters = new HashMap<String, String>();

            parameters.put("access_token", SecurePreferences.getInstance().getString("access_token", "empty"));


            //new_installs, owned_activity, related_activity
            parameters.put("type", type);
            parameters.put("mode", "json");
            parameters.put("timestamp", String.valueOf(PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getLong("timelineTimestamp", 1)));


            HttpContent content = new UrlEncodedContent(parameters);
            HttpRequest httpRequest = requestFactory.buildPostRequest(url, content);
            httpRequest.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, requestFactory));
            httpRequest.setParser(new JacksonFactory().createJsonObjectParser());

            return httpRequest.execute().parseAs(TimelineActivityJson.class);

    }

}
