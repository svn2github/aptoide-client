package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.Error;



import java.util.List;

/**
 * Created by j-pac on 30-05-2014.
 */
public class GenericResponseV2  {

    String status;


    List<cm.aptoide.ptdev.model.Error> errors;

    public String getStatus() {
        return status;
    }

    public List<Error> getErrors() {
        return errors;
    }

}
