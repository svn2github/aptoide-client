package cm.aptoide.ptdev;

import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.CheckServerRequest;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.GetRepositoryInfoRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryInfoJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 19-11-2013
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public class StoreGetter {

    public interface StorePasswordCallback {
        public void on401(String url);
    }

    private final ParserService service;
    private final MainActivity.DismissCallback dismissCallback;
    private final StorePasswordCallback storePasswordCallback;
    private final SpiceManager spiceManager;
    private final Database db;

    public StoreGetter(SpiceManager spiceManager, final Database db, MainActivity.DismissCallback callback, ParserService service, StorePasswordCallback storePasswordCallback) {
        this.dismissCallback = callback;
        this.service = service;
        this.storePasswordCallback = storePasswordCallback;
        this.spiceManager = spiceManager;
        this.db = db;
    }

    public SpiceRequest getRequest() {
        return request;
    }

    SpiceRequest request;

    public void getRepositoryInfoAndParse(String repoName, final String url, final Login login) {
        request = new GetRepositoryInfoRequest(repoName);
        Log.d("Aptoide-", url);
        Log.d("Aptoide-", repoName);


        spiceManager.execute( request, url.hashCode(), DurationInMillis.ONE_HOUR, new RequestListener<RepositoryInfoJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.d("Aptoide-", "failed");

                if (dismissCallback != null) dismissCallback.onDismiss("Unable to add store " + spiceException);
            }

            @Override
            public void onRequestSuccess(RepositoryInfoJson repositoryInfoJson) {
                String message = null;

                Log.d("Aptoide-", "success");

                if ("FAIL".equals(repositoryInfoJson.getStatus())) {

                    message = "Store doesn't exist.";

                } else {
                    final Store store = new Store();

                    store.setBaseUrl(url);
                    store.setName(repositoryInfoJson.getListing().getName());
                    store.setDownloads(repositoryInfoJson.getListing().getDownloads());
                    store.setAvatar(repositoryInfoJson.getListing().getAvatar());
                    store.setDescription(repositoryInfoJson.getListing().getDescription());
                    store.setTheme(repositoryInfoJson.getListing().getTheme());
                    store.setView(repositoryInfoJson.getListing().getView());
                    store.setItems(repositoryInfoJson.getListing().getItems());

                    service.startParse(db, store, login);

                }

                if (dismissCallback != null) dismissCallback.onDismiss(message);

            }
        });
    }

    public void get(String s, final Login login) {
        final String url = AptoideUtils.checkStoreUrl(s);


        final String repoName = AptoideUtils.RepoUtils.split(url);

        Log.d("Aptoide-", repoName);

        request = new CheckServerRequest(url, login);
        spiceManager.execute(request, new RequestListener<Integer>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                if (dismissCallback != null) dismissCallback.onDismiss("Unable to add store " + spiceException);
            }

            @Override
            public void onRequestSuccess(Integer integer) {
                Log.d("Aptoide-", String.valueOf(integer));
                switch (integer) {
                    case 401:
                        if (dismissCallback != null) dismissCallback.onDismiss(null);
                        storePasswordCallback.on401(url);
                        break;
                    case -1:
                        if (dismissCallback != null) dismissCallback.onDismiss("An error ocurred");
                        break;
                    default:

                        if (!url.endsWith(".store.aptoide.com/")) {

                            Store store = new Store();

                            store.setBaseUrl(url);
                            store.setName(url);

                            service.startParse(db, store, login);

                        } else {

                            getRepositoryInfoAndParse(repoName, url, login);

                        }
                        break;
                }
            }
        });

    }
}
