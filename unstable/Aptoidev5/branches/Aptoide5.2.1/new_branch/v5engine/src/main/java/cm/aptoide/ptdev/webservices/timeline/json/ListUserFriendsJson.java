package cm.aptoide.ptdev.webservices.timeline.json;

import com.google.api.client.util.Key;

import java.util.ArrayList;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListUserFriendsJson extends GenericResponse {
    public ArrayList<Friend> getInactiveFriends() {

        if(userfriends!=null){
            return userfriends.timeline_inactive;
        }else{
            return new ArrayList<Friend>();
        }


    }
    public ArrayList<Friend> getActiveFriends() {

        if(userfriends!=null){
            return userfriends.timeline_active;
        }else{
            return new ArrayList<Friend>();
        }
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