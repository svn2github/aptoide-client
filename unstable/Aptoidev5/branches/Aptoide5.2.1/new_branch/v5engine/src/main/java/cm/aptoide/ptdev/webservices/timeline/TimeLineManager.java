package cm.aptoide.ptdev.webservices.timeline;

/**
 * Created by asantos on 29-09-2014.
 */
public interface TimeLineManager {

    public void likePost(long id);
    public void unlikePost(long id);
    public void commentPost(long id,String comment);
    public void getComments(long id);
    public void openCommentsDialog(long id);
}
