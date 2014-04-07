package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.Comment;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 27-12-2013.
 */
public class AllCommentsJson {

    public List<Comment> getListing() {
        return listing;
    }

    @Key
    List<Comment> listing;

}
