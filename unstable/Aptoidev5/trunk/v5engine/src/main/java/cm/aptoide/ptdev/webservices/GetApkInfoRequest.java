package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfoRequest extends GoogleHttpClientSpiceRequest<GetApkInfoJson> {


    String baseUrl = "http://webservices.aptoide.com/webservices/getApkInfo/savou/com.gameloft.android.ANMP.GloftA7HM/1.0.6/json";


    public GetApkInfoRequest() {
        super(GetApkInfoJson.class);
    }

    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }
}
