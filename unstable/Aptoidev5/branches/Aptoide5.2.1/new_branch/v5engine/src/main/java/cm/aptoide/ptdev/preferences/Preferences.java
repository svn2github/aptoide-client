package cm.aptoide.ptdev.preferences;

import android.preference.PreferenceManager;

import cm.aptoide.ptdev.Aptoide;

/**
 * Created by asantos on 03-10-2014.
 */
public class Preferences {

    public static final String SHARE_TIMELINE_DOWNLOAD_BOOL = "STLD";
    public static final String TIMELINE_ACEPTED_BOOL = "TLA";

    public static final boolean getBoolean(String key, boolean defValue){
        return PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean(key,defValue);
    }
    public static final void putBooleanAndCommit(String key, boolean Value){
        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext())
                .edit().putBoolean(key, Value).commit();
    }
}
