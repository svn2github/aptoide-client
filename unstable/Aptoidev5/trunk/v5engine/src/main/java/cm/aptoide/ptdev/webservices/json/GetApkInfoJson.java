
package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.*;
import com.google.api.client.util.Key;

import java.util.List;


public class GetApkInfoJson {

    @Key
    private Apk apk;

    @Key
    private String latest;
    @Key
    private Malware malware;
    @Key
    private Media media;
    @Key
    private Meta meta;
    @Key
    private Payment payment;
    @Key
    private Signature signature;
    @Key
    private String status;

    @Key("obb")
    private ObbObject obb;

    @Key
    private List<cm.aptoide.ptdev.model.Error> errors;

    public Apk getApk() {
        return apk;
    }

    public String getLatest() {
        return latest;
    }

    public Malware getMalware() {
        return malware;
    }

    public Media getMedia() {
        return media;
    }

    public Meta getMeta() {
        return meta;
    }

    public Payment getPayment() {
        return payment;
    }

    public Signature getSignature() {
        return signature;
    }

    public String getStatus() {
        return status;
    }

    public List<cm.aptoide.ptdev.model.Error> getErrors() {
        return errors;
    }

    public ObbObject getObb() {
        return obb;
    }

    public static class Media{
        @Key private List<String> sshots;
        @Key private List<Screenshots> sshots_hd;
        @Key private List<Videos> videos;

        public List<String> getSshots(){

            return this.sshots;
        }

        public List<Screenshots> getSshots_hd(){
            return this.sshots_hd;
        }
        public void setSshots(List<String> sshots){
            this.sshots = sshots;
        }

        public static class Videos{
            @Key private String thumb;
            @Key private String type;
            @Key private String url;

            public String getThumb(){
                return this.thumb;
            }
            public void setThumb(String thumb){
                this.thumb = thumb;
            }
            public String getType(){
                return this.type;
            }
            public void setType(String type){
                this.type = type;
            }
            public String getUrl(){
                return this.url;
            }
            public void setUrl(String url){
                this.url = url;
            }
        }

        public static class Screenshots {
            @Key private String path;
            @Key private String orient;


            public String getOrient() {
                return orient;
            }

            public String getPath() {
                return path;
            }
        }
    }

    public static class Payment{
        @Key private Number amount;
        @Key private String status;

        public Number getAmount(){
            return this.amount;
        }
        public void setAmount(Number amount){
            this.amount = amount;
        }
        public String getStatus(){
            return this.status;
        }
        public void setStatus(String status){
            this.status = status;
        }
    }

    public static class Meta{
        @Key private List<Comment> comments;
        @Key private String description;
        @Key private Developer developer;
        @Key private Likevotes likevotes;
        @Key private String news;
        @Key private String title;

        public List<Comment> getComments(){
            return this.comments;
        }
        public void setComments(List<Comment> comments){
            this.comments = comments;
        }
        public String getDescription(){
            return this.description;
        }
        public void setDescription(String description){
            this.description = description;
        }
        public Developer getDeveloper(){
            return this.developer;
        }
        public void setDeveloper(Developer developer){
            this.developer = developer;
        }
        public Likevotes getLikevotes(){
            return this.likevotes;
        }
        public void setLikevotes(Likevotes likevotes){
            this.likevotes = likevotes;
        }
        public String getNews(){
            return this.news;
        }
        public void setNews(String news){
            this.news = news;
        }
        public String getTitle(){
            return this.title;
        }
        public void setTitle(String title){
            this.title = title;
        }

        public static class Likevotes{
            @Key private Number dislikes;
            @Key private Number likes;
            @Key private Number rating;
            @Key private String uservote;


            public String getUservote() {
                return uservote;
            }

            public void setUservote(String uservote) {
                this.uservote = uservote;
            }

            public Number getDislikes(){
                return this.dislikes;
            }
            public void setDislikes(Number dislikes){
                this.dislikes = dislikes;
            }
            public Number getLikes(){
                return this.likes;
            }
            public void setLikes(Number likes){
                this.likes = likes;
            }
            public Number getRating(){
                return this.rating;
            }
            public void setRating(Number rating){
                this.rating = rating;
            }
        }

        public static class Developer{
            @Key private Info info;
            @Key private List<String> packages;

            public Info getInfo(){
                return this.info;
            }
            public void setInfo(Info info){
                this.info = info;
            }
            public List<String> getPackages(){
                return this.packages;
            }
            public void setPackages(List<String> packages){
                this.packages = packages;
            }

            public static class Info{
                @Key private String email;
                @Key private String name;
                @Key private String privacy_policy;
                @Key private String website;

