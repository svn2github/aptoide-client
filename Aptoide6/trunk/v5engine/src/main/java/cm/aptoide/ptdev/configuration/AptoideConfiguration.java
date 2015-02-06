package cm.aptoide.ptdev.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.SearchManager;
import cm.aptoide.ptdev.pushnotification.PushNotificationReceiver;
import openiab.IABPurchaseActivity;
import openiab.PaidAppPurchaseActivity;

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
    private Class<?> moreActivityClass;

    public boolean isSaveOldRepos(){
        return true;
    }

    public String getUpdatesSyncAdapterAuthority(){
        return Aptoide.getContext().getPackageName() + ".UpdatesProvider";
    }

    public String getSearchAuthority(){
        return Aptoide.getContext().getPackageName() + ".SuggestionProvider";
    }

    public String getAutoUpdatesSyncAdapterAuthority(){
        return Aptoide.getContext().getPackageName() + ".AutoUpdateProvider";
    }


    public String getTimelineActivitySyncAdapterAuthority(){
        return "cm.aptoide.pt.TimelineActivity";
    }


    public String getTimeLinePostsSyncAdapterAuthority(){
        return "cm.aptoide.pt.TimelinePosts";
    }

    public String getAccountType() { return AccountGeneral.ACCOUNT_TYPE;
    };

    public String getMarketName() {
        return MARKETNAME;
    }

    public void setMARKETNAME(String MARKETNAME) {
        AptoideConfiguration.MARKETNAME = MARKETNAME;
    }

    public String getBrand() {
        return BRAND;
    }

    public static void setBrand(String brand) {
        AptoideConfiguration.BRAND = brand;
    }

    public Class getShortcutClass() {
        return cm.aptoide.ptdev.Start.class;
    }
    public Class getNotificationsReceiver() {
        return PushNotificationReceiver.class;
    }


    public String getTopAppsUrl(){
        return "http://apps.store.aptoide.com/top.xml";
    }

    public String getEditorsUrl(){
        return "http://apps.store.aptoide.com/editors_more.xml";
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
        String path = sPref.getString(PREF_PATH_CACHE_APK, Defaults.PATH_CACHE_APKS);

        new File(path).mkdirs();

        return path;
    }

    public void setPathCacheApks(String path) {
        sPref.edit().putString(PREF_PATH_CACHE_APK, path).commit();
    }

    public String getPathCacheIcons() {

        String pathIcons = sPref.getString(PREF_PATH_CACHE_ICONS, Defaults.PATH_CACHE_ICONS);;

        new File(pathIcons).mkdirs();
        return pathIcons;
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

    public String getExtraId(){
        return "";
    }

    public int getIcon() {
        return R.drawable.icon_brand_aptoide;
    }

    public Class getStartActivityClass(){
        return cm.aptoide.ptdev.Start.class;
    }

    public Class getAppViewActivityClass(){
        return cm.aptoide.ptdev.AppViewActivity.class;
    }

    public Class getSettingsActivityClass() {
        return cm.aptoide.ptdev.Settings.class;
    }

    public Class getSignUpActivityClass() {
        return cm.aptoide.ptdev.SignUpActivity.class;
    }

    public Class<?> getSearchActivityClass() {
        return SearchManager.class;
    }

    public Class getMoreEditorsChoiceActivityClass() { return  cm.aptoide.ptdev.MoreEditorsChoiceActitivy.class;}

    public Class getIABPurchaseActivityClass(){
        return IABPurchaseActivity.class;
    }
    public Class getPaidAppPurchaseActivityClass(){
        return PaidAppPurchaseActivity.class;
    }

    public String getTrackUrl() {
        return "cm.aptoide.pt.PushNotificationTrackUrl";
    }

    public String getAction() {
        return "cm.aptoide.pt.PushNotification";
    }

    public String getActionFirstTime() {
        return "cm.aptoide.pt.PushNotificationFirstTime";
    }

    public Class<?> getMoreActivityClass() {
        return MoreActivity.class;
    }
}
