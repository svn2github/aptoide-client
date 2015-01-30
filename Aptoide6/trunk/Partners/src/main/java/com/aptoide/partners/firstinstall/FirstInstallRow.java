package com.aptoide.partners.firstinstall;

/**
 * Created by rmateus on 23-01-2015.
 */
public class FirstInstallRow {

    private boolean selected;
    private String icon;
    private String appName;
    private long downloads;
    private long id;
    private long size;
    private boolean animate;

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
}
