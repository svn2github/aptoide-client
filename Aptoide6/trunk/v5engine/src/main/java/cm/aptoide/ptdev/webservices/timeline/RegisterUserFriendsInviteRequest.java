package cm.aptoide.ptdev.webservices.timeline;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;

/**
 * Created by asantos on 20-10-2014.
 */
public class RegisterUserFriendsInviteRequest extends TimelineRequest<GenericResponse>  {

    ArrayList<String> list;

    public void addEmail(String value) {
        list.add("f"+(list.size()+1)+"=" + value);
    }

    public interface RegisterUserFriendsInvite extends TimelineRequest.Webservice<GenericResponse>{

    }

    public RegisterUserFriendsInviteRequest() {
        super(GenericResponse.class, RegisterUserFriendsInvite.class );
        list= new ArrayList<String>();
    }
    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/registerUserFriendsInvite";
    }

    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("friends", TextUtils.join(";", list));
        return parameters;
    }
}
