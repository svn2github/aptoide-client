package cm.aptoide.ptdev.fragments;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 27-12-2013.
 */
public class GenericResponse {

    @Key
    String status;

    @Key
    List<String> errors;

    public String getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
