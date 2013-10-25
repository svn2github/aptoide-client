package cm.aptoide.ptdev.webservices;

import android.widget.Toast;
import cm.aptoide.ptdev.model.Server;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
public class ListRepositoryChange extends AbstractWebservice {

    HashMap<String, String> args = new HashMap<String, String>();
    private FutureCallback<JsonObject> callback;

    private StringBuilder repo;
    private StringBuilder hash;

    public ListRepositoryChange(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }

    public ListRepositoryChange setCallback(FutureCallback<JsonObject> callback) {
        this.callback = callback;
        return this;
    }

    public ListRepositoryChange setRepos(List<Server> servers) {
        repo.append("");
        hash.append("");

        int remaining_repos = servers.size();
        for (Server server : servers) {
            repo.append(server.getName());
            hash.append(server.getHash());
            remaining_repos--;
            if (remaining_repos != 0) {
                repo.append(",");
                hash.append(",");
            }
        }


        return this;
    }

    @Override
    public Future<JsonObject> execute() {
        args.put("repo", repo.toString());
        args.put("hash", hash.toString());

        return getHttpClient().post(getWebservicePath() + "webservices/listRepositoryChange", args).setCallback(callback);
    }


}
