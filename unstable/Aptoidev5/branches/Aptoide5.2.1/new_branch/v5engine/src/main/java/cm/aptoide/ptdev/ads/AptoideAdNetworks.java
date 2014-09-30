package cm.aptoide.ptdev.ads;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.Date;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.EnumPreferences;

/**
 * Created by rmateus on 30-09-2014.
 */
public class AptoideAdNetworks {

    public static String parseAppiaString(Context context, String clickUrl) throws IOException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        String deviceId = android.provider.Settings.Secure.getString(Aptoide.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String aaId = AdvertisingIdClient.getAdvertisingIdInfo(Aptoide.getContext()).getId();
        String myid = PreferenceManager.getDefaultSharedPreferences(context).getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");

        clickUrl = clickUrl.replace("[USER_ANDROID_ID]", deviceId);
        clickUrl = clickUrl.replace("[USER_UDID]", myid);
        clickUrl = clickUrl.replace("[USER_AAID]", aaId);
        clickUrl = clickUrl.replace("[TIME_STAMP]", String.valueOf(new Date().getTime()));
        return clickUrl;

    }

}
