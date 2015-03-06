package cm.aptoide.ptdev.webservices.timeline;

import android.text.TextUtils;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.OauthErrorHandler;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 20-10-2014.
 */
public class RegisterUserFriendsInviteRequest  extends RetrofitSpiceRequest<GenericResponse, RegisterUserFriendsInviteRequest.RegisterUserFriendsInvite> {

    ArrayList<String> list;

    public void addEmail(String value) {
        list.add("f"+(list.size()+1)+"=" + value);
    }

    public interface RegisterUserFriendsInvite{
        @POST(WebserviceOptions.WebServicesLink+"3/registerUserFriendsInvite")
        @FormUrlEncoded
        public GenericResponse run(@FieldMap HashMap<String, String> args);
    }

    public RegisterUserFriendsInviteRequest() {
        super(GenericResponse.class, RegisterUserFriendsInvite.class );
        list= new ArrayList<String>();
    }

    @Override
    public GenericResponse loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("friends", TextUtils.join(";", list));

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        try{
            return getService().run(parameters);
        }catch (RetrofitError e){
            OauthErrorHandler.handle(e);
        }

        return getService().run(parameters);

    }
}
