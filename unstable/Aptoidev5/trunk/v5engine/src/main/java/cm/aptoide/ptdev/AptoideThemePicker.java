package cm.aptoide.ptdev;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.prefs.Preferences;

public class AptoideThemePicker {

	public void setAptoideTheme(Context activity) {


        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(activity);


        if(sPref.getString("theme", "light").equals("dark")){
            activity.setTheme(R.style.AptoideThemeDefaultDark);
        }else{
            activity.setTheme(R.style.AptoideThemeDefault);
        }




    }

}
