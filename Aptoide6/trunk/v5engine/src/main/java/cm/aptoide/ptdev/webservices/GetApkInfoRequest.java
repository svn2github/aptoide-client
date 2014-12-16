package cm.aptoide.ptdev.webservices;

import android.content.Context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import retrofit.RetrofitError;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 07-08-2014.
 */
public abstract class  GetApkInfoRequest extends RetrofitSpiceRequest<GetApkInfoJson, GetApkInfoRequest.Webservice> {


    private boolean fromSponsored;

    public void setFromSponsored(boolean fromSponsored) {
        this.fromSponsored = fromSponsored;
    }

    public boolean isFromSponsored() {
        return fromSponsored;
    }

    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/getApkInfo")
        @FormUrlEncoded

        GetApkInfoJson getApkInfo(@FieldMap HashMap<String, String> args);
    }

    protected String repoName;
    protected String packageName;
    protected String versionName;
    protected String token;
    protected Context context;
    public GetApkInfoRequest(Context context) {
        super(GetApkInfoJson.class, GetApkInfoRequest.Webservice.class);
        this.context = context;
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }

    protected abstract ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options);
    protected abstract HashMap<String, String > getParameters();
    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception{
        ArrayList<WebserviceOptions> options = getoptions();
        token = SecurePreferences.getInstance().getString("access_token", null);
        fillWithExtraOptions(options);
        HashMap<String, String > parameters = getParameters();
        parameters.put("options", buildOptions(options));
        parameters.put("mode", "json");

        if(fromSponsored){
            parameters.put("adview", "1");
        }

//        HttpContent content = new UrlEncodedContent(parameters);
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(
//                new GenericUrl( WebserviceOptions.WebServicesLink+"3/getApkInfo"),
//                content);
//        if (token!=null) {
//            parameters.put("access_token", token);
//            request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
//        }
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//        request.setReadTimeout(5000);
//        HttpResponse response;
//        try{
//            response = request.execute();
//        } catch (EOFException e){
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.put("Connection", "close");
//            request.setHeaders(httpHeaders);
//            response = request.execute();
//        }
        //return response.parseAs(getResultType());

        //RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(createConverter()).build();
        //setService(adapter.create(getRetrofitedInterfaceClass()));

        try{
            return getService().getApkInfo(parameters);
        }catch (RetrofitError e){
            OauthErrorHandler.handle(e);
            throw e;
        }

    };

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    protected ArrayList<WebserviceOptions> getoptions(){
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();
        options.add(new WebserviceOptions("cmtlimit", "5"));
        options.add(new WebserviceOptions("payinfo", "true"));
        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));
        return options;
    }

    protected String buildOptions(ArrayList<WebserviceOptions> options){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");
        return sb.toString();
    }
}
