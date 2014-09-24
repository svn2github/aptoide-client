package cm.aptoide.ptdev.webservices.timeline;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserSettingsRequestListener extends TimelineRequestListener {

    public GetUserSettingsRequestListener(TimelineCallback callBack) {
        super(callBack);
    }

    @Override
    protected void caseOK(GenericResponse response) {
        if (((GetUserSettingsJson)response).getResults() != null) {
            boolean serverResponse = ((GetUserSettingsJson)response).getResults().getTimeline().equals("active");
            if(callBack!=null)
                callBack.OnGetServerSetting(serverResponse);
        }
    }

}
