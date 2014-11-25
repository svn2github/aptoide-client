package cm.aptoide.ptdev.webservices;

import android.util.Log;

import cm.aptoide.ptdev.webservices.json.RelatedApkJson;
import cm.aptoide.ptdev.webservices.json.RepositoryCommentsJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryCommentsRequest extends RetrofitSpiceRequest<RepositoryCommentsJson, ListRepositoryCommentsRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/listRepositoryComments")
        @FormUrlEncoded
        RepositoryCommentsJson listRepositoryComments(@FieldMap HashMap<String, String> args);
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }

    private final String storeName;

    public ListRepositoryCommentsRequest(String storeName) {
        super(RepositoryCommentsJson.class, Webservice.class);
        this.storeName = storeName;
    }

    @Override
    public RepositoryCommentsJson loadDataFromNetwork() throws Exception {
        String baseUrl = WebserviceOptions.WebServicesLink + "listRepositoryComments";

//        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("repo", storeName);
        parameters.put("mode", "json");

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(createConverter()).build();
        setService(adapter.create(getRetrofitedInterfaceClass()));

        return getService().listRepositoryComments(parameters);

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setConnectTimeout(10000);
//        request.setReadTimeout(10000);
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs(getResultType());
    }

}
