package cm.aptoide.ptdev.fragments;



import java.util.List;

/**
 * Created by rmateus on 27-12-2013.
 */
public class GenericResponse {


    String status;


    List<String> errors;

    public String getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
