package cm.aptoide.ptdev.services;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;

/**
 * Created by rmateus on 30-12-2013.
 */
public class HttpClientSpiceService extends Jackson2GoogleHttpClientSpiceService {

    @Override
    public int getThreadCount() {
        return 4;
    }
    public static HttpRequestFactory createRequestFactory() {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

        return httpTransport.createRequestFactory();
    }


}
