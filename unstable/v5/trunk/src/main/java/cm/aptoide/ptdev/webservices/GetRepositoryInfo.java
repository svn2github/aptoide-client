package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class GetRepositoryInfo extends AbstractWebservice {

    ArrayList<String> args = new ArrayList<String>();
    private FutureCallback<JsonObject> callback;

    private String repo;

    public GetRepositoryInfo(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }

    public GetRepositoryInfo setCallback(FutureCallback<JsonObject> callback) {
        this.callback = callback;
        return this;
    }

    public GetRepositoryInfo setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    @Override
    public Future<JsonObject> execute() {
        args.add(repo);

        return getHttpClient().get(getWebservicePath() + "webservices/getRepositoryInfo/", args).setCallback(callback);
    }
}
