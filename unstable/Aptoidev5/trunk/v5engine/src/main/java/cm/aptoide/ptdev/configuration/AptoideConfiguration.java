package cm.aptoide.ptdev.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MainActivity;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-09-2013
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class AptoideConfiguration {


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

    private static Context context = Aptoide.getContext();
    private static SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);

    private String webservicesPath;
    private static String MARKETNAME = "Aptoide";
    private static String BRAND;



    public static String getMarketName() {
        return MARKETNAME;
    }

    public static void setMARKETNAME(String MARKETNAME) {
        AptoideConfiguration.MARKETNAME = MARKETNAME;
    }

    public static String getBrand() {
        return BRAND;
    }

    public static void setBrand(String brand) {
        AptoideConfiguration.BRAND = brand;
    }

    public static Class getShortcutClass() {
        return MainActivity.class;
    }

    public String getAutoUpdateUrl() {
        return sPref.getString(PREF_AUTO_UPDATE_URL, Defaults.AUTO_UPDATE_URL);
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

        String cache = sPref.getString(PREF_PATH_CACHE, Defaults.PATH_CACHE);
        new File(cache).mkdirs();

        return cache;

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

//    public ActivitiesClasses getClasses(){
//        return new ActivitiesClasses();
//
//    }
//
//    public static class ActivitiesClasses {
//
//
//        public Class getAppViewActivity() {
//            return AppViewActivity;
//        }
//
//        private final Class AppViewActivity = cm.aptoide.ptdev.AppViewActivity.class;
//
//    }


}
