package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 20-10-2014.
 */
public class registerUserFriendsInviteRequest extends TimelineRequest<GenericResponse>  {

    public registerUserFriendsInviteRequest() { super(GenericResponse.class);   }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/registerUserFriendsInvite";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        return null;
    }
}
