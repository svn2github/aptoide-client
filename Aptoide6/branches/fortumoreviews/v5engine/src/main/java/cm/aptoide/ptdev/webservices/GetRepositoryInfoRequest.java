package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;









import java.io.EOFException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
//public class GetRepositoryInfoRequest extends GoogleHttpClientSpiceRequest<RepositoryInfoJson> {
//
//    private final String storeName;
//
//    public GetRepositoryInfoRequest(String storeName) {
//        super(RepositoryInfoJson.class);
//        this.storeName = storeName;
//    }
//
//    @Override
//    public RepositoryInfoJson loadDataFromNetwork() throws Exception {
//
//        String baseUrl = WebserviceOptions.WebServicesLink + "2/getRepositoryInfo";
//
//        GenericUrl url = new GenericUrl(baseUrl);
//
//        HashMap<String, String > parameters = new HashMap<String, String>();
//        parameters.put("repo", storeName);
//        parameters.put("mode", "json");
//
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setConnectTimeout(10000);
//        request.setReadTimeout(10000);
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//        HttpResponse response;
//        try{
//            response = request.execute();
//        } catch (EOFException e){
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.put("Connection", "close");
//            request.setHeaders(httpHeaders);
//            response = request.execute();
//        }
//
//        return response.parseAs(getResultType());
//    }
//
//}
