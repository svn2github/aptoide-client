package cm.aptoide.ptdev.webservices.timeline;

import java.util.List;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by asantos on 25-09-2014.
 */
public class TOAdd {

    /**
     * Listener to be used on ChangeUserSettingsRequest and GetUserSettingsRequest
     */
    public class GetUserSettingsRequestListener extends TimelineRequestListener {
        @Override
        protected void caseOK(GenericResponse response) {
            if (((GetUserSettingsJson)response).getResults() != null) {
                boolean serverResponse = ((GetUserSettingsJson)response).getResults().getTimeline().equals("active");
                OnGetServerSetting(serverResponse);
            }
        }
    }

    public class ListAPKsInstallsRequestListener extends TimelineRequestListener {
        @Override
        protected void caseOK(GenericResponse response) {
            if (((TimelineListAPKsJson)response).getUsersapks()!=null &&
                    ((TimelineListAPKsJson)response).getUsersapks().getUsers_apks() !=null) {
                OnlistUserApkInstalls(((TimelineListAPKsJson) response).getUsersapks().getUsers_apks());
            }
        }
    }

    public class GetUserApkInstallCommentsRquestListener extends TimelineRequestListener {
        @Override
        protected void caseOK(GenericResponse response) {
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

    private void OnlistUserApkInstalls(List<TimelineListAPKsJson.UsersApks.Entry> users_apks) {
        //TODO
    }
    private void OnGetUserApkInstallComments(List<ApkInstallComments.Comment.Entry> entry) {
        //TODO
    }
}
