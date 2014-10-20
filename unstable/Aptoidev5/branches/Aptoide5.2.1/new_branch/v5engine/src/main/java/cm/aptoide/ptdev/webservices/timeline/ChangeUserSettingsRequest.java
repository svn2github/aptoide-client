package cm.aptoide.ptdev.webservices.timeline;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;

/**
 * Created by asantos on 24-09-2014.
 */
public class ChangeUserSettingsRequest extends TimelineRequest<GenericResponseV2> {
    public static final String TIMELINEACTIVE = "active";
    public static final String TIMELINEINACTIVE = "inactive ";

    ArrayList<String> list;
    public void addTimeLineSetting(String value) {
        list.add("timeline=" + value);
    }

    public ChangeUserSettingsRequest() {
        super(GenericResponseV2.class);
        list = new ArrayList<String>();
    }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/changeUserSettings";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("settings", TextUtils.join(",", list));
        return parameters;
    }
}
