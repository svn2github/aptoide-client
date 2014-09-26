package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserApkInstallCommentsRequest extends TimelineRequest<ApkInstallComments> {

    private long postID;
    private int limit;
    private int offset;
    public void setPostID(long id) { this.postID = id; }
    public void setPostLimit(int limit) { this.limit = limit; }
    public void setPostOffSet(int offset) { this.offset = offset; }
    public GetUserApkInstallCommentsRequest() {    super(ApkInstallComments.class);    }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/getUserApkInstallComments";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("id", String.valueOf(postID));
        parameters.put("limit", String.valueOf(limit));
        parameters.put("offset", String.valueOf(offset));
        return parameters;
    }
}
