package com.aptoide.partners;

import cm.aptoide.ptdev.SearchManager;

public class SearchManagerPartners extends SearchManager {

    @Override
    public boolean isSearchMoreVisible() {
        return false;/*if(((AptoideConfigurationPartners) Aptoide.getConfiguration()).getSearchStores()){
            return super.isSearchMoreVisible();
        }else{
            return false;
        }*/

    }

}
