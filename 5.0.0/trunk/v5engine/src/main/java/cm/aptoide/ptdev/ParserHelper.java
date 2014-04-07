package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.webservices.RepositoryChangeRequest;
import cm.aptoide.ptdev.webservices.json.RepositoryChangeJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by rmateus on 20-02-2014.
 */

public class ParserHelper {

    private ParserService service;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ParserService.MainServiceBinder)binder).getService();
            spiceManager.start(context);

            if (!storesToCheck.isEmpty()) {
                spiceManager.execute(request, (repos.toString() + hashes.toString()).hashCode(), DurationInMillis.ONE_HOUR, new RequestListener<RepositoryChangeJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        if (spiceManager.isStarted()) {
                            spiceManager.shouldStop();
                        }
                    }

                    @Override
                    public void onRequestSuccess(RepositoryChangeJson repositoryChangeJson) {

                        for (RepositoryChangeJson.Listing changes : repositoryChangeJson.listing) {
                            if (Boolean.parseBoolean(changes.getHasupdates())) {
//                                Toast.makeText(Start.this, changes.getRepo() + " has updates.", Toast.LENGTH_SHORT).show();
                                spiceManager.removeDataFromCache(RepositoryChangeJson.class);
                                final Store store = new Store();
                                Cursor c = database.getStore(storesIds.get(changes.getRepo()));
                                if (c.moveToFirst()) {
                                    store.setBaseUrl(c.getString(c.getColumnIndex("url")));
                                    store.setTopTimestamp(c.getLong(c.getColumnIndex("top_timestamp")));
                                    store.setLatestTimestamp(c.getLong(c.getColumnIndex("latest_timestamp")));
                                    store.setDelta(c.getString(c.getColumnIndex("hash")));
                                    store.setId(c.getLong(c.getColumnIndex("id_repo")));
                                    if (c.getString(c.getColumnIndex("username")) != null) {
                                        Login login = new Login();
                                        login.setUsername(c.getString(c.getColumnIndex("username")));
                                        login.setPassword(c.getString(c.getColumnIndex("password")));
                                        store.setLogin(login);
                                    }

                                }
                                c.close();
                                if (spiceManager.isStarted()) {
                                    spiceManager.shouldStop();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        service.setShowNotification(true);
                                        service.startParse(database, store, false);
                                    }
                                }).start();
                            }
                        }

                        context.unbindService(conn);

                    }
                });
            } else {
                if (spiceManager.isStarted()) {
                    spiceManager.shouldStop();
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private ArrayList<BasicNameValuePair> storesToCheck;
    private Context context;
    private Database database;
    private HashMap<String, Long> storesIds;
    private RepositoryChangeRequest request;
    private StringBuilder hashes;
    private StringBuilder repos;

    public void parse(final Context context) {

        Log.d("Aptoide-UpdateSync", "parse");
        this.context = context;

        database = new Database(Aptoide.getDb());
        Cursor c = database.getServers();

        storesToCheck = new ArrayList<BasicNameValuePair>();
        storesIds = new HashMap<String, Long>();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            storesToCheck.add(new BasicNameValuePair(c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("hash"))));
            storesIds.put(c.getString(c.getColumnIndex("name")), c.getLong(c.getColumnIndex("id_repo")));
        }

        c.close();

        repos = new StringBuilder();
        hashes = new StringBuilder();
        Iterator<?> it = storesToCheck.iterator();
        while (it.hasNext()) {
            BasicNameValuePair next = (BasicNameValuePair) it.next();
            repos.append(next.getName());
            hashes.append(next.getValue());

            if (it.hasNext()) {
                repos.append(",");
                hashes.append(",");
            }
        }

        request = new RepositoryChangeRequest();
        request.setRepos(repos.toString());
        request.setHashes(hashes.toString());
        context.bindService(new Intent(context, ParserService.class),conn,Context.BIND_AUTO_CREATE);

    }





}

