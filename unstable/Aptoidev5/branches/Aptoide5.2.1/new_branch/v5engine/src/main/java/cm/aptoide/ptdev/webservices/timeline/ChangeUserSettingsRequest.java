package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 24-09-2014.
 */
public class ChangeUserSettingsRequest extends TimelineRequest<GenericResponse> {
    public static final String TIMELINEACTIVE = "active";
    public static final String TIMELINEINACTIVE = "inactive ";

    StringBuilder sb;

    public void addTimeLineSetting(String value) {
        sb.append("timeline="+value);
        sb.append(',');
    }

    public ChangeUserSettingsRequest() {
        super(GenericResponse.class);
        sb  = new StringBuilder();
    }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/changeUserSettings";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        if(sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            parameters.put("settings",sb.toString());
        }
        return parameters;
    }
}
