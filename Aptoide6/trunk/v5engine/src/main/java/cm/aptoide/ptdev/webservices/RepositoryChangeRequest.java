package cm.aptoide.ptdev.webservices;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;


import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class RepositoryChangeRequest extends RetrofitSpiceRequest<RepositoryChangeJson, RepositoryChangeRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/listRepositoryChange")
        @FormUrlEncoded
        RepositoryChangeJson getRepositoryChange(@FieldMap HashMap<String, String> args);

    }

    String baseUrl = WebserviceOptions.WebServicesLink + "listRepositoryChange";
    private String repos;
    private String hashes;

    public RepositoryChangeRequest() {
        super(RepositoryChangeJson.class, Webservice.class);
    }

    public void setRepos(String repos){
        this.repos = repos;
    }

    public void setHashes(String hashes){
        this.hashes = hashes;
    }

    @Override
    public RepositoryChangeJson loadDataFromNetwork() throws Exception {

//        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();

        parameters.put("mode", "json");
        parameters.put("repo", repos);
        parameters.put("hash", hashes);



        return getService().getRepositoryChange(parameters);

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
    }
}
