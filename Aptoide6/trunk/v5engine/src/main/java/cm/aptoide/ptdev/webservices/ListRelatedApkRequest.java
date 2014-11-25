package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class ListRelatedApkRequest extends RetrofitSpiceRequest<RelatedApkJson, ListRelatedApkRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/2/listRelatedApks")
        @FormUrlEncoded
        RelatedApkJson getRelatedApkJson(@FieldMap HashMap<String, String> args);
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }

    String baseUrl = WebserviceOptions.WebServicesLink + "2/listRelatedApks";
    private String repos;
    private int limit;
    private Context context;
    private String packageName;
    private int vercode;
    private String mode;

    public ListRelatedApkRequest(Context context) {
        super(RelatedApkJson.class, Webservice.class);
        this.context = context;
    }

    public void setRepos(String repos) {
        this.repos = repos;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public RelatedApkJson loadDataFromNetwork() throws Exception {
        ArrayList<WebserviceOptions> options = new ArrayList<WebserviceOptions>();

//        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("apkid", packageName);

        if(repos!=null)options.add(new WebserviceOptions("repo", repos));
        if(mode!=null)options.add(new WebserviceOptions("type", mode));
        options.add(new WebserviceOptions("limit", String.valueOf(limit)));
        options.add(new WebserviceOptions("vercode", String.valueOf(vercode)));
        options.add(new WebserviceOptions("q", AptoideUtils.filters(context)));
        options.add(new WebserviceOptions("lang", AptoideUtils.getMyCountryCode(context)));


        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for(WebserviceOptions option: options){
            sb.append(option);
            sb.append(";");
        }
        sb.append(")");

        parameters.put("options", sb.toString());


        RestAdapter adapter = new RestAdapter.Builder().setConverter(createConverter()).build();

        setService(adapter.create(getRetrofitedInterfaceClass()));


        return getService().getRelatedApkJson(parameters);
//
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        Log.d("Aptoide-ApkRelated", url.toString());
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVercode(int vercode) {
        this.vercode = vercode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
