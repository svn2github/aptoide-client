package cm.aptoide.ptdev.model;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-10-2013
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public class Server {

    private String name;
    private String url;
    private String hash;
    private String webservicespath;
    private String apkpath;
    private String screenspath;
    private String iconspath;
    private String basepath;
    public Login login;

    public String getName() {
        return name;
    }

    public Server setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Server setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public Server setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public void setWebservicespath(String webservicespath) {
        this.webservicespath = webservicespath;
    }

    public String getWebservicespath() {
        return webservicespath;
    }

    public void setApkpath(String apkpath) {
        this.apkpath = apkpath;
    }

    public String getApkpath() {
        return apkpath;
    }

    public void setScreenspath(String screenspath) {
        this.screenspath = screenspath;
    }

    public String getScreenspath() {
        return screenspath;
    }

    public void setIconspath(String iconspath) {
        this.iconspath = iconspath;
    }

    public String getIconspath() {
        return iconspath;
    }

    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    public String getBasepath() {
        return basepath;
    }
}
