package cm.aptoide.ptdev.webservices.json;


import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by brutus on 09-12-2013.
 */
public class CreateUserJson {

    @Key
    private String status;

    @Key
    private List<String> errors;

   // @Key
    //private String queueName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /*
    public String getQueueName() { return queueName; }

    public void setQueueName(String queueName) { this.queueName = queueName; }
    */
}
