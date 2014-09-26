package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserSettingsRequest extends TimelineRequest<GenericResponse> {
    public static final String TIMELINE = "timeline";
    StringBuilder sb;

    public void addSetting(String setting) {
        sb.append(setting);
        sb.append(',');
    }

    public GetUserSettingsRequest() {
        super(GenericResponse.class);
        sb  = new StringBuilder();
    }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/getUserSettings";
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
