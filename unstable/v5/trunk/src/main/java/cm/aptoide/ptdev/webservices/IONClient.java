package cm.aptoide.ptdev.webservices;

import cm.aptoide.ptdev.Aptoide;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
public class IONClient extends Client {


    @Override
    <T> ResponseFuture<JsonObject> get(String string, List<String> arguments) {
        return null;
    }

    @Override
    <T> ResponseFuture<JsonObject> post(String url, HashMap<String, String> arguments) {
        return null;
    }

    /*
    @Override
    <T> T post(List<T> arguments) {
        return null;
    }*/
}
