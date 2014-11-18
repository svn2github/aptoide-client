package cm.aptoidetv.pt;

import android.os.Bundle;
import android.view.Menu;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.SearchManager;

/**
 * Created by tdeus on 3/21/14.
 */
public class SearchManagerTV extends SearchManager {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
    }

    @Override
    public boolean isSearchMoreVisible() {
        if(((AptoideConfigurationTV)AptoideTV.getConfiguration()).getSearchStores()){
            return super.isSearchMoreVisible();
        }else{
            return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
