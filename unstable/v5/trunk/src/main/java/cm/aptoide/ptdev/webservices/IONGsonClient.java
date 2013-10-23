package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.Aptoide;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public class IONGsonClient extends Client {

    @Override
    <T> ResponseFuture<JsonObject> get(String url, List<String> arguments) {

        if(arguments.isEmpty()){
            return Ion.with(Aptoide.getContext(), url).asJsonObject();
        }

        StringBuilder sb = new StringBuilder();
        for(String arg: arguments){
            sb.append(arg).append("/");
        }

        sb.append("json");

        String finalUrl = url + sb.toString();

        Builders.Any.B builder = Ion.with(Aptoide.getContext(), finalUrl);


        return builder.asJsonObject();
    }

    @Override
    <T> ResponseFuture<JsonObject> post(String url, HashMap<String, String> arguments) {

        Builders.Any.B builder = Ion.with(Aptoide.getContext(), url);

        for(String key: arguments.keySet()){
            builder.setBodyParameter(key, arguments.get(key));
        }

        builder.setBodyParameter("mode", "json");

        return builder.asJsonObject();
    }
}
