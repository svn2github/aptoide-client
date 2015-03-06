package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;









import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfoRequestFromId extends GetApkInfoRequest {

    public GetApkInfoRequestFromId(Context context) {
        super(context);
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    private String appId;
    protected ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options){
        return options;
    }

    protected HashMap<String, String > getParameters(){
        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("identif", "id:" + appId);
        parameters.put("mode", "json");
        return parameters;
    }
}
