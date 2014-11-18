package cm.aptoidetv.pt;

import cm.aptoide.ptdev.SuggestionProvider;

/**
 * Created by tdeus on 12/23/13.
 */
public class SuggestionProviderTV extends SuggestionProvider {

    public SuggestionProviderTV() {

        setupSuggestions("cm.aptoidetv.pt.SuggestionProviderTV", DATABASE_MODE_QUERIES);
    }
}
