package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class CreateUserRequest extends GoogleHttpClientSpiceRequest<CreateUserJson> {


    String baseUrl = "http://webservices.aptoide.com/webservices/createUser";
    private String email;
    private String pass;
    private String name = "";

    public CreateUserRequest() {
        super(CreateUserJson.class);
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    @Override
    public CreateUserJson loadDataFromNetwork() throws Exception {

        GenericUrl url = new GenericUrl(baseUrl);

        HashMap<String, String > parameters = new HashMap<String, String>();
        String passhash = AptoideUtils.Algorithms.computeSHA1sum(pass);
        parameters.put("mode", "json");
        parameters.put("email", email);
        parameters.put("name", name);
        parameters.put("passhash", passhash);

        parameters.put("hmac", AptoideUtils.Algorithms.computeHmacSha1(email+passhash+name, "bazaar_hmac"));

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs( getResultType() );
    }

    public void setName(String name) {
        this.name = name;
    }
}
