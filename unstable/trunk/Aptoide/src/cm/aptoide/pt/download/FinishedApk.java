package cm.aptoide.pt.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 17-09-2013
 * Time: 17:48
 * To change this template use File | Settings | File Templates.
 */
public class FinishedApk implements Parcelable{
    private String path;
    private String name;
    private String apkid;
    private int appHashId;
    private String iconpath;


    public FinishedApk(String name, String apkid, int appHashId, String iconpath, String path) {
        this.name = name;
        this.apkid = apkid;
        this.appHashId = appHashId;
        this.iconpath = iconpath;
        this.path = path;
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

    public void setApkid(String apkid) {
        this.apkid = apkid;
    }

    public int getAppHashId() {
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
        dest.writeInt(appHashId);
        dest.writeString(iconpath);
        dest.writeString(path);
    }

    public static final Parcelable.Creator<FinishedApk> CREATOR
            = new Parcelable.Creator<FinishedApk>() {
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
        appHashId = in.readInt();
        iconpath = in.readString();
        path = in.readString();
    }

    public String getPath() {
        return path;
    }
}
