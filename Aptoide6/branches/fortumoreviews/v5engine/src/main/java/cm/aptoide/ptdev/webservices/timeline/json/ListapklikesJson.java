package cm.aptoide.ptdev.webservices.timeline.json;



import java.util.ArrayList;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 08-10-2014.
 */
public class ListapklikesJson extends GenericResponse {
    public ArrayList<Friend> getUsersapks_likes() {
        return usersapks_likes;
    }


        ArrayList<Friend> usersapks_likes;


}