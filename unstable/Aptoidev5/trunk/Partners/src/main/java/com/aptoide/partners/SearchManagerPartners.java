package com.aptoide.partners;

import android.view.View;
import android.widget.ListView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.SearchManager;

/**
 * Created by tdeus on 3/21/14.
 */
public class SearchManagerPartners extends SearchManager {

    @Override
    public void setFooterView(ListView lv, int res) {
        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getSearchStores()){
            super.setFooterView(lv, R.layout.partner_footer_search);
        }
    }

    @Override
    public boolean isSearchMoreVisible() {
        if(((AptoideConfigurationPartners)Aptoide.getConfiguration()).getSearchStores()){
            return super.isSearchMoreVisible();
        }else{
            return false;
        }

    }
}
