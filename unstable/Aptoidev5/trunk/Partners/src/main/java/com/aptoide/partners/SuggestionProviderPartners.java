package com.aptoide.partners;

import cm.aptoide.ptdev.SuggestionProvider;

/**
 * Created by tdeus on 12/23/13.
 */
public class SuggestionProviderPartners extends SuggestionProvider {

    public SuggestionProviderPartners() {

        setupSuggestions("com.aptoide.partners.SuggestionProvider", DATABASE_MODE_QUERIES);
    }
}
