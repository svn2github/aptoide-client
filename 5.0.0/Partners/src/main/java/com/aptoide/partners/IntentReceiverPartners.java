package com.aptoide.partners;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.IntentReceiver;

import java.util.ArrayList;

/**
 * Created by rmateus on 24-03-2014.
 */
public class IntentReceiverPartners extends IntentReceiver {

    @Override
    public void startActivityWithRepo(ArrayList<String> repo) {
        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getMultistores()){
            super.startActivityWithRepo(repo);
        }else{
            finish();
        }
    }

    @Override
    public void startFromMyApp(long id) {

        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getSearchStores()){
            super.startFromMyApp(id);
        }

    }

    @Override
    public void startMarketIntent(String param) {

        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getSearchStores()){
            super.startMarketIntent(param);
        }else{
            finish();
        }

    }
}
