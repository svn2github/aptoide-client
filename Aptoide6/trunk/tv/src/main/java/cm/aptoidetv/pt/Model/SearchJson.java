package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.api.client.util.Key;

import java.util.List;

import cm.aptoidetv.pt.DetailsActivity;
import cm.aptoidetv.pt.R;

/**
 * Created by rmateus on 08-07-2014.
 */
public class SearchJson {

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

        public static class Apks implements BindInterface{
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
            private String packagename;
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

            @Override
            public boolean isEditorsChoice() {
                return false;
            }

            @Override
            public String getText(Context context) {
                return context.getString(R.string.version) + ": " + getVersion();
            }

            public String getName(Context context) {
                return this.name;
            }

            @Override
            public String getVersion() {
                return this.vername;
            }

            @Override
            public String getDownloads() {
                return "";
            }

            @Override
            public String getImage() {
                String imagepath;
                if(!TextUtils.isEmpty(iconhd)){
                    imagepath = iconhd;
                }else{
                    imagepath = icon;
                }
                return imagepath;
            }

            @Override
            public void startActivity(Context context) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(DetailsActivity.PACKAGE_NAME, getPackage());
                intent.putExtra(DetailsActivity.FEATURED_GRAPHIC, getImage());
                intent.putExtra(DetailsActivity.APP_NAME, getName(context));
                intent.putExtra(DetailsActivity.DOWNLOAD_URL, getDownloadUrl());
                intent.putExtra(DetailsActivity.VERCODE, getVercode());
                intent.putExtra(DetailsActivity.MD5_SUM, getMd5sum());
                intent.putExtra(DetailsActivity.APP_ICON, getImage());

                context.startActivity(intent);
//                Toast.makeText(context, "Start Activity", Toast.LENGTH_LONG).show();
            }

            @Override
            public String getDownloadUrl() {
                return "";
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPackage() {
                return this.packagename;
            }

            public void setPackage(String thePackage) {
                this.packagename = thePackage;
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
