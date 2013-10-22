package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryComments extends AbstractWebservice {

    ArrayList<String> args = new ArrayList<String>();
    private FutureCallback<JsonObject> callback;

    private String repo;
    private int limit;
    private int offset;

    public ListRepositoryComments(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }

    public ListRepositoryComments setCallback(FutureCallback<JsonObject> callback) {
        this.callback = callback;
        return this;
    }

    public ListRepositoryComments setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public ListRepositoryComments setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public ListRepositoryComments setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Future<JsonObject> execute() {
        args.add(repo);
        if(limit > 0 && offset > 0) {
            args.add(String.valueOf(limit));
            args.add(String.valueOf(offset));
        }

        return getHttpClient().get(getWebservicePath() + "webservices/listRepositoryComments/", args).setCallback(callback);
    }
}
