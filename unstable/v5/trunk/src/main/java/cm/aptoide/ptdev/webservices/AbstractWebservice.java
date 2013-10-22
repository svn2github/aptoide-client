package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractWebservice {

    private String webservicePath;

    public Client getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(Client httpClient) {
        this.httpClient = httpClient;
    }

    private Client httpClient;

    public void setWebservicePath(String webservicePath) {
        this.webservicePath = webservicePath;
    }

    public String getWebservicePath() {
        return webservicePath;
    }


    abstract <T> Future<JsonObject> execute();

}
