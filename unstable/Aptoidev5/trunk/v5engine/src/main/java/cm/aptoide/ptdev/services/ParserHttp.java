package cm.aptoide.ptdev.services;

import android.app.Application;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileBigInputStreamObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-11-2013
 * Time: 10:23
 * To change this template use File | Settings | File Templates.
 */
public class ParserHttp extends SpiceService {

    protected HttpRequestFactory httpRequestFactory;


    @Override
    public void onCreate() {
        super.onCreate();
        httpRequestFactory = createRequestFactory();

    }

    @Override
    public int getThreadCount() {
        return 2;
    }


    @Override
    public int getThreadPriority() {
        return Thread.MIN_PRIORITY;
    }

    public static HttpRequestFactory createRequestFactory() {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

        return httpTransport.createRequestFactory();
    }

    @Override
    public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof GoogleHttpClientSpiceRequest) {
            ((GoogleHttpClientSpiceRequest<?>) request.getSpiceRequest()).setHttpRequestFactory(httpRequestFactory);
        }
        super.addRequest(request, listRequestListener);
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {

        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new InFileBigInputStreamObjectPersister(application));

        return cacheManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Aptoide-ParserHttp", "onDestroy");

    }
}
