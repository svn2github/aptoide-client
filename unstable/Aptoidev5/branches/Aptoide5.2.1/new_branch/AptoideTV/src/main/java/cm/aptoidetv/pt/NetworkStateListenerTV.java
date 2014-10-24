package cm.aptoidetv.pt;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.NetworkStateListener;
import cm.aptoide.ptdev.database.Database;

/**
 * Created by rmateus on 25-03-2014.
 */
public class NetworkStateListenerTV extends NetworkStateListener {

    @Override
    public void loadEditorsChoice(Database database, String url, String countryCode) throws IOException {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);
        int code;
        try{
            code = request.execute().getStatusCode();
        }catch (HttpResponseException e){
            code = e.getStatusCode();
        }

        if (code != 200) {
            url = ((AptoideConfigurationTV) AptoideTV.getConfiguration()).getDefaultEditorsUrl();
        }


        super.loadEditorsChoice(database, url, countryCode);
    }

    @Override
    public void loadTopApps(Database database, String url) throws IOException {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();

        GenericUrl genericUrl = new GenericUrl(url);

        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);

        int code;
        try{
            code = request.execute().getStatusCode();
        }catch (HttpResponseException e){
            code = e.getStatusCode();
        }

        if (code != 200) {
            url = ((AptoideConfigurationTV) AptoideTV.getConfiguration()).getDefaultTopAppsUrl();
        }

        super.loadTopApps(database, url);
    }
}
