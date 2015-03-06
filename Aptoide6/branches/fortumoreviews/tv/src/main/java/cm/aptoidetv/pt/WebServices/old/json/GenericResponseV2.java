package cm.aptoidetv.pt.WebServices.old.json;

import java.util.List;
import cm.aptoidetv.pt.Model.Error;

public class GenericResponseV2  {

    String status;


    List<Error> errors;

    public String getStatus() {
        return status;
    }

    public List<Error> getErrors() {
        return errors;
    }

}
