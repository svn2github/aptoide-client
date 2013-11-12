package cm.aptoide.ptdev.model;

import android.database.sqlite.SQLiteStatement;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.Filters;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 14-10-2013
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public abstract class Apk {


    private String name;
    private String remotePath;
    private String versionName;
    private int versionCode;
    private String packageName;
    private String iconPath;
    private String md5h;
    private int downloads;
    private double rating;
    private String category1;
    private String category2;
    private long size;
    private Filters.Age age;
    private int minSdk;
    private Filters.Screen minScreen;
    private String screenCompat;
    private Date date;
    private double price;
    private String minGlEs;

    public String getCpuAbi() {
        return cpuAbi;
    }

    public void setCpuAbi(String cpuAbi) {
        this.cpuAbi = cpuAbi;
    }

    private String cpuAbi;


    public abstract List<String> getStatements();


    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getMd5h() {
        return md5h;
    }

    public void setMd5h(String md5h) {
        this.md5h = md5h;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Filters.Age getAge() {
        return age;
    }

    public void setAge(Filters.Age age) {
        this.age = age;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public Filters.Screen getMinScreen() {
        return minScreen;
    }

    public void setMinScreen(Filters.Screen minScreen) {
        this.minScreen = minScreen;
    }



    public void setScreenCompat(String screenCompat) {
        this.screenCompat = screenCompat;
    }

    public String getScreenCompat() {
        return screenCompat;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setMinGlEs(String minGlEs) {
        this.minGlEs = minGlEs;
    }

    public String getMinGlEs() {
        return minGlEs;
    }

    public abstract void databaseDelete(Database db);

    public abstract void databaseInsert(List<SQLiteStatement> sqLiteStatements, HashMap<String, Long> categoriesIds);
}
