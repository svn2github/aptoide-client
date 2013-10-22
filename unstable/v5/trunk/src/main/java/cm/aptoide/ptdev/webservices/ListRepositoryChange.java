package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryChange extends AbstractWebservice {

    ArrayList<String> args = new ArrayList<String>();
    private FutureCallback<JsonObject> callback;

    private String repo;
    private String hash;

    public ListRepositoryChange(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }


    @Override
    <T> Future<JsonObject> execute() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
