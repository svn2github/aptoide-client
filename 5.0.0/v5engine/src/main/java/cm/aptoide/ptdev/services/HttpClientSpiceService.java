package cm.aptoide.ptdev.services;

import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;

/**
 * Created by rmateus on 30-12-2013.
 */
public class HttpClientSpiceService extends Jackson2GoogleHttpClientSpiceService {

    @Override
    public int getThreadCount() {
        return 4;
    }



}
