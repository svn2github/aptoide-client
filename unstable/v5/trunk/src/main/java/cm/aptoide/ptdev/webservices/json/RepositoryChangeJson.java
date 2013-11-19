package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryChangeJson {

    @Key
    public Listing listing;
    @Key
    String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public static class Listing {

        @Key
        public String name;
        @Key
        public String avatar;
        @Key
        public int downloads;
        @Key
        public String theme;
        @Key
        public String description;
        @Key
        public String items;
        @Key
        public String view;

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getDownloads() {
            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
        }

        public String getItems() {
            return items;
        }

        public void setItems(String items) {
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public String getView() {
            return view;
        }

        public void setView(String view) {
            this.view = view;
        }


    }


}
