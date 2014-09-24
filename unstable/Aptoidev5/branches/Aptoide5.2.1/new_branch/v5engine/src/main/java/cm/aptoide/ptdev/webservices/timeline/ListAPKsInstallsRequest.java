package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by asantos on 24-09-2014.
 */
public class ListAPKsInstallsRequest extends TimelineRequest<TimelineListAPKsJson> {
    private String limit;
    private String offset_id;

    public void setLimit(String limit) {
        this.limit = limit;
    }
    public void setOffset_id(String offset_id) {
        this.offset_id = offset_id;
    }

    public ListAPKsInstallsRequest() {
        super(TimelineListAPKsJson.class);
    }

    @Override
    protected String GetURL() {
        return WebserviceOptions.WebServicesLink+"3/listUserApkInstalls";
    }
    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("limit", limit);
        parameters.put("offset", offset_id);
        return parameters;
    }
}
