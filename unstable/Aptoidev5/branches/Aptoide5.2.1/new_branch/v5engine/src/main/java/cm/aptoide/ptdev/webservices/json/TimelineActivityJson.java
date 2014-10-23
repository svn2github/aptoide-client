package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 23-10-2014.
 */
public class TimelineActivityJson {
    @Key
    public String status;
    @Key
    public ActivityGroup new_installs;
    @Key
    public ActivityGroup owned_activity;
    @Key
    public ActivityGroup related_activity;

    public String getStatus() {
        return status;
    }

    public ActivityGroup getNew_installs() {
        return new_installs;
    }

    public ActivityGroup getOwned_activity() {
        return owned_activity;
    }

    public ActivityGroup getRelated_activity() {
        return related_activity;
    }

    public static class ActivityGroup{

        @Key
        public List<Friend> friends;
        @Key
        public Number total;
        @Key
        public Number total_likes;
        @Key
        public Number total_comments;

        public List<Friend> getFriends() {
            return friends;
        }

        public Number getTotal() {
            return total;
        }

        public Number getTotal_likes() {
            return total_likes;
        }

        public Number getTotal_comments() {
            return total_comments;
        }
    }


    public static class Friend{
        @Key
        public String avatar;

        public String getAvatar() {
            return avatar;
        }
    }

}
