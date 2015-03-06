package com.aptoide.partners;

import cm.aptoide.ptdev.SuggestionProvider;

/**
 * Created by tdeus on 12/23/13.
 */
public class SuggestionProviderPartners extends SuggestionProvider {

    @Override
    public String getSearchProvider() {
        return getContext().getPackageName() + ".SuggestionProvider";
    }

}
