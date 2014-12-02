package cm.aptoide.ptdev.webservices.timeline;

import android.text.TextUtils;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 24-09-2014.
 */
public class ChangeUserSettingsRequest extends RetrofitSpiceRequest<GenericResponseV2, ChangeUserSettingsRequest.ChangeUserSettings> {
    public static final String TIMELINEACTIVE = "active";
    public static final String TIMELINEINACTIVE = "inactive ";

    ArrayList<String> list;
    public void addTimeLineSetting(String value) {
        list.add("timeline=" + value);
    }

    public interface ChangeUserSettings{
        @POST(WebserviceOptions.WebServicesLink+"3/changeUserSettings")
        @FormUrlEncoded
        public GenericResponseV2 run(@FieldMap HashMap<String, String> args);
    }

    public ChangeUserSettingsRequest() {
        super(GenericResponseV2.class, ChangeUserSettings.class );
        list = new ArrayList<String>();
    }


    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("settings", TextUtils.join(",", list));

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        return getService().run(parameters);
    }
}
