package cm.aptoide.ptdev.webservices;

import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.TimelineActivityJson;

/**
 * Created by rmateus on 23-10-2014.
 */
public class TimelineCheckRequestSync {


    public static TimelineActivityJson getRequest(String type) throws IOException {

        RequestBody body = new FormEncodingBuilder()

//            GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink + "3/checkUserApkInstallsActivity");
//            HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();
            .add("access_token", SecurePreferences.getInstance().getString("access_token", "empty"))
            //new_installs, owned_activity, related_activity
            .add("type", type)
            .add("mode", "json")
            .add("timestamp", String.valueOf(PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getLong("timelineTimestamp", 1)))
            .build();

            Request request = new Request.Builder().url("http:/" + WebserviceOptions.WebServicesLink + "3/checkUserApkInstallsActivity").post(body).build();

//            HttpContent content = new UrlEncodedContent(parameters);
//            HttpRequest httpRequest = requestFactory.buildPostRequest(url, content);
//            httpRequest.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, requestFactory));
//            httpRequest.setParser(new JacksonFactory().createJsonObjectParser());
//
//            return httpRequest.execute().parseAs(TimelineActivityJson.class);

        OkHttpClient client = new OkHttpClient();
        Response execute = client.newCall(request).execute();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(execute.body().charStream(), TimelineActivityJson.class);

    }

}
