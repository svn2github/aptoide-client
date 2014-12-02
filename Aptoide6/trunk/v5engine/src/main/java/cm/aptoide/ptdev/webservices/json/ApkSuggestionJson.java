package cm.aptoide.ptdev.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by rmateus on 29-07-2014.
 */
public class ApkSuggestionJson {

     private List<Ads> ads;
     private String status;

    public List<Ads> getAds(){
        return this.ads;
    }
    public void setAds(List<Ads> ads){
        this.ads = ads;
    }
    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public static class Data{
         private String description;
         private Number downloads;
         private String icon;
         private Number id;
         private String md5sum;
         private String name;
         @JsonProperty("package") private String packageName;
         private String repo;
         private Number size;
         private Number stars;
         private Number vercode;
         private String vername;

         private String url;
         private String image;

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

        private Data data;
        private Info info;
        private Partner partner;

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

        public Partner getPartner() {
            return partner;
        }

        public void setPartner(Partner partner) {
            this.partner = partner;
        }
    }

    public static class Info{
         private String ad_type;
         private String cpc_url;
         private String cpi_url;

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

    public static class Partner{

        public Info getPartnerInfo() {
            return partnerInfo;
        }

        public Data getPartnerData() {
            return partnerData;
        }

        @JsonProperty("info") private Info partnerInfo;
         private Data partnerData;

        public static class Info{

            public Number getId() {
                return id;
            }

            public String getName() {
                return name;
            }

             private Number id;
             private String name;
        }

        public static class Data{

            public String getClick_url() {
                return click_url;
            }

            public String getImpression_url() {
                return impression_url;
            }

             private String click_url;
             private String impression_url;
        }


    }

}
