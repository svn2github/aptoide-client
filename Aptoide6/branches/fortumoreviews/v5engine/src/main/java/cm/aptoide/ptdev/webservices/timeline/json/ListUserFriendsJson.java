package cm.aptoide.ptdev.webservices.timeline.json;



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

    Friends userfriends;

    public static class Friends{
             ArrayList<Friend> timeline_inactive;

        ArrayList<Friend> timeline_active;
    }
}