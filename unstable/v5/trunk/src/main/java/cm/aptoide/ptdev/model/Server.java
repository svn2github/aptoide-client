package cm.aptoide.ptdev.model;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-10-2013
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */
public abstract class Server {

    private String name;
    private String url;
    private String hash;

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

}
