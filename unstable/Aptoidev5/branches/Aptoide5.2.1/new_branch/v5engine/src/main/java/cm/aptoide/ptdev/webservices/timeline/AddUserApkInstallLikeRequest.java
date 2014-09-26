package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;

/**
 * Created by asantos on 24-09-2014.
 */
public class AddUserApkInstallLikeRequest extends TimelineRequest<GetUserSettingsJson> {
    public static final String LIKE = "like";
    public static final String DISLIKE = "dislike";
    public static final String UNLIKE = "unlike";
    private long postID;
    private String like;

    public void setPostId(long postID) {	this.postID = postID;	}
    public void setLike(String s) {	this.like = s;	}

    public AddUserApkInstallLikeRequest() { super(GetUserSettingsJson.class);   }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/addUserApkInstallLike";
    }
    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("id", String.valueOf(postID));
        parameters.put("like", like);
        return parameters;
    }

}
