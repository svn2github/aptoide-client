package cm.aptoidetv.pt.Model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.Serializable;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.CardPresenter;
import cm.aptoidetv.pt.DetailsActivity;
import cm.aptoidetv.pt.R;
import cm.aptoidetv.pt.WebServices.Response;

public class EditorsChoice implements Serializable, BindInterface {

    private String basepath;
    private String iconspath;
    private String screenspath;
    private String featuregraphicpath;
    private String apkpath;
    private String categories;
    private PackageName packagename;
    private String name;

    public EditorsChoice(Response.GetStore.Widgets.Widget widget) {
        this.name = widget.name;
    }

    @Override
    public boolean isEditorsChoice() {
        return true;
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.downloads) + ": " + getDownloads();
    }

    @Override
    public String getName(Context context) {
        return packagename.name;
    }

    @Override
    public String getVersion() {
        return packagename.getVer();
    }

    @Override
    public String getDownloads() {
        return packagename.getDwn();
    }

    public String getImage() {
        return featuregraphicpath +""+ packagename.featuregraphic;
    }

    @Override
    public void startActivity(Context context) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(DetailsActivity.PACKAGE_NAME, packagename.getApkid());
        intent.putExtra(DetailsActivity.FEATURED_GRAPHIC, featuregraphicpath + packagename.featuregraphic);
        intent.putExtra(DetailsActivity.APP_NAME, packagename.name);
        intent.putExtra(DetailsActivity.DOWNLOAD_URL, apkpath+getPackagename().getPath());
        intent.putExtra(DetailsActivity.VERCODE, getPackagename().getVercode());
        intent.putExtra(DetailsActivity.MD5_SUM, getPackagename().getMd5h());
        intent.putExtra(DetailsActivity.APP_SIZE, getPackagename().getSz());

        context.startActivity(intent);

//        Toast.makeText(context, "Start Activity", Toast.LENGTH_LONG).show();

    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Editors Choice {" +
                    "basepath=\'" + basepath + "\'" +
                    ", featuregraphicpath=\'" + featuregraphicpath + "\'" +
                    ", apkpath=\'" + apkpath + "\'" +
                    ", categories=\'" + categories + "\'" +
                "}";
    }

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

    public String getScreenspath() {
        return screenspath;
    }

    public void setScreenspath(String screenspath) {
        this.screenspath = screenspath;
    }

    public String getFeaturegraphicpath() {
        return featuregraphicpath;
    }

    public void setFeaturegraphicpath(String featuregraphicpath) {
        this.featuregraphicpath = featuregraphicpath;
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

    public PackageName getPackagename() {
        return packagename;
    }

    public void setPackagename(PackageName packagename) {
        this.packagename = packagename;
    }

    public String getDownloadUrl() {
        return apkpath+getPackagename().getPath();
    }

    @Override
    public void setImage(int iconWidth, int iconHeight, CardPresenter.PicassoImageCardViewTarget picassoImageCardViewTarget) {
        AppTV.getPicasso()
                .load(getImage())
                .placeholder(android.R.drawable.sym_def_app_icon)
                .centerInside()
                .resize(iconWidth,iconHeight)
                        //   .error(mDefaultCardImage)
                .into(picassoImageCardViewTarget);
    }

    public static class PackageName implements Serializable {

        private String apkid;
        private String ver;
        private String name;
        private String catg;
        private String catg2;
        private String dwn;
        private String rat;
        private String featuregraphic;
        private String icon_hd;
        private String icon;
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

        public void setName(String name) {
            this.name = name;
        }

        public String getCatg() {
            return catg;
        }

        public void setCatg(String catg) {
            this.catg = catg;
        }

        public String getCatg2() {
            return catg2;
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

        public String getFeaturegraphic() {
            return featuregraphic;
        }

        public void setFeaturegraphic(String featuregraphic) {
            this.featuregraphic = featuregraphic;
        }

        public String getIcon_hd() {
            String iconapk;
            if(!TextUtils.isEmpty(icon_hd)){
                iconapk = icon_hd;
            }else{
                iconapk = icon;
            }
            return iconapk;
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

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
