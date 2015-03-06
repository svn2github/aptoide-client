package cm.aptoidetv.pt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.UUID;

public class MainActivity extends Activity implements RequestsTvListener {

    private static final int MINTIME_FOR_SPLASHSCREEN =3000;
    public static final String ARGS_SKIP ="SKIP";
    SplashDialogFragment d;
    long time;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Init();
        ThemePicker.setThemePicker(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(getIntent().getExtras()==null || getIntent().getExtras().containsKey(ARGS_SKIP)) {
            if (getResources().getBoolean(R.bool.showsplash)) {
                d = new SplashDialogFragment();
                time = System.currentTimeMillis();
                d.show(getFragmentManager(), "SSF");
            }
        }
        new AutoUpdate(this).execute();
    }

    private void Init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.contains(Preferences.APTOIDE_CLIENT_UUID)) {
            SharedPreferences.Editor e=prefs.edit();
            e.putString(Preferences.APTOIDE_CLIENT_UUID, UUID.randomUUID().toString());
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            e.putInt(Preferences.SCREEN_WIDTH, dm.widthPixels);
            e.putInt(Preferences.SCREEN_HEIGHT, dm.heightPixels);
            if (!prefs.contains("Rooted")) {
                boolean isRooted = isRooted();
                e.putBoolean("Rooted", isRooted);
            }
            e.apply();
        }
    }
    private static boolean isRooted() {
        return findBinary("su");
    }
    private static boolean findBinary(String binaryName) {
        boolean found = false;
        final String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;
                break;
            }
        }
        return found;
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    @Override
    public void onSuccess() {
        if(d==null)
            return;
        long timepassed = System.currentTimeMillis()-time;
        if (timepassed>= MINTIME_FOR_SPLASHSCREEN && d.isAdded()) {
            d.dismissAllowingStateLoss();
        }else{
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onSuccess();
                }
            }, MINTIME_FOR_SPLASHSCREEN -timepassed);
        }
    }

    @Override
    public void onFailure() {
        if (d.isAdded()){
            d.dismissAllowingStateLoss();
        }
        startActivity(new Intent(this, MainFail.class));
        finish();
    }
}
