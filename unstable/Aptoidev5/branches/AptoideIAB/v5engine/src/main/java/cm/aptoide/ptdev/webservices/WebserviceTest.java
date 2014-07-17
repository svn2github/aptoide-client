package cm.aptoide.ptdev.webservices;

import android.accounts.AccountManager;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import cm.aptoide.ptdev.webservices.json.OAuth;
import com.google.api.client.http.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;
import com.octo.android.robospice.retry.RetryPolicy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */
public class WebserviceTest extends GoogleHttpClientSpiceRequest<GetApkInfoJson> {


    public WebserviceTest() {
        super(GetApkInfoJson.class);
    }

    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception {

        final HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("access_token", "fec9bf02766152e2d1c8f95f431192bd011279e2");
        parameters.put("repo", "lopes-oem");
        parameters.put("mode", "json");
        parameters.put("apkid", "com.viber.voip");
        parameters.put("apkversion", "4.3.3.67");

        final HttpContent content = new UrlEncodedContent(parameters);
        final GenericUrl url = new GenericUrl("http://webservices.aptoide.com/webservices/3/getApkInfo");

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setNumberOfRetries(2);
        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));

        request.setParser(new JacksonFactory().createJsonObjectParser());
        HttpResponse response = request.execute();

        return response.parseAs( getResultType() );

    }
}
