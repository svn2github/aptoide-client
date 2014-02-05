package cm.aptoide.ptdev.model;

import cm.aptoide.ptdev.downloadmanager.DownloadInfo;
import cm.aptoide.ptdev.downloadmanager.state.EnumState;

import java.io.Serializable;

/**
 * Created by rmateus on 11-12-2013.
 */
public class Download implements Serializable{

    private String name;
    private String version;
    private long id;
    private int progress;
    private int size;
    private long timeLeft;
    private double speed;
    private String icon;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    private String packageName;


    public void setParent(DownloadInfo parent) {
        this.parent = parent;
    }

    private DownloadInfo parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public EnumState getDownloadState() {
        return parent.getStatusState().getEnumState();
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public boolean equals(Object o) {

        if(o instanceof Download){
            return this.getId() == ((Download) o).getId();
        }else{
            return super.equals(o);
        }


    }


}