                public String getEmail(){
                    return this.email;
                }
                public void setEmail(String email){
                    this.email = email;
                }
                public String getName(){
                    return this.name;
                }
                public void setName(String name){
                    this.name = name;
                }
                public String getPrivacy_policy(){
                    return this.privacy_policy;
                }
                public void setPrivacy_policy(String privacy_policy){
                    this.privacy_policy = privacy_policy;
                }
                public String getWebsite(){
                    return this.website;
                }
                public void setWebsite(String website){
                    this.website = website;
                }
            }
        }
    }

    public static class Malware{
        @Key private Reason reason;
        @Key private String status;

        public Reason getReason(){
            return this.reason;
        }
        public void setReason(Reason reason){
            this.reason = reason;
        }
        public String getStatus(){
            return this.status;
        }
        public void setStatus(String status){
            this.status = status;
        }

        public static class Scanned{
            @Key private List<String> av;
            @Key private String date;
            @Key private String status;

            public List<String> getAv(){
                return this.av;
            }
            public void setAv(List<String> av){
                this.av = av;
            }
            public String getDate(){
                return this.date;
            }
            public void setDate(String date){
                this.date = date;
            }
            public String getStatus(){
                return this.status;
            }
            public void setStatus(String status){
                this.status = status;
            }
        }

        public static class Reason{
            @Key private Scanned scanned;
            @Key private Signature_validated signature_validated;
            @Key private Thirdparty_validated thirdparty_validated;

            public Scanned getScanned(){
                return this.scanned;
            }
            public void setScanned(Scanned scanned){
                this.scanned = scanned;
            }
            public Signature_validated getSignature_validated(){
                return this.signature_validated;
            }
            public void setSignature_validated(Signature_validated signature_validated){
                this.signature_validated = signature_validated;
            }
            public Thirdparty_validated getThirdparty_validated(){
                return this.thirdparty_validated;
            }
            public void setThirdparty_validated(Thirdparty_validated thirdparty_validated){
                this.thirdparty_validated = thirdparty_validated;
            }

            public static class Signature_validated{
                @Key private String date;
                @Key private String signature_from;
                @Key private String status;

                public String getDate(){
                    return this.date;
                }
                public void setDate(String date){
                    this.date = date;
                }
                public String getSignature_from(){
                    return this.signature_from;
                }
                public void setSignature_from(String signature_from){
                    this.signature_from = signature_from;
                }
                public String getStatus(){
                    return this.status;
                }
                public void setStatus(String status){
                    this.status = status;
                }
            }

            public static class Thirdparty_validated{
                @Key private String date;
                @Key private String store;

                public String getDate(){
                    return this.date;
                }
                public void setDate(String date){
                    this.date = date;
                }
                public String getStore(){
                    return this.store;
                }
                public void setStore(String store){
                    this.store = store;
                }
            }
        }

    }

    public static class Signature{
        @Key private String c;
        @Key private String cN;
        @Key private String l;
        @Key private String o;
        @Key private String sHA1;
        @Key private String sT;

        public String getC(){
            return this.c;
        }
        public void setC(String c){
            this.c = c;
        }
        public String getCN(){
            return this.cN;
        }
        public void setCN(String cN){
            this.cN = cN;
        }
        public String getL(){
            return this.l;
        }
        public void setL(String l){
            this.l = l;
        }
        public String getO(){
            return this.o;
        }
        public void setO(String o){
            this.o = o;
        }
        public String getSHA1(){
            return this.sHA1;
        }
        public void setSHA1(String sHA1){
            this.sHA1 = sHA1;
        }
        public String getST(){
            return this.sT;
        }
        public void setST(String sT){
            this.sT = sT;
        }
    }


    public static class Apk{

        @Key
        private String icon;
        @Key
        private Number id;
        @Key
        private String md5sum;
        @Key("package")
        private String packageName;
        @Key
        private String path;
        @Key
        private List<String> permissions;
        @Key
        private String repo;
        @Key
        private Number size;
        @Key
        private Number vercode;
        @Key
        private String vername;
        @Key
        private String icon_hd;


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
        public String getPackage(){
            return this.packageName;
        }
        public void setPackage(String packageName){
            this.packageName = packageName;
        }
        public String getPath(){
            return this.path;
        }
        public void setPath(String path){
            this.path = path;
        }
        public List<String> getPermissions(){
            return this.permissions;
        }
        public void setPermissions(List<String> permissions){
            this.permissions = permissions;
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

        public String getIconHd() {
            return icon_hd;
        }


    }


    public static class ObbObject {

        @Key("main")
        private Obb main;

        public Obb getMain() {
            return main;
        }

        public Obb getPatch() {
            return patch;
        }

        @Key("patch")
        private Obb patch;
    }
}
