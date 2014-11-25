package cm.aptoide.ptdev.webservices.timeline;

import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.ListapklikesJson;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by asantos on 24-09-2014.
 */
public class ListApksInstallsRequest extends TimelineRequest<TimelineListAPKsJson> {
    private String limit;
    private String offset_id;
    private boolean upwards = false;
    private long postId;


    public interface ListApksInstalls extends TimelineRequest.Webservice<TimelineListAPKsJson>{

    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
    public void setOffset_id(String offset_id) {
        this.offset_id = offset_id;
    }

    public ListApksInstallsRequest() {
        super(TimelineListAPKsJson.class, ListApksInstalls.class);
    }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/listUserApkInstalls";
    }
    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("limit", limit);
        parameters.put("offset_id", offset_id);
        //parameters.put("offset_dir", upwards ? "up":"");
        if(postId>0){
            parameters.put("post_id", String.valueOf(postId));
        }
        return parameters;
    }

    public void setUpwardsDirection() {
        upwards = true;
    }

    @Override
    public TimelineListAPKsJson loadDataFromNetwork() throws Exception {
        TimelineListAPKsJson returnJson = super.loadDataFromNetwork();

        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putLong("timelineTimestamp", DateTime.now(DateTimeZone.forID("UTC")).toDate().getTime()/1000).commit();

        return returnJson;
    }

    public void setDownwardsDirection() {
        upwards = false;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }
}
