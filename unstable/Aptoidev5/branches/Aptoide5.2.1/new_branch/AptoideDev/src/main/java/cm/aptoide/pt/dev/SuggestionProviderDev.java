package cm.aptoide.pt.dev;

import cm.aptoide.ptdev.SuggestionProvider;

/**
 * Created by tdeus on 12/23/13.
 */
public class SuggestionProviderDev extends SuggestionProvider {

    public SuggestionProviderDev() {

        setupSuggestions("cm.aptoide.pt.dev.SuggestionProvider", DATABASE_MODE_QUERIES);
    }
}
