package cm.aptoide.ptdev.webservices.timeline;

import android.support.v7.app.ActionBarActivity;

import com.octo.android.robospice.SpiceManager;

import java.util.List;

import cm.aptoide.ptdev.dialogs.TimeLineCommentsDialog;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;

/**
 * Created by asantos on 25-09-2014.
 */
public class TOAdd extends ActionBarActivity implements TimeLineManager{

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);


    private static final int COMMENTSLIMIT = 10;
    private static final String COMMENTSDIALOGTAG = "CD";


    /* *************** Methods of the TimeLineManager Interface *************** */

    @Override
    public void likePost(long id){
        likeRequestPost(id,AddUserApkInstallLikeRequest.LIKE);
    }
    @Override
    public void unlikePost(long id){
        likeRequestPost(id,AddUserApkInstallLikeRequest.UNLIKE);
    }
    private void likeRequestPost(long id,String like){
        AddUserApkInstallLikeRequest request = new AddUserApkInstallLikeRequest();
        request.setLike(like);
        request.setPostId(id);
        manager.execute(request, new NothingRequestListener<GenericResponse>());
    }
    @Override
    public void commentPost(long id,String comment){
        AddUserApkInstallCommentRequest request = new AddUserApkInstallCommentRequest();
        request.setPostId(id);
        request.setComment(comment);
        manager.execute(request,new NothingRequestListener<GenericResponse>());

    }
    @Override
    public void getComment(long id) {
        GetUserApkInstallCommentsRequest request = new GetUserApkInstallCommentsRequest();
        request.setPostID(id);
        request.setPostLimit(COMMENTSLIMIT);
        manager.execute(request,new GetUserApkInstallCommentsRequestListener());
    }
    @Override
    public void openCommentsDialog(long id){
        new TimeLineCommentsDialog().show(getSupportFragmentManager(), COMMENTSDIALOGTAG);
    }

    /* *************** Methods of the TimeLineManager Interface *************** */



    public class NothingRequestListener<E> extends TimelineRequestListener<E> {
        @Override
        protected void caseOK(E response) {

        }
    }

    /**
     * Listener to be used on ChangeUserSettingsRequest and GetUserSettingsRequest
     */
    public class GetUserSettingsRequestListener extends TimelineRequestListener<GetUserSettingsJson> {
        @Override
        protected void caseOK(GetUserSettingsJson response) {
            if (((GetUserSettingsJson)response).getResults() != null) {
                boolean serverResponse = ((GetUserSettingsJson)response).getResults().getTimeline().equals("active");
                OnGetServerSetting(serverResponse);
            }
        }
    }

    public class GetUserApkInstallCommentsRequestListener extends TimelineRequestListener<ApkInstallComments> {
        @Override
        protected void caseOK(ApkInstallComments response) {
            if (((ApkInstallComments)response).getComment()!=null &&
                    ((ApkInstallComments)response).getComment().getEntry() !=null) {
                OnGetUserApkInstallComments(((ApkInstallComments) response).getComment().getEntry());
            }
        }
    }

    /**
     * Called be the listener of ChangeUserSettingsRequest and GetUserSettingsRequest
     */
    private void OnGetServerSetting(boolean timeline){
        //TODO
    }

    private void OnGetUserApkInstallComments(List<ApkInstallComments.Comments.Comment> entry) {
        ((TimeLineCommentsDialog) getSupportFragmentManager().findFragmentByTag(COMMENTSDIALOGTAG))
                .SetComments(entry);
    }
}
