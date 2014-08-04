package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 29-07-2014.
 */
public class ApkSuggestionJson {

    String status;


    public List<AppSuggested> getApp_suggested() {
        return app_suggested;
    }

    @Key List<AppSuggested> app_suggested;


    public static class AppSuggested {

        @Key private String cpc_url;
        @Key private String cpi_url;
        @Key private String description;
        @Key private Number downloads;
        @Key private String icon;
        @Key private String icon_hd;
        @Key private Number id;
        @Key private String md5sum;
        @Key private String name;
        @Key("package") private String packageName;
        @Key private String repo;
        @Key private Number size;
        @Key private Number stars;
        @Key private Number vercode;
        @Key private String vername;


        public String getCpc_url() {
            return cpc_url;
        }

        public String getCpi_url() {
            return cpi_url;
        }

        public String getDescription() {
            return description;
        }

        public Number getDownloads() {
            return downloads;
        }

        public String getIcon() {
            return icon;
        }

        public String getIcon_hd() {
            return icon_hd;
        }

        public Number getId() {
            return id;
        }

        public String getMd5sum() {
            return md5sum;
        }

        public String getName() {
            return name;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getRepo() {
            return repo;
        }

        public Number getSize() {
            return size;
        }

        public Number getStars() {
            return stars;
        }

        public Number getVercode() {
            return vercode;
        }

        public String getVername() {
            return vername;
        }
    }

}
