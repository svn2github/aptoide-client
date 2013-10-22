
package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 22-10-2013
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class ListApkComments extends AbstractWebservice {

    ArrayList<String> args = new ArrayList<String>();
    private FutureCallback<JsonObject> callback;

    private String repo;
    private String apkid;
    private String apkversion;
    private String lang;

    public ListApkComments(String webServicePath) {
        setWebservicePath(webServicePath);
        setHttpClient(new IONGsonClient());
    }

    public ListApkComments setCallback(FutureCallback<JsonObject> callback) {
        this.callback = callback;
        return this;
    }

    public ListApkComments setRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public ListApkComments setApkid(String apkid) {
        this.apkid = apkid;
        return this;
    }

    public ListApkComments setApkversion(String apkversion) {
        this.apkversion = apkversion;
        return this;
    }

    public ListApkComments setLang(String lang) {
        this.lang = lang;
        return this;
    }

    @Override
    public Future<JsonObject> execute() {
        args.add(repo);
        args.add(apkid);
        args.add(apkversion);
        if(lang != null) {
            args.add(lang);
        }

        return getHttpClient().get(getWebservicePath() + "webservices/listApkComments/", args).setCallback(callback);
    }
}
