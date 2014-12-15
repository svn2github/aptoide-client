package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;


import java.util.List;

/**
 * Created by rmateus on 22-01-2014.
 */
public class RelatedApkJson {

     public List<Item> develbased;
     public List<Item> itembased;
     public List<Item> multiversion;
     public String status;


     public List<cm.aptoide.ptdev.model.Error> errors;

    public List<Error> getErrors() {
        return errors;
    }

    public List<Item> getDevelbased(){
        return this.develbased;
    }

    public List<Item> getItembased(){
        return this.itembased;
    }

    public List<Item> getMultiversion(){
        return this.multiversion;
    }

    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public static class Item{
         public String age;
         public String icon;
         public String icon_hd;
         public Number malrank;
         public String md5sum;
         public String name;
         public String packageName;

         public String repo;
         public String signature;
         public String timestamp;
         public Number vercode;
         public String vername;

        public String getAge(){
            return this.age;
        }

        public String getIcon(){
            if(this.icon_hd==null){
                return this.icon;
            }
            return this.icon_hd;
        }
        public void setIcon(String icon){
            this.icon = icon;
        }
        public Number getMalrank(){
            return this.malrank;
        }
        public void setMalrank(Number malrank){
            this.malrank = malrank;
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
        public String getPackage(){
            return this.packageName;
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
        public String getSignature(){
            return this.signature;
        }
        public void setSignature(String signature){
            this.signature = signature;
        }
        public String getTimestamp(){
            return this.timestamp;
        }
        public void setTimestamp(String timestamp){
            this.timestamp = timestamp;
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


    }
}
