package cm.aptoide.pt;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 17-09-2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class RecentSearchProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "cm.aptoide.com.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentSearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
