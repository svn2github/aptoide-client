package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserFriendsJson extends GenericResponse {
    public List<Friend> getFriends() {
        return friends;
    }
    @Key
    List<Friend> friends;

    public static class Friend {
        public String getUsername() {
            return username;
        }
        public String getAvatar() {
            return avatar;
        }
        public String getEmail() {
            return email;
        }

        @Key
        String username;
        @Key
        String avatar;
        @Key
        String email;

    }
}