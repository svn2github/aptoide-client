package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserFriendsRequest extends TimelineRequest<ListUserFriendsJson> {
    private String limit;
    private String offset;

    public ListUserFriendsRequest() {
        super(ListUserFriendsJson.class);
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/listUserFriends";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("limit", limit);
        parameters.put("offset", offset);
        return parameters;
    }
}
