package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;
import cm.aptoide.ptdev.webservices.timeline.json.ListapklikesJson;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserapklikesRequest extends TimelineRequest<ListapklikesJson> {
    private int limit;
    private long postID;

    public interface ListUserapklikes extends TimelineRequest.Webservice<ListapklikesJson>{

    }

    public ListUserapklikesRequest() {
        super(ListapklikesJson.class, ListUserapklikes.class);
    }
    public void setPostID(long id){		this.postID = id;	}
    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/listUserApkInstallLikes";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("post_id", String.valueOf(postID));
        parameters.put("limit", String.valueOf(limit));
        return parameters;
    }
}
