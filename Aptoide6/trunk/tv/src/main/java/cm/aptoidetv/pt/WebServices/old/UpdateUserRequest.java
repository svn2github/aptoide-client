package cm.aptoidetv.pt.WebServices.old;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;
import java.util.Locale;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.WebServices.old.json.GenericResponseV2;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class UpdateUserRequest extends RetrofitSpiceRequest<GenericResponseV2, UpdateUserRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/createUser")
        @FormUrlEncoded
        GenericResponseV2 updateUser(@FieldMap HashMap<String, String> args);
    }

    String baseUrl = WebserviceOptions.WebServicesLink + "createUser";

    private String name = "";
    private Context context;

    public UpdateUserRequest(Context context) {
        super(GenericResponseV2.class, Webservice.class);
        this.context = context;
    }

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {

        //GenericUrl url = new GenericUrl(baseUrl);
        AccountManager manager = AccountManager.get(context);
        Account account = manager.getAccountsByType(AppTV.getConfiguration().getAccountType())[0];
        String email = account.name.toLowerCase(Locale.ENGLISH);
        String pass = manager.getPassword(account);

        HashMap<String, String > parameters = new HashMap<String, String>();
        String passhash = AptoideUtils.Algorithms.computeSHA1sum(pass);
        parameters.put("mode", "json");
        parameters.put("email", email);
        parameters.put("name", name);
        parameters.put("update", "true");
        parameters.put("passhash", passhash);

        parameters.put("hmac", AptoideUtils.Algorithms.computeHmacSha1(email+passhash+name+"true", "bazaar_hmac"));
        return getService().updateUser(parameters);
    }

    public void setName(String name) {
        this.name = name;
    }
}
