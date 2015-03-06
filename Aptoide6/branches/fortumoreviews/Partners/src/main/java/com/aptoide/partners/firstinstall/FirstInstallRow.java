package com.aptoide.partners.firstinstall;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rmateus on 23-01-2015.
 */
public class FirstInstallRow implements Parcelable {

    private boolean selected;
    private String icon;
    private String appName;
    private long downloads;
    private long id;
    private long size;
    private boolean animate;
    private String cpi_url;
    private String network_click_url;



    public String getCpi_url() {
        return cpi_url;
    }

    public void setCpi_url(String cpi_url) {
        this.cpi_url = cpi_url;
    }

    public String getNetwork_click_url() {
        return network_click_url;
    }

    public void setNetwork_click_url(String network_click_url) {
        this.network_click_url = network_click_url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getDownloads() {
        return downloads;
    }

    public void setDownloads(long downloads) {
        this.downloads = downloads;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void animate(boolean animate) {
        this.animate = animate;
    }

    public boolean isAnimate() {
        return animate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
        dest.writeString(this.icon);
        dest.writeString(this.appName);
        dest.writeLong(this.downloads);
        dest.writeLong(this.id);
        dest.writeLong(this.size);
        dest.writeByte(animate ? (byte) 1 : (byte) 0);
        dest.writeString(this.cpi_url);
        dest.writeString(this.network_click_url);
    }

    public FirstInstallRow() {
    }

    private FirstInstallRow(Parcel in) {
        this.selected = in.readByte() != 0;
        this.icon = in.readString();
        this.appName = in.readString();
        this.downloads = in.readLong();
        this.id = in.readLong();
        this.size = in.readLong();
        this.animate = in.readByte() != 0;
        this.cpi_url = in.readString();
        this.network_click_url = in.readString();
    }

    public static final Parcelable.Creator<FirstInstallRow> CREATOR = new Parcelable.Creator<FirstInstallRow>() {
        public FirstInstallRow createFromParcel(Parcel source) {
            return new FirstInstallRow(source);
        }

        public FirstInstallRow[] newArray(int size) {
            return new FirstInstallRow[size];
        }
    };
}
