package cm.aptoide.ptdev.webservices;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
public abstract class Client {

    abstract <T> ResponseFuture<JsonObject> get(String string, List<String> arguments);
    abstract <T> T post(List<T> arguments);

}
