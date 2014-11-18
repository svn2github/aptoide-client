package cm.aptoide.ptdev.webservices.timeline;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserSettingsRequest extends TimelineRequest<GetUserSettingsJson> {
    public static final String TIMELINE = "timeline";
    ArrayList<String> list;

    public void addSetting(String setting) {
        list.add(setting);
    }

    public GetUserSettingsRequest() {
        super(GetUserSettingsJson.class);
        list = new ArrayList<String>();
    }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/getUserSettings";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("settings", TextUtils.join(",", list));
        return parameters;
    }
}
