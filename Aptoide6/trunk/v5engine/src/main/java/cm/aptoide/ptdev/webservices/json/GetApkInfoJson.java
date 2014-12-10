
package cm.aptoide.ptdev.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.List;

import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Obb;
import openiab.webservices.json.PaymentServices;


public class GetApkInfoJson {


    private Apk apk;


    private String latest;

    private Malware malware;

    private Media media;

    private Meta meta;

    private Payment payment;

    private Signature signature;

    private String status;


    private ObbObject obb;


    List<cm.aptoide.ptdev.model.Error> errors;

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
         private List<String> sshots;
         private List<Screenshots> sshots_hd;
         private List<Videos> videos;

        public List<String> getSshots(){

            return this.sshots;
        }

        public List<Screenshots> getSshots_hd(){
            return this.sshots_hd;
        }
        public void setSshots(List<String> sshots){
            this.sshots = sshots;
        }

        public List<Videos> getVideos() { return videos; }
        public void setVideos(List<Videos> videos) { this.videos = videos; }

        public static class Videos{
             private String thumb;
             private String type;
             private String url;

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
             private String path;
             private String orient;

            public String getOrient() {
                return orient;
            }

            public String getPath() {
                return path;
            }
        }
    }

    public static class Payment{

         private Number amount;
         private String symbol;
         private String error;
         private String apkpath;
         private Metadata metadata;
         private List<PaymentServices> payment_services;
         private String status;

        public Number getAmount(){
            return this.amount;
        }
        public void setAmount(Number amount){
            this.amount = amount;
        }
        public String getapkpath(){
            return this.apkpath;
        }
        public void setapkpath(String apkpath){
            this.apkpath = apkpath;
        }
        public String geterror(){
            return this.error;
        }
        public void seterror(String error){
            this.error = error;
        }
        public String getStatus(){
            return this.status;
        }
        public void setStatus(String status){
            this.status = status;
        }
        public List<PaymentServices> getPayment_services(){
            return this.payment_services;
        }
        public void setPayment_services(List<PaymentServices> payment_services){ this.payment_services = payment_services;}
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public Metadata getMetadata() { return metadata; }

        public static class Metadata{
             private int id;

            public int getId(){
                return id;
            }
        }
    }

    public static class Meta{
         private List<Comment> comments;
         private String description;
         private Developer developer;
         private Likevotes likevotes;
         private String news;
         private String title;
         private String wurl;
         private Flags flags;
         private int downloads;


        public int getDownloads(){return this.downloads;}
        public void setDownloads(int downloads){this.downloads = downloads; }
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
        public Likevotes getLikevotes(){ return this.likevotes; }
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
        public String getWUrl() { return wurl; }

        public Flags getFlags() { return flags; }
        public void setFlags(Flags flags) { this.flags = flags;}

        public static class Likevotes{
             private Number dislikes;
             private Number likes;
             private Number rating;
             private String uservote;


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

        public static class Flags{
             private Votes votes;
             private String uservote;
             private Veredict veredict;

            public String getUservote() {
                return uservote;
            }

            public Votes getVotes(){
                return this.votes;
            }
            public void setVotes(Votes votes){
                this.votes = votes;
            }

            public Veredict getVeredict(){ return this.veredict;}
            public void setVeredict(Veredict veredict){ this.veredict = veredict;}
        }

        public static class Veredict{
             private String flag;
             private String review;

            public String getFlag(){
                return this.flag;
            }
            public void setFlag(String flag){
                this.flag = flag;
            }
            public String getReview(){
                return this.review;
            }
            public void setReview(String review){
                this.review = review;
            }
        }

        public static class Votes{
             private Number fake;
             private Number freeze;
             private Number good;
             private Number license;
             private Number virus;

            public Number getFake(){
                return this.fake;
            }
            public void setFake(Number fake){
                this.fake = fake;
            }
            public Number getFreeze(){
                return this.freeze;
            }
            public void setFreeze(Number freeze){
                this.freeze = freeze;
            }
            public Number getGood(){
                return this.good;
            }
            public void setGood(Number good){
                this.good = good;
            }
            public Number getLicense(){
                return this.license;
            }
            public void setLicense(Number license){
                this.license = license;
            }
            public Number getVirus(){
                return this.virus;
            }
            public void setVirus(Number virus){
                this.virus = virus;
            }
        }

        public static class Developer{
             private Info info;
             private List<String> packages;

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
                 private String email;
                 private String name;
                 private String privacy_policy;
                 private String website;

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
         private Reason reason;
         private String status;

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
             private List<Av_info> av_info;
             private String date;
             private String status;

            public List<Av_info> getAv_info(){ return this.av_info; }
            public void setAv_info(List<Av_info> av_info){ this.av_info = av_info; }
            public String getDate(){ return this.date; }
            public void setDate(String date){ this.date = date; }
            public String getStatus(){ return this.status; }
            public void setStatus(String status){ this.status = status; }
        }

        public static class Reason{
             private Scanned scanned;
             private Signature_validated signature_validated;
             private Thirdparty_validated thirdparty_validated;
             private Manual_qa manual_qa;

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
            public Manual_qa getManual_qa() { return manual_qa; }
            public void setManual_qa(Manual_qa manual_qa) { this.manual_qa = manual_qa; }

            public static class Signature_validated{
                 private String date;
                 private String signature_from;
                 private String status;

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
                 private String date;
                 private String store;

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

            public static class Manual_qa{
                 private String date;
                 private String tester;
                 private String status;


                public String getDate(){
                    return this.date;
                }
                public void setDate(String date){
                    this.date = date;
                }
                public void setTester(String tester) { this.tester = tester; }
                public String getTester() { return tester; }
                public String getStatus() { return status; }
                public void setStatus(String status) { this.status = status; }

            }


        }

    }

    public static class Av_info{
         private List<Infection> infections;
         private String name;

        public List getInfections(){ return this.infections; }
        public void setInfections(List<Infection> infections){ this.infections = infections; }
        public String getName(){ return this.name; }
        public void setName(String name){ this.name = name; }
    }

    public static class Infection{
         private String description;
         private String name;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getName() {return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Signature{

        @JsonProperty("SHA1") private String SHA1;


        public String getSHA1(){
            return this.SHA1;
        }
        public void setSHA1(String sHA1){
            this.SHA1 = sHA1;
        }

    }


    public static class Apk{


        private String icon;

        private Number id;

        private String md5sum;


        private Number minSdk;


        private String minScreen;

        private String packageName;

        private String path;

        private String altpath;

        private List<String> permissions;

        private String repo;

        private Number size;

        private Number vercode;

        private String vername;

        public String icon_hd;

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


        public Number getMinSdk() {
            return minSdk;
        }

        public String getMinScreen() {
            return minScreen;
        }

        public String getAltPath() {
            return altpath;
        }
    }


    public static class ObbObject {


        private Obb main;

        public Obb getMain() {
            return main;
        }

        public Obb getPatch() {
            return patch;
        }


        private Obb patch;
    }
}
