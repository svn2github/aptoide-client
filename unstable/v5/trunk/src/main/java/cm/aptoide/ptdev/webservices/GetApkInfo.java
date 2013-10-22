package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 12:28
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfo extends AbstractWebservice{

    private FutureCallback<JsonObject> callback;

    ArrayList<String> args = new ArrayList<String>();

    private String repoName;
    private String packageName;
    private String versionName;



    GetApkInfo(String webservicePath) {
        setWebservicePath(webservicePath);
        setHttpClient(new IONGsonClient());
    }

    public void setCallback(FutureCallback<JsonObject> callback){
        this.callback = callback;
    }

    public void setRepoName(String repoName){
        this.repoName = repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }


    @Override
    public Future<JsonObject> execute() {

        args.add(repoName);
        args.add(packageName);
        args.add(versionName);

        return getHttpClient().get(getWebservicePath() + "webservices/getApkInfo/", args ).setCallback(callback);
    }
}
