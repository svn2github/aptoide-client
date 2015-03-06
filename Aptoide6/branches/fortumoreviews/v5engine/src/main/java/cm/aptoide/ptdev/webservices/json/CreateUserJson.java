package cm.aptoide.ptdev.webservices.json;


import cm.aptoide.ptdev.model.Error;


import java.util.List;

/**
 * Created by brutus on 09-12-2013.
 */
public class CreateUserJson {


    public String status;


    public List<Error> errors;

   //
    //public String queueName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Error> getErrors() {
        return errors;
    }


    /*
    public String getQueueName() { return queueName; }

    public void setQueueName(String queueName) { this.queueName = queueName; }
    */
}
