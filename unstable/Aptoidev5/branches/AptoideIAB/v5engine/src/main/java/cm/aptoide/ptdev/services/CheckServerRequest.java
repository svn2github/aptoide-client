package cm.aptoide.ptdev.services;

import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.octo.android.robospice.request.SpiceRequest;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 19-11-2013
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class CheckServerRequest extends SpiceRequest<Integer> {
    private final String url;
    private final Login login;


    public CheckServerRequest(String url, Login login) {
        super(Integer.class);
        this.url = url;
        this.login = login;

    }

    @Override
    public Integer loadDataFromNetwork() throws Exception {
        int responseCode;
        if(login!=null){
            responseCode = AptoideUtils.NetworkUtils.checkServerConnection(url, login.getUsername(), login.getPassword());
        }else{
            responseCode = AptoideUtils.NetworkUtils.checkServerConnection(url, null, null);
        }

        return responseCode;
    }
}
