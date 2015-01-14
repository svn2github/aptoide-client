package cm.aptoidetv.pt;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by asantos on 19-11-2014.
 */
public class AppTV extends Application {
    static Context c;
    private static AptoideConfiguration configuration;
    private static boolean webInstallServiceRunning;

    public static Context getContext() {
        return c;
    }

    public static AptoideConfiguration getConfiguration() {
        return configuration;
    }

    private static Picasso picassoInstance;
    public static Picasso getPicasso() {
        return picassoInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        c=this;
        configuration= new AptoideConfiguration();
        Crashlytics.start(this);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(
                new OkHttpDownloader(
                        getDefaultCacheDir(Defaults.PATH_CACHE_ICONS), 100 * 1024 * 1024));
        picassoInstance = builder.build();
        /*final OkHttpClient okHttpClient = new OkHttpClient();
        try {
            okHttpClient.setCache(new Cache(
                    new File(Defaults.PATH_CACHE), 100 * 1024 * 1024));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("pois","SetUp Cache Failed!");
        }
        Log.d("pois","CahcePath: "+ okHttpClient.getCache().getDirectory().getPath());*/
    }

    private static File getDefaultCacheDir(String path) {
        File cache = new File(path);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }
    public static boolean isWebInstallServiceRunning() {
        return webInstallServiceRunning;
    }

    public static void setWebInstallServiceRunning(boolean webInstallServiceRunning) {
        AppTV.webInstallServiceRunning = webInstallServiceRunning;
    }
}
