package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by asantos on 07-08-2014.
 */
public class GetApkInfoRequestFromVercode extends GetApkInfoRequest {

    public GetApkInfoRequestFromVercode(Context context) {
        super(context);
    }

    public void setVercode(long vercode) {
        this.vercode = vercode;
    }

    private long vercode;
    protected ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options){
        options.add(new WebserviceOptions("vercode", Long.toString(vercode)));
        return options;
    }
    protected HashMap<String, String > getParameters(){
        Log.d("Refactortest", "GetApkInfoRequestFromVercode");
        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("repo", repoName);
        parameters.put("apkid", packageName);
        parameters.put("apkversion", versionName);
        return parameters;
    }

    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception {
        versionName = URLEncoder.encode(versionName, "UTF-8");
        return super.loadDataFromNetwork();
    }
}

