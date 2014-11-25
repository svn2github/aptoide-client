package cm.aptoide.ptdev.webservices.json;


import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;


import java.util.List;

/**
 * Created by brutus on 09-12-2013.
 */

public class CheckUserCredentialsJson {


    private String status;


    private String token;

     String repo;


    private String avatar;


    private String username;


    private String queueName;


    private List<Error> errors;

     private Settings settings;

    public Settings getSettings() {
        return settings;
    }

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

    public static class Settings {
         private String timeline;

        public String getTimeline() {
            return timeline;
        }
    }

}
