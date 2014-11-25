package cm.aptoide.ptdev.services;



import cm.aptoide.ptdev.webservices.HttpService;

/**
 * Created by rmateus on 30-12-2013.
 */
public class HttpClientSpiceService extends HttpService {

    @Override
    public int getThreadCount() {
        return 4;
    }



}
