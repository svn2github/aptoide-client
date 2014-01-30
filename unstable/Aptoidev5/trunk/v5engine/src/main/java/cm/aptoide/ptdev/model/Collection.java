package cm.aptoide.ptdev.model;

import cm.aptoide.ptdev.fragments.HomeItem;

import java.util.ArrayList;

/**
 * Created by rmateus on 28-01-2014.
 */
public class Collection {
    private boolean expanded = false;
    private String name;
    private int marginBottom;
    private ArrayList<HomeItem> appsList;
    private int parentId;
    private boolean hasMore;
    private boolean expanded2;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }


    public ArrayList<HomeItem> getAppsList() {
        return appsList;
    }

    public void setAppsList(ArrayList<HomeItem> appsList) {
        this.appsList = appsList;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public boolean isExpanded2() {
        return expanded2;
    }

    public void setExpanded2(boolean expanded2) {
        this.expanded2 = expanded2;
    }
}
