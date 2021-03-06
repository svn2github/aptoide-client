package cm.aptoide.ptdev.downloadmanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 17-09-2013
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class FinishedApk implements Parcelable, Serializable{

    private String name;
    private String apkid;
    private String version;
    private long appHashId;
    private String iconpath;
    private String path;
    private ArrayList<String> permissionsList;



    public FinishedApk(String name, String apkid, String version, long appHashId, String iconpath, String path, ArrayList<String> permissions) {
        this.name = name;
        this.apkid = apkid;
        this.version = version;
        this.appHashId = appHashId;



        this.iconpath = iconpath;
        this.path = path;
        this.permissionsList = permissions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApkid() {
        return apkid;
    }

    public String getVersion() { return version; }

    public void setApkid(String apkid) {
        this.apkid = apkid;
    }

    public long getAppHashId() {
        return appHashId;
    }

    public void setAppHashId(int appHashId) {
        this.appHashId = appHashId;
    }

    public String getIconPath() {
        return iconpath;
    }

    public void setIconPath(String iconpath) {
        this.iconpath = iconpath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(apkid);
        dest.writeLong(appHashId);
        dest.writeString(iconpath);
        dest.writeString(path);
        dest.writeStringList(permissionsList);
        Log.d("Aptoide-FinishedApkParcel", "" + path);

    }

    public static final Creator<FinishedApk> CREATOR
            = new Creator<FinishedApk>() {
        public FinishedApk createFromParcel(Parcel in) {
            return new FinishedApk(in);
        }

        public FinishedApk[] newArray(int size) {
            return new FinishedApk[size];
        }
    };

    private FinishedApk(Parcel in) {
        name = in.readString();
        apkid = in.readString();
        appHashId = in.readLong();
        iconpath = in.readString();
        path = in.readString();

        permissionsList = new ArrayList<String>();
        in.readStringList(permissionsList);
        Log.d("Aptoide-FinishedApkParceOut", "Path" + path);


    }

    public String getPath() {
        return path;
    }

    public ArrayList<String> getPermissionsList() {
        return permissionsList;
    }

    public void setPermissionsList(ArrayList<String> permissionsList) {
        this.permissionsList = permissionsList;
    }
}
