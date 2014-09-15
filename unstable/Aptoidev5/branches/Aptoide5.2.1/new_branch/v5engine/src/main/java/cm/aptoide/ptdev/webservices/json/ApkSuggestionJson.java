package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 29-07-2014.
 */
public class ApkSuggestionJson {

    @Key private List<Ads> ads;
    @Key private String status;

    public List getAds(){
        return this.ads;
    }
    public void setAds(List ads){
        this.ads = ads;
    }
    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public static class Data{
        @Key private String description;
        @Key private Number downloads;
        @Key private String icon;
        @Key private Number id;
        @Key private String md5sum;
        @Key private String name;
        @Key("package") private String packageName;
        @Key private String repo;
        @Key private Number size;
        @Key private Number stars;
        @Key private Number vercode;
        @Key private String vername;

        @Key private String url;
        @Key private String image;

        public String getDescription(){
            return this.description;
        }
        public void setDescription(String description){
            this.description = description;
        }
        public Number getDownloads(){
            return this.downloads;
        }
        public void setDownloads(Number downloads){
            this.downloads = downloads;
        }
        public String getIcon(){
            return this.icon;
        }
        public void setIcon(String icon){
            this.icon = icon;
        }
        public Number getId(){
            return this.id;
        }
        public void setId(Number id){
            this.id = id;
        }
        public String getMd5sum(){
            return this.md5sum;
        }
        public void setMd5sum(String md5sum){
            this.md5sum = md5sum;
        }
        public String getName(){
            return this.name;
        }
        public void setName(String name){
            this.name = name;
        }
        public String getPackageName() {
            return packageName;
        }
        public void setPackage(String packageName){
            this.packageName = packageName;
        }
        public String getRepo(){
            return this.repo;
        }
        public void setRepo(String repo){
            this.repo = repo;
        }
        public Number getSize(){
            return this.size;
        }
        public void setSize(Number size){
            this.size = size;
        }
        public Number getStars(){
            return this.stars;
        }
        public void setStars(Number stars){
            this.stars = stars;
        }
        public Number getVercode(){
            return this.vercode;
        }
        public void setVercode(Number vercode){
            this.vercode = vercode;
        }
        public String getVername(){
            return this.vername;
        }
        public void setVername(String vername){
            this.vername = vername;
        }

        public String getUrl(){
            return this.url;
        }
        public void setUrl(String url){
            this.vername = url;
        }
        public String getImage(){
            return this.image;
        }
        public void setImage(String image){
            this.image = image;
        }
    }


    public static class Ads{
        @Key private Data data;
        @Key private Info info;

        public Data getData(){
            return this.data;
        }
        public void setData(Data data){
            this.data = data;
        }
        public Info getInfo(){
            return this.info;
        }
        public void setInfo(Info info){
            this.info = info;
        }
    }

    public static class Info{
        @Key private String ad_type;
        @Key private String cpc_url;
        @Key private String cpi_url;

        public String getAd_type(){
            return this.ad_type;
        }
        public void setAd_type(String ad_type){
            this.ad_type = ad_type;
        }
        public String getCpc_url(){
            return this.cpc_url;
        }
        public void setCpc_url(String cpc_url){
            this.cpc_url = cpc_url;
        }
        public String getCpi_url(){
            return this.cpi_url;
        }
        public void setCpi_url(String cpi_url){
            this.cpi_url = cpi_url;
        }
    }

}
