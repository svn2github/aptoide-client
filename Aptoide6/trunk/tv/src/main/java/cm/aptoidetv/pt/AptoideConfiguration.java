package cm.aptoidetv.pt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-09-2013
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class AptoideConfiguration {

    private static final String ACCOUNT_TYPE = "cm.aptoidetv.pt";
    private static final String PREF_PATH_CACHE = "dev_mode_path_cache";

    /*
    private static final String PREF_AUTO_UPDATE_URL = "dev_mode_auto_update_url";
    private static final String PREF_ALWAYS_UPDATE = "dev_mode_always_update";
    private static final String PREF_PATH_CACHE_ICONS = "dev_mode_path_cache_icons";
    private static final String PREF_PATH_CACHE_APK = "dev_mode_path_cache_apks";

    private static final String PREF_URI_SEARCH = "dev_mode_uri_search";
    private static final String PREF_STORE_DOMAIN = "dev_mode_store_domain";
    private static final String PREF_WEBSERVICE_URL = "dev_mode_webservices_url";
    private static final String PREF_EDITORS_PATH = "dev_mode_editors_path";
    private static final String PREF_TOP_PATH = "dev_mode_top_path";
    private static final String PREF_DEFAULT_STORE = "dev_mode_featured_store";
*/
    private static Context context = AppTV.getContext();
    private static SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);

    private static String MARKETNAME = "Aptoide";

    public String getUpdatesSyncAdapterAuthority(){
        return AppTV.getContext().getPackageName() + ".UpdatesProvider";
    }

    public String getSearchAuthority(){
        return AppTV.getContext().getPackageName() + ".SuggestionProvider";
    }

    public String getAutoUpdatesSyncAdapterAuthority(){
        return AppTV.getContext().getPackageName() + ".AutoUpdateProvider";
    }


    public String getAccountType() { return ACCOUNT_TYPE;
    }

    public String getMarketName() {
        return MARKETNAME;
    }

    public void setMARKETNAME(String MARKETNAME) {
        AptoideConfiguration.MARKETNAME = MARKETNAME;
    }

    public String getPathCache() {

        String cache = sPref.getString(PREF_PATH_CACHE, Defaults.PATH_CACHE);
        new File(cache).mkdirs();

        return cache;

    }

    public void setPathCache(String path) {
        sPref.edit().putString(PREF_PATH_CACHE, path).commit();
    }

    public String getExtraId(){
        return "";
    }

    public int getIcon() {
        return R.drawable.ic_launcher;
    }


}
