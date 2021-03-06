package cm.aptoide.pt.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cm.aptoide.pt.ApplicationAptoide;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-09-2013
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class AptoideConfiguration {

    private static final AptoideConfiguration INSTANCE = new AptoideConfiguration();
    private static final String PREF_AUTO_UPDATE_URL = "dev_mode_auto_update_url";
    private static final String PREF_ALWAYS_UPDATE = "dev_mode_always_update";
    private static final String PREF_PATH_CACHE_ICONS = "dev_mode_path_cache_icons";
    private static final String PREF_PATH_CACHE_APK = "dev_mode_path_cache_apks";
    private static final String PREF_PATH_CACHE = "dev_mode_path_cache";
    private static final String PREF_URI_SEARCH = "dev_mode_uri_search";
    private static final String PREF_STORE_DOMAIN = "dev_mode_store_domain";
    private static final String PREF_WEBSERVICE_URL = "dev_mode_webservices_url";
    private static final String PREF_EDITORS_PATH = "dev_mode_editors_path";
    private static final String PREF_TOP_PATH = "dev_mode_top_path";
    private static final String PREF_DEFAULT_STORE = "dev_mode_featured_store";

    private static Context context = ApplicationAptoide.getContext();
    private static SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);

    private String webservicesPath;


    private AptoideConfiguration() {}

    public static AptoideConfiguration getInstance() {
        return INSTANCE;
    }

    public String getAutoUpdateUrl() {
        String url;

        if(ApplicationAptoide.PARTNERID!=null){
            url = String.format(Defaults.OEM_AUTO_UPDATE_URL, ApplicationAptoide.DEFAULTSTORENAME, ApplicationAptoide.DEFAULTSTORENAME);
        }else{
            url = Defaults.AUTO_UPDATE_URL;
        }

        return sPref.getString(PREF_AUTO_UPDATE_URL, url );
    }

    public void setAutoUpdateUrl(String autoUpdateUrl) {
        sPref.edit().putString(PREF_AUTO_UPDATE_URL, autoUpdateUrl).commit();
    }

    public String getWebServicesUri() {

        if(this.webservicesPath == null){
            this.webservicesPath = sPref.getString(PREF_WEBSERVICE_URL, Defaults.WEBSERVICES_URI);
        }

        return this.webservicesPath;
    }

    public void setWebservicesUri(String path) {
        this.webservicesPath = path;
        sPref.edit().putString(PREF_WEBSERVICE_URL, path).commit();
    }

    public String getDomainAptoideStore() {
        return sPref.getString(PREF_STORE_DOMAIN, Defaults.DOMAIN_APTOIDE_STORE);
    }

    public void setDomainStore(String path) {
        sPref.edit().putString(PREF_STORE_DOMAIN, path).commit();
    }

    public String getUriSearch() {
        return sPref.getString(PREF_URI_SEARCH, Defaults.URI_SEARCH_BAZAAR);

    }

    public void setUriSearch(String path) {
        sPref.edit().putString(PREF_URI_SEARCH, path).commit();
    }

    public String getPathCache() {
        return sPref.getString(PREF_PATH_CACHE, Defaults.PATH_CACHE);

    }

    public void setPathCache(String path) {
        sPref.edit().putString(PREF_PATH_CACHE, path).commit();
    }

    public String getPathCacheApks() {
        return sPref.getString(PREF_PATH_CACHE_APK, Defaults.PATH_CACHE_APKS);
    }

    public void setPathCacheApks(String path) {
        sPref.edit().putString(PREF_PATH_CACHE_APK, path).commit();
    }

    public String getPathCacheIcons() {
        return sPref.getString(PREF_PATH_CACHE_ICONS, Defaults.PATH_CACHE_ICONS);
    }

    public void setPathCacheIcons(String path) {
        sPref.edit().putString(PREF_PATH_CACHE_ICONS, path).commit();
    }

    public boolean isAlwaysUpdate() {
        return sPref.getBoolean(PREF_ALWAYS_UPDATE, Defaults.ALWAYS_UPDATE);
    }

    public void setAlwaysUpdate(boolean alwaysUpdate) {
        sPref.edit().putBoolean(PREF_ALWAYS_UPDATE, alwaysUpdate).commit();
    }


    public String getEditorsPath() {
        return sPref.getString(PREF_EDITORS_PATH, Defaults.EDITORS_PATH);
    }

    public void setEditorsPath(String editorsPath) {
        sPref.edit().putString(PREF_EDITORS_PATH, editorsPath).commit();
    }

    public String getTopPath() {
        return sPref.getString(PREF_TOP_PATH, Defaults.TOP_PATH);
    }

    public void setTopPath(String topPath) {
        sPref.edit().putString(PREF_TOP_PATH, topPath).commit();
    }

    public String getDefaultStore() {
        return sPref.getString(PREF_DEFAULT_STORE, Defaults.DEFAULT_STORE);
    }

    public void setDefaultStore(String store) {
        sPref.edit().putString(PREF_DEFAULT_STORE, store).commit();
    }

}
