package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;

/**
 * Created by rmateus on 08-07-2014.
 */
public class SearchJson{

    @Key private Results results;
    @Key private String status;
    @Key private List<Error> errors ;

    public Results getResults(){
        return this.results;
    }
    public void setResults(Results results){
        this.results = results;
    }
    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public List<Error> getErrors() {
        return errors;
    }


    public static class Results{
        @Key private List<Apks> apks;
        @Key private List<Apks> u_apks;
        @Key private List<String> didyoumean;

        public List<Apks> getApks(){
            return this.apks;
        }
        public void setApks(List<Apks> apks){
            this.apks = apks;
        }
        public List<Apks> getU_Apks(){
            return this.u_apks;
        }
        public void setU_Apks(List<Apks> apks){
            this.u_apks = u_apks;
        }
        public List<String> getDidyoumean(){
            return this.didyoumean;
        }
        public void setDidyoumean(List<String> didyoumean){
            this.didyoumean = didyoumean;
        }

        public static class Apks {
            @Key
            private String age;
            @Key
            private String icon;
            @Key("icon_hd")
            private String iconhd;
            @Key
            private Number malrank;
            @Key
            private String md5sum;
            @Key
            private String name;

            @Key("package")
            private String thePackage;
            @Key
            private String repo;
            @Key
            private String signature;
            @Key
            private Number stars;

            @Key
            private String timestamp;
            @Key
            private Number vercode;
            @Key
            private String vername;


            public String getAge() {
                return this.age;
            }

            public void setAge(String age) {
                this.age = age;
            }

            public String getIcon() {
                return this.icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public Number getMalrank() {
                return this.malrank;
            }

            public void setMalrank(Number malrank) {
                this.malrank = malrank;
            }

            public String getMd5sum() {
                return this.md5sum;
            }

            public void setMd5sum(String md5sum) {
                this.md5sum = md5sum;
            }

            public String getName() {
                return this.name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPackage() {
                return this.thePackage;
            }

            public void setPackage(String thePackage) {
                this.thePackage = thePackage;
            }

            public String getRepo() {
                return this.repo;
            }

            public void setRepo(String repo) {
                this.repo = repo;
            }

            public String getSignature() {
                return this.signature;
            }

            public void setSignature(String signature) {
                this.signature = signature;
            }

            public String getTimestamp() {
                return this.timestamp;
            }

            public void setTimestamp(String timestamp) {
                this.timestamp = timestamp;
            }

            public Number getVercode() {
                return this.vercode;
            }

            public void setVercode(Number vercode) {
                this.vercode = vercode;
            }

            public String getVername() {
                return this.vername;
            }

            public void setVername(String vername) {
                this.vername = vername;
            }

            public String getIconhd() {
                return iconhd;
            }


            public float getStars() {

                if(stars!=null){
                    return stars.floatValue();
                }else{
                    return 0.0f;
                }

            }
        }

    }

}
