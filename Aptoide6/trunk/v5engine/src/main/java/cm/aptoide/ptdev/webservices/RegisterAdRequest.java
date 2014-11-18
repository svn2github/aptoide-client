package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by rmateus on 29-07-2014.
 */
public class RegisterAdRequest extends GoogleHttpClientSpiceRequest<GenericResponseV2> {




    public RegisterAdRequest(Context context) {
        super(GenericResponseV2.class);
        //this.context = context;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    String url;

    @Override
    public GenericResponseV2 loadDataFromNetwork() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();

        String oemid = Aptoide.getConfiguration().getExtraId();

        if(!TextUtils.isEmpty(oemid)){
            parameters.put("oemid", oemid);
        }
        GenericUrl url = new GenericUrl(this.url);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

}
