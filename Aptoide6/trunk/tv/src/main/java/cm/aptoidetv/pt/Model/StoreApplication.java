package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import java.io.Serializable;
import java.util.List;

import cm.aptoidetv.pt.DetailsActivity;
import cm.aptoidetv.pt.DetailsFragmentAppView;

public class StoreApplication implements Serializable {


    private Repository repository;

    public List<PackageName> getPackagenames() {
        return packages;
    }

    private List<PackageName> packages;

    public StoreApplication() {

    }


    public Repository getRepository() { return repository; }
    public void setRepository(Repository repository) {
        this.repository = repository;
    }


    public static class Repository implements Serializable {
        private String basepath;
        private String iconspath;
        private String webservicespath;
        private String apkpath;
        private String categories;


        public String getBasepath() {
            return basepath;
        }

        public void setBasepath(String basepath) {
            this.basepath = basepath;
        }

        public String getIconspath() {
            return iconspath;
        }

        public void setIconspath(String iconspath) {
            this.iconspath = iconspath;
        }

        public String getWebservicespath() {
            return webservicespath;
        }

        public void setWebservicespath(String webservicespath) {
            this.webservicespath = webservicespath;
        }

        public String getApkpath() {
            return apkpath;
        }

        public void setApkpath(String apkpath) {
            this.apkpath = apkpath;
        }

        public String getCategories() {
            return categories;
        }

        public void setCategories(String categories) {
            this.categories = categories;
        }
    }

    public static class PackageName implements Serializable, BindInterface{

        private String apkid;
        private String ver;
        private String name;
        private String catg;
        private String catg2;
        private String dwn;
        private String rat;
        private String icon_hd;
        private String icon;
        private String date;
        private String path;
        private String md5h;
        private String sz;
        private String vercode;
        private String minSdk;
        private String minScreen;
        private String cpu;

        public String getApkid() {
            return apkid;
        }

        public void setApkid(String apkid) {
            this.apkid = apkid;
        }

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return ver;
        }

        @Override
        public String getImage() {
            String imagepath;
            if(!TextUtils.isEmpty(icon_hd)){
                imagepath = "http://pool.img.aptoide.com/geniatechapps/" + icon_hd;
            }else{
                imagepath = "http://pool.img.aptoide.com/geniatechapps/" + icon;
            }
            return imagepath;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCatg() {
            return catg;
        }

        public void setCatg(String catg) {
            this.catg = catg;
        }

        public void setCatg2(String catg2) {
            this.catg2 = catg2;
        }

        public String getDwn() {
            return dwn;
        }

        public void setDwn(String dwn) {
            this.dwn = dwn;
        }

        public String getRat() {
            return rat;
        }

        public void setRat(String rat) {
            this.rat = rat;
        }

        public String getIcon_hd() {
            return icon_hd;
        }

        public void setIcon_hd(String icon_hd) {
            this.icon_hd = icon_hd;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMd5h() {
            return md5h;
        }

        public void setMd5h(String md5h) {
            this.md5h = md5h;
        }

        public String getSz() {
            return sz;
        }

        public void setSz(String sz) {
            this.sz = sz;
        }

        public String getVercode() {
            return vercode;
        }

        public void setVercode(String vercode) {
            this.vercode = vercode;
        }

        public String getMinSdk() {
            return minSdk;
        }

        public void setMinSdk(String minSdk) {
            this.minSdk = minSdk;
        }

        public String getMinScreen() {
            return minScreen;
        }

        public void setMinScreen(String minScreen) {
            this.minScreen = minScreen;
        }

        public String getCpu() {
            return cpu;
        }

        public void setCpu(String cpu) {
            this.cpu = cpu;
        }

        public String getIcon() {
            return icon;
        }

        @Override
        public String getCategory() {
            return catg2;
        }

        @Override
        public void startActivity(Context context) {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsActivity.PACKAGE_NAME, apkid);
            intent.putExtra(DetailsActivity.FEATURED_GRAPHIC, icon_hd);
            intent.putExtra(DetailsActivity.APP_NAME, name);
            intent.putExtra(DetailsActivity.DOWNLOAD_URL, getDownloadUrl());
            intent.putExtra(DetailsActivity.VERCODE, vercode);
            intent.putExtra(DetailsActivity.MD5_SUM, md5h);
            intent.putExtra(DetailsActivity.APP_ICON, getImage());

            context.startActivity(intent);
//            Toast.makeText(context, "Start Activity", Toast.LENGTH_LONG).show();

        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDownloadUrl() {
            return "http://pool.apk.aptoide.com/geniatechapps/"+path;
        }
    }
}
