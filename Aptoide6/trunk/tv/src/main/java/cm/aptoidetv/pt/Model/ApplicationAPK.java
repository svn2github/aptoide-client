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

/**
 * Created by asantos on 26-11-2014.
 */
public class ApplicationAPK implements Serializable, BindInterface{

    private String packagename;
    private String ver;
    private String name;
    private String catg;
    private String catg2;
    private String downloads;
    private String rat;
    private String icon_hd;
    private String icon;
    private String date;
    private String path;
    private String md5h;
/*    private String sz;
    private String vercode;
    private String minSdk;
    private String minScreen;
    private String cpu;*/

    public ApplicationAPK(Response.ListApps.Apk apk,String cat){
        this.name = apk.name;
        this.packagename =  apk.packageName;
        this.ver = apk.vername;
        this.md5h = apk.md5sum;
        this.downloads = String.valueOf(apk.downloads);
        this.rat= String.valueOf(apk.rating);
        this.icon = apk.icon;
        this.catg2=cat;
        this.catg=cat;

        //public String apk.graphic;
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.downloads) + ": " + getDownloads();
    }

    @Override
    public String getName(Context context) {
        return name;
    }

    @Override
    public String getVersion() {
        return ver;
    }

    @Override
    public String getDownloads() {
        return downloads;
    }

    public String getImage() {
        return TextUtils.isEmpty(icon_hd) ? icon:icon_hd;
    }

    @Override
    public void startActivity(Context context) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(DetailsActivity.PACKAGE_NAME, packagename);
        intent.putExtra(DetailsActivity.FEATURED_GRAPHIC, getImage());
        intent.putExtra(DetailsActivity.APP_NAME, name);
        intent.putExtra(DetailsActivity.DOWNLOADS, downloads);
        intent.putExtra(DetailsActivity.MD5_SUM, md5h);
        intent.putExtra(DetailsActivity.APP_ICON, getImage());

        context.startActivity(intent);
//            Toast.makeText(context, "Start Activity", Toast.LENGTH_LONG).show();

    }

    public String getDownloadUrl() {
        return path;
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

    @Override
    public int getWidth() {
        return CardPresenter.ICON_WIDTH;
    }
    @Override
    public int getHeight() {
        return CardPresenter.ICON_HEIGHT;
    }
}