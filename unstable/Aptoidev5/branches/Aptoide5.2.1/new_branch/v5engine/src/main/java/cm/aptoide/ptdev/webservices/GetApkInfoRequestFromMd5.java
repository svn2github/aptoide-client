package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.util.Log;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.io.EOFException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfoRequestFromMd5 extends GetApkInfoRequest {

    public GetApkInfoRequestFromMd5(Context context) {
        super(context);
    }

    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    private String md5Sum;
    protected ArrayList<WebserviceOptions> fillWithExtraOptions(ArrayList<WebserviceOptions> options){
        return options;
    }

    protected HashMap<String, String > getParameters(){
        Log.d("Refactortest","GetApkInfoRequestFromMd5");
        HashMap<String, String > parameters = new HashMap<String, String>();
        if(repoName != null) {
            parameters.put("repo", repoName);
        }
        parameters.put("identif", "md5sum:" + md5Sum);
        return parameters;
    }
}
