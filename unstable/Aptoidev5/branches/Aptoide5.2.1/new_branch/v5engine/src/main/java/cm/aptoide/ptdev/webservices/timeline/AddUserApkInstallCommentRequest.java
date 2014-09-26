package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 24-09-2014.
 */
public class AddUserApkInstallCommentRequest extends TimelineRequest<GenericResponse> {
        private long postID;
        private String comment;

        public void setPostId(long id){		this.postID = id;	}
        public void setComment(String comment) {    this.comment = comment; }

        public AddUserApkInstallCommentRequest() {     super(GenericResponse.class);   }

        @Override
        protected String getUrl() {
                return WebserviceOptions.WebServicesLink+"3/addUserApkInstallComment";
        }

        @Override
        protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
            parameters.put("id", String.valueOf(postID));
            parameters.put("text", comment);
            return parameters;
        }

}