package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.utils.AptoideUtils;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 14:16
 * To change this template use File | Settings | File Templates.
 */
public class CheckUserCredentials extends AbstractWebservice {


    ArrayList<String> args = new ArrayList<String>();

    private FutureCallback<JsonObject> callback;

    private String username;
    private String password;


    public CheckUserCredentials(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }

    public void setCallback(FutureCallback<JsonObject> callback) {
        this.callback = callback;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Future<JsonObject> execute() {
        args.add(username);
        try {
            args.add(AptoideUtils.Algorithms.computeSHA1sum(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return getHttpClient().get(getWebservicePath() + "webservices/checkUserCredentials/", args).setCallback(callback);
    }


}
