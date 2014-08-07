package cm.aptoide.ptdev.fragments;

import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 27-11-2013
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class HomeItem implements Home{


    private final String categoryString;
    private boolean recommended;
    private String md5;
    private String repoName;

    public void setPriority(int priority) {
        this.priority = priority;
    }

    private int priority = 0;


    public long getId() {
        return id;
    }

    private final long id;

    public String getCategory() { return category; }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getItemsSize() {
        return 0;
    }

    @Override
    public int getSortPriority() {
        return priority;
    }

    @Override
    public int getParentId() {
        return 0;
    }


    public String getDownloads() { return downloads; }

    public float getRating() { return rating; }

    private String name;
    private String icon;
    private String category;
    private String downloads;
    private float rating;

    public HomeItem(String name, String category, String icon, long id, String downloads, float rating, String categoryString) {
        this.category = category;
        this.icon = icon;
        this.name = name;
        this.id = id;
        this.downloads = downloads;
        this.rating = rating;
        this.categoryString = categoryString;
    }
    public HomeItem(Cursor c) {
        String iconPath = c.getString(c.getColumnIndex("iconpath"));
        String icon = c.getString(c.getColumnIndex("icon"));
        this.category = c.getString(c.getColumnIndex("mycatnameid"));
        this.icon = iconPath + icon;
        this.name = c.getString(c.getColumnIndex("name"));
        this.id = c.getLong(c.getColumnIndex("id"));
        this.downloads = c.getString(c.getColumnIndex("downloads"));
        this.rating = c.getFloat(c.getColumnIndex("rating"));
        this.categoryString = c.getString(c.getColumnIndex("mycatname"));
    }

    public boolean isRecommended() {
        return recommended;
    }

    public String getMd5() {
        return md5;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getCategoryString() {
        return categoryString;
    }


}
