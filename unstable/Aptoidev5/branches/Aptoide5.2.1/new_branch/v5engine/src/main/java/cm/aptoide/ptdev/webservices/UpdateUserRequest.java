package cm.aptoide.ptdev.webservices;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class UpdateUserRequest extends GoogleHttpClientSpiceRequest<CreateUserJson> {


    String baseUrl = "https://webservices.aptoide.com/webservices/createUser";

    private String name = "";
    private Context context;

    public UpdateUserRequest(Context context) {
        super(CreateUserJson.class);
        this.context = context;
    }

    @Override
    public CreateUserJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);
        AccountManager manager = AccountManager.get(context);
        Account account = manager.getAccountsByType(Aptoide.getConfiguration().getAccountType())[0];
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

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        HttpResponse response;
        try{
            response = request.execute();
        } catch (EOFException e){

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Connection", "close");
            request.setHeaders(httpHeaders);
            response = request.execute();
        }

        return response.parseAs(getResultType());    }

    public void setName(String name) {
        this.name = name;
    }
}
