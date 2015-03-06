package cm.aptoide.ptdev.webservices.timeline.json;



import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserSettingsJson extends GenericResponse {


    public Setting settings;

    public static class Setting {

        public String timeline;

        public String getTimeline() {	return timeline;	}
    }

    public Setting getResults() {
        return settings;
    }
}