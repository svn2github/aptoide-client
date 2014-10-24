package cm.aptoide.pt.dev;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideDev extends Aptoide {

    @Override
    public AptoideConfigurationDev getAptoideConfiguration() {
        return new AptoideConfigurationDev();
    }

    @Override
    public AptoideThemePicker getNewThemePicker() {
        return super.getNewThemePicker();
    }
}
