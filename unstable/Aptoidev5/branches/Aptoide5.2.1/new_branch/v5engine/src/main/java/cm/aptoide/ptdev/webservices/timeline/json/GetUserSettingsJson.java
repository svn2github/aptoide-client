package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserSettingsJson extends GenericResponse {

    @Key
    private Setting settings;

    public static class Setting {
        @Key
        private String timeline;

        public String getTimeline() {	return timeline;	}
    }

    public Setting getResults() {
        return settings;
    }
}