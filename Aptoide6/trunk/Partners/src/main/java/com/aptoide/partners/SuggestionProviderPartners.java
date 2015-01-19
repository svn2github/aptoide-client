package com.aptoide.partners;

import cm.aptoide.ptdev.SuggestionProvider;

/**
 * Created by tdeus on 12/23/13.
 */
public class SuggestionProviderPartners extends SuggestionProvider {

    public SuggestionProviderPartners() {}

    @Override
    public boolean onCreate() {
        setupSuggestions(getContext().getPackageName() + ".SuggestionProvider", DATABASE_MODE_QUERIES);
        isConfigured = true;
        return super.onCreate();
    }
}
