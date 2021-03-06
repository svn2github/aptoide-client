package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class ApkInstallComments extends GenericResponse {
    public List<Comment> getComments() {
        return comments;
    }
    @Key List<Comment> comments;

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
        @Key
        Number id;
        @Key
        String username;
        @Key
        String text;
        @Key
        String timestamp;
        @Key
        boolean owned;

        public String getAvatar() {
            return avatar;
        }

        @Key
        String avatar;
    }
}