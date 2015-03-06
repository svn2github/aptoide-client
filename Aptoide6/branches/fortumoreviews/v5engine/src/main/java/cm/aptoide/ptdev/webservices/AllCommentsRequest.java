package cm.aptoide.ptdev.webservices;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import cm.aptoide.ptdev.webservices.json.AllCommentsJson;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AllCommentsRequest extends RetrofitSpiceRequest<AllCommentsJson, AllCommentsRequest.WebService> {

    public interface WebService{
        @GET("/webservices.aptoide.com/webservices/2/listApkComments/{repo}/{apkid}/{apkversion}/json")
        AllCommentsJson getAllComments(@Path("repo") String repo, @Path("apkid") String apkid, @Path("apkversion") String apkversion );
    }

    String baseUrl = WebserviceOptions.WebServicesLink + "2/listApkComments";
    private String repoName;
    private String packageName;
    private String versionName;

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public AllCommentsRequest() {
        super(AllCommentsJson.class, WebService.class);
    }

    @Override
    public AllCommentsJson loadDataFromNetwork() throws Exception {
        //AllCommentsRequest.WebService adapter = new RestAdapter.Builder().setEndpoint("http://").build().create(getRetrofitedInterfaceClass());
        //setService(adapter);
        //GenericUrl url = new GenericUrl(baseUrl);

//        HashMap<String, String > parameters = new HashMap<String, String>();
//        parameters.put("repo", repoName);
//        parameters.put("apkid", packageName);
//        parameters.put("apkversion", versionName);
//        parameters.put("mode", "json");

//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
        // return request.execute().parseAs(getResultType());
        return getService().getAllComments(repoName, packageName, versionName);
    }
}
