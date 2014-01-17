package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryChangeJson {

    @Key
    public List<Listing> listing;

    @Key
    String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public static class Listing{

        @Key
        private Number appscount;

        @Key
        private String hasupdates;

        @Key
        private String repo;

        public Number getAppscount(){
            return this.appscount;
        }
        public void setAppscount(Number appscount){
            this.appscount = appscount;
        }
        public String getHasupdates(){
            return this.hasupdates;
        }
        public void setHasupdates(String hasupdates){
            this.hasupdates = hasupdates;
        }
        public String getRepo(){
            return this.repo;
        }
        public void setRepo(String repo){
            this.repo = repo;
        }
    }


}
