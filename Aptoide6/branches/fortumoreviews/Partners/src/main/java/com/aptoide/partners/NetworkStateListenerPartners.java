package com.aptoide.partners;

import cm.aptoide.ptdev.NetworkStateListener;

/**
 * Created by rmateus on 25-03-2014.
 */
public class NetworkStateListenerPartners extends NetworkStateListener {

//    @Override
//    public void loadEditorsChoice(Database database, String url, String countryCode) throws IOException {
//
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        GenericUrl genericUrl = new GenericUrl(url);
//        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);
//        int code;
//        try{
//            code = request.execute().getStatusCode();
//        }catch (HttpResponseException e){
//            code = e.getStatusCode();
//        }
//
//        if (code != 200) {
//            url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultEditorsUrl();
//        }
//
//
//        super.loadEditorsChoice(database, url, countryCode);
//    }
//
//    @Override
//    public void loadTopApps(Database database, String url) throws IOException {
//
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//
//        GenericUrl genericUrl = new GenericUrl(url);
//
//        HttpRequest request = transport.createRequestFactory().buildHeadRequest(genericUrl);
//
//        int code;
//        try{
//            code = request.execute().getStatusCode();
//        }catch (HttpResponseException e){
//            code = e.getStatusCode();
//        }
//
//        if (code != 200) {
//            url = ((AptoideConfigurationPartners) Aptoide.getConfiguration()).getDefaultTopAppsUrl();
//        }
//
//        super.loadTopApps(database, url);
//    }
}
