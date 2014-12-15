package cm.aptoide.ptdev.ads;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.Date;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.EnumPreferences;

/**
 * Created by rmateus on 30-09-2014.
 */
public class AptoideAdNetworks {

    public static String parseString(String type, Context context, String clickUrl) throws IOException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        if(type.equalsIgnoreCase("appia")){
            String deviceId = android.provider.Settings.Secure.getString(Aptoide.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            String myid = PreferenceManager.getDefaultSharedPreferences(context).getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
            clickUrl = clickUrl.replace("[USER_ANDROID_ID]", deviceId);
            clickUrl = clickUrl.replace("[USER_UDID]", myid);
            clickUrl = replaceAdvertisementId(clickUrl);
            clickUrl = clickUrl.replace("[TIME_STAMP]", String.valueOf(new Date().getTime()));
        }else if(type.equalsIgnoreCase("glispa")){
            //do nothing;
        }else if(type.equalsIgnoreCase("taptica")){
            clickUrl = replaceAdvertisementId(clickUrl);
        }else if(type.equalsIgnoreCase("instal")){
            //do nothing;
        }else if(type.equalsIgnoreCase("woobi")){

        }

        return clickUrl;
    }

    private static String replaceAdvertisementId(String clickUrl) throws IOException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(Aptoide.getContext())==0){
            String aaId = AdvertisingIdClient.getAdvertisingIdInfo(Aptoide.getContext()).getId();
            clickUrl = clickUrl.replace("[USER_AAID]", aaId);
        }
        return clickUrl;
    }
}
