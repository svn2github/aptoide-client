package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.Comment;


import java.util.List;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AllCommentsJson {

    public List<Comment> getListing() {
        return listing;
    }


    List<Comment> listing;

}
