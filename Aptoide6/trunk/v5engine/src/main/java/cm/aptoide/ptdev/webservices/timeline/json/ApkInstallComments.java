package cm.aptoide.ptdev.webservices.timeline.json;



import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class ApkInstallComments extends GenericResponse {
    public List<Comment> getComments() {
        return comments;
    }
     List<Comment> comments;

    public static class Comment {
        public Number getId() {
            return id;
        }
        public String getUsername() {
            return username;
        }
        public String getText() {
            return text;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public boolean isOwned() {
            return owned;
        }

        Number id;

        String username;

        String text;

        String timestamp;

        boolean owned;

        public String getAvatar() {
            return avatar;
        }


        String avatar;
    }
}