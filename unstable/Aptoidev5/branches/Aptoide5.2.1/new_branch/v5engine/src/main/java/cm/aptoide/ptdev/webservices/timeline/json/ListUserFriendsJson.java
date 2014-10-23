package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import java.util.ArrayList;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserFriendsJson extends GenericResponse {
    public ArrayList<Friend> getInactiveFriends() {
        return userfriends.timeline_inactive;
    }
    public ArrayList<Friend> getActiveFriends() {
        return userfriends.timeline_active;
    }
    @Key
    Friends userfriends;

    public static class Friends{
        @Key
        ArrayList<Friend> timeline_inactive;
        @Key
        ArrayList<Friend> timeline_active;
    }


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