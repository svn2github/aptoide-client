package cm.aptoide.ptdev.webservices.json;


import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by brutus on 09-12-2013.
 */

public class CheckUserCredentialsJson {

    @Key
    private String status;

    @Key
    private String token;

    @Key String repo;

    @Key
    private String avatar;

    @Key
    private String username;

    @Key
    private String queueName;

    @Key
    private List<Error> errors;

   // @Key
    //private String queueName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public String getQueue() {
        return queueName;
    }

    public String getAvatar() { return avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getRepo() { return repo; }

    /*
    public String getQueueName() { return queueName; }

    public void setQueueName(String queueName) { this.queueName = queueName; }
    */
}
