package cm.aptoide.ptdev.webservices.json;


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

    @Key
    private String repo;

    @Key
    private String avatar;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
