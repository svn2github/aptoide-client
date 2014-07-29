package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by rmateus on 29-07-2014.
 */
public class RegisterAdRequest extends GoogleHttpClientSpiceRequest<GenericResponseV2> {


    private final Context context;
    private String location;
    private String keyword;

    public RegisterAdRequest(Context context) {
        super(GenericResponseV2.class);
        this.context = context;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    String url;

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {


        HashMap<String, String> parameters = new HashMap<String, String>();
        String mature = "1";

        if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){
            mature = "0";
        }


        parameters.put("q", AptoideUtils.filters(context));
        parameters.put("lang", AptoideUtils.getMyCountryCode(context));

        String myid = PreferenceManager.getDefaultSharedPreferences(context).getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
        parameters.put("cpuid", myid);

        parameters.put("location","native-aptoide:" + location);
        parameters.put("type","app:suggested");
        parameters.put("limit","3");
        parameters.put("keywords", keyword);
        parameters.put("get_mature", mature);



        GenericUrl url = new GenericUrl(this.url);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setParser(new JacksonFactory().createJsonObjectParser());



        return request.execute().parseAs( getResultType() );

    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
