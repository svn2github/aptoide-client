package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.impl.analytics.FlurryAnalyticsModule;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import org.apache.http.params.HttpParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by rmateus on 29-07-2014.
 */
public class GetAdsRequest extends GoogleHttpClientSpiceRequest<ApkSuggestionJson> {

    private int CONNECTION_TIMEOUT = 10000;
    private final Context context;
    private String location;
    private String keyword;
    private int limit;

    public GetAdsRequest(Context context) {
        super(ApkSuggestionJson.class);
        this.context = context;
    }

    String url = "http://webservices.aptwords.net/api/2/getAds";

    @Override
    public ApkSuggestionJson loadDataFromNetwork() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("q", AptoideUtils.filters(context));
        parameters.put("lang", AptoideUtils.getMyCountryCode(context));

        String myid = AptoideUtils.getSharedPreferences().getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
        parameters.put("cpuid", myid);

        String mature = "1";



        if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){
            mature = "0";
        }

        parameters.put("location","native-aptoide:" + location);
        parameters.put("type", "url:banner,url:googleplay,app:suggested");
        parameters.put("keywords", keyword);

        String oemid = Aptoide.getConfiguration().getExtraId();

        if(!TextUtils.isEmpty(oemid)){
            parameters.put("oemid", oemid);
        }


        parameters.put("limit", String.valueOf(limit));

        parameters.put("get_mature", mature);

        GenericUrl url = new GenericUrl(this.url);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setParser(new JacksonFactory().createJsonObjectParser());

        request.setConnectTimeout(CONNECTION_TIMEOUT);
        request.setReadTimeout(CONNECTION_TIMEOUT);

        ApkSuggestionJson result = request.execute().parseAs( getResultType() );

        Map<String, String> adsParams = new HashMap<String, String>();
        adsParams.put("placement", location);

        for(ApkSuggestionJson.Ads suggestionJson : result.getAds()) {
            String ad_type = suggestionJson.getInfo().getAd_type();
            adsParams.put("type", ad_type);

            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Get_Sponsored_Ad", adsParams);
//            Log.d("AdsFlurry", "Map is " + adsParams);
        }

        return result;
    }

    public void setTimeout(int timeout){
        CONNECTION_TIMEOUT = timeout;
    }


    public void setLocation(String location) {
        this.location = location;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
