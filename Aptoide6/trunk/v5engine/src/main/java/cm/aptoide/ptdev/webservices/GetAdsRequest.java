package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.text.TextUtils;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.ads.AptoideAdNetworks;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by rmateus on 29-07-2014.
 */
public class GetAdsRequest extends RetrofitSpiceRequest<ApkSuggestionJson, GetAdsRequest.Webservice> {

    private int CONNECTION_TIMEOUT = 10000;

    private String location;
    private String keyword;
    private int limit;
    private String package_name;
    private String repo;

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public GetAdsRequest(Context context) {
        super(ApkSuggestionJson.class, Webservice.class);

    }

    public interface Webservice{
        @POST("/webservices.aptwords.net/api/2/getAds")
        @FormUrlEncoded
        ApkSuggestionJson getAds(@FieldMap HashMap<String, String> arg);
    }

    String url = "http://webservices.aptwords.net/api/2/getAds";
    ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override
    public ApkSuggestionJson loadDataFromNetwork() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("q", AptoideUtils.filters(Aptoide.getContext()));
        parameters.put("lang", AptoideUtils.getMyCountryCode(Aptoide.getContext()));

        String myid = AptoideUtils.getSharedPreferences().getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
        parameters.put("cpuid", myid);

        String mature = "1";

        if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){
            mature = "0";
        }

        parameters.put("location","native-aptoide:" + location);
        parameters.put("type", "1-3");
        parameters.put("flag", "gms");
        parameters.put("keywords", keyword);

        String oemid = Aptoide.getConfiguration().getExtraId();

        if( !TextUtils.isEmpty(oemid) ){
            parameters.put("oemid", oemid);
        }


        parameters.put("limit", String.valueOf(limit));

        parameters.put("get_mature", mature);

        parameters.put("partners", "1-3,5-6");


        parameters.put("app_pkg", package_name);
        parameters.put("app_store", repo);
        parameters.put("filter_pkg", "true");



        parameters.put("conn_type", AptoideUtils.NetworkUtils.getConnectionType().toString());

        if(Aptoide.DEBUG_MODE){
            parameters.put("country", AptoideUtils.getSharedPreferences().getString("forcecountry", null));
        }

//        GenericUrl url = new GenericUrl(this.url);
//
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//        request.setSuppressUserAgentSuffix(true);
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        request.setConnectTimeout(CONNECTION_TIMEOUT);
//        request.setReadTimeout(CONNECTION_TIMEOUT);

//        ApkSuggestionJson result = request.execute().parseAs( getResultType() );

        ApkSuggestionJson result = getService().getAds(parameters);

        Map<String, String> adsParams = new HashMap<String, String>();
        adsParams.put("placement", location);


        for(ApkSuggestionJson.Ads suggestionJson : result.getAds()) {
            String ad_type = suggestionJson.getInfo().getAd_type();
            adsParams.put("type", ad_type);

            FlurryAgent.logEvent("Get_Sponsored_Ad", adsParams);

            if(suggestionJson.getPartner() != null){

                try{
                    String impressionUrlString = suggestionJson.getPartner().getPartnerData().getImpression_url();

                    impressionUrlString = AptoideAdNetworks.parseString(suggestionJson.getPartner().getPartnerInfo().getName(), Aptoide.getContext(), impressionUrlString);

                    Request request = new Request.Builder().get().url(impressionUrlString).build();

                    new OkHttpClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {

                        }

                        @Override
                        public void onResponse(Response response) throws IOException {

                        }
                    });

//                    GenericUrl impressionUrl = new GenericUrl(impressionUrlString);
//                    getHttpRequestFactory().buildGetRequest(impressionUrl).setSuppressUserAgentSuffix(true).executeAsync(executor);

                } catch (Exception ignored) {}

            }

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
