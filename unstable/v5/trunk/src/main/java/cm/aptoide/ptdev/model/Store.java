package cm.aptoide.ptdev.model;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 08-11-2013
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class Store {

    private String name;
    private String downloads;
    private String avatar;
    private String description;
    private String theme;
    private String view;
    private String items;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    long id;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String baseUrl;

    public String getInfoXmlUrl() {
        return baseUrl + "info.xml";
    }

    public String getLatestXmlUrl() {
        return baseUrl + "latest.xml";
    }

    public String getTopXmlUrl() {
        return baseUrl + "top.xml";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDownloads(String downloads) {
        this.downloads = downloads;
    }

    public String getDownloads() {
        return downloads;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getItems() {
        return items;
    }
}
