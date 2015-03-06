package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import cm.aptoide.ptdev.webservices.json.RepositoryLikesJson;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import retrofit.http.GET;
import retrofit.http.Path;

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
public class ListRepositoryLikesRequest extends RetrofitSpiceRequest<RepositoryLikesJson, ListRepositoryLikesRequest.Webservice> {


    public interface Webservice{
        @GET("/webservices.aptoide.com/webservices/listRepositoryLikes/{repo}/json")
        RepositoryLikesJson getRepositoryLikes(@Path("repo") String repo);
    }

    private final String storeName;

    public ListRepositoryLikesRequest(String storeName) {
        super(RepositoryLikesJson.class, Webservice.class);
        this.storeName = storeName;
    }

    @Override
    public RepositoryLikesJson loadDataFromNetwork() throws Exception {
        String baseUrl = WebserviceOptions.WebServicesLink + "listRepositoryLikes";

//        GenericUrl url = new GenericUrl(baseUrl);

//        HashMap<String, String > parameters = new HashMap<String, String>();
//        parameters.put("repo", storeName);
//        parameters.put("mode", "json");

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setConnectTimeout(10000);
//        request.setReadTimeout(10000);
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs(getResultType());

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(new JacksonConverter()).build();

        adapter.create(getRetrofitedInterfaceClass());

        return getService().getRepositoryLikes(storeName);

    }

}
