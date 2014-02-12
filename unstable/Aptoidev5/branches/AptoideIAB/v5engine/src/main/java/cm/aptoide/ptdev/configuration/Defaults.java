package cm.aptoide.ptdev.configuration;

import android.os.Environment;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-09-2013
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
public class Defaults {


    public static final String AUTO_UPDATE_URL = "http://imgs.aptoide.com/latest_version.xml";
    public static final String WEBSERVICES_URI = "http://webservices.aptoide.com/";
    public static final String DOMAIN_APTOIDE_STORE = ".store.aptoide.com/";
    public static final String URI_SEARCH_BAZAAR = "http://m.aptoide.com/searchview.php?search=";

    public static final String PATH_SDCARD       = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String PATH_CACHE        = PATH_SDCARD + "/.aptoide/";
    public static final String PATH_CACHE_APKS   = PATH_CACHE + "apks/";
    public static final String PATH_CACHE_ICONS  = "icons/";

    public static final boolean ALWAYS_UPDATE = false;
    public static final String EDITORS_PATH = "editors.xml";
    public static final String TOP_PATH = "top.xml";
    public static final String DEFAULT_STORE = "apps";
}