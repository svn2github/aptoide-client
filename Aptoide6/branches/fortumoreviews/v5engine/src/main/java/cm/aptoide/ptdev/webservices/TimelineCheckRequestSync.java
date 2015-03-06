package cm.aptoide.ptdev.webservices;

import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.json.TimelineActivityJson;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by rmateus on 23-10-2014.
 */
public class TimelineCheckRequestSync {


    public interface WebserviceTimeline{

        @POST("/3/checkUserApkInstallsActivity")
        @FormUrlEncoded
        TimelineActivityJson post(@FieldMap HashMap<String, String> args);

    }

    static int retries = 0;

    public static TimelineActivityJson getRequest(String type) throws IOException {



        HashMap<String, String> params = new HashMap<>();






//            GenericUrl url = new GenericUrl(WebserviceOptions.WebServicesLink + "3/checkUserApkInstallsActivity");
//            HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();
            params.put("access_token", SecurePreferences.getInstance().getString("access_token", "empty"));
            //new_installs, owned_activity, related_activity
        params.put("type", type);
        params.put("mode", "json");
        params.put("timestamp", String.valueOf(PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getLong("timelineTimestamp", 1)));

        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://webservices.aptoide.com/webservices").setConverter(new JacksonConverter(mapper)).build();

            //Request request = new Request.Builder().url("http:/" + WebserviceOptions.WebServicesLink + "3/checkUserApkInstallsActivity").post(body).build();

//            HttpContent content = new UrlEncodedContent(parameters);
//            HttpRequest httpRequest = requestFactory.buildPostRequest(url, content);
//            httpRequest.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, requestFactory));
//            httpRequest.setParser(new JacksonFactory().createJsonObjectParser());
//
//            return httpRequest.execute().parseAs(TimelineActivityJson.class);

        //OkHttpClient client = new OkHttpClient();
        //Response execute = client.newCall(request).execute();
        //return mapper.readValue(execute.body().charStream(), TimelineActivityJson.class);

        try{
            TimelineActivityJson post = adapter.create(WebserviceTimeline.class).post(params);
            retries = 0;
            return post;
        }catch (RetrofitError error){
            try{
                retries++;
                if(retries<3){
                    OauthErrorHandler.handle(error);
                }
            }catch (RetrofitError retrofitError){
                return getRequest(type);
            }
        }

        return null;


    }

}
