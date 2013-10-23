package cm.aptoide.ptdev.webservices;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 21-10-2013
 * Time: 12:43
 * To change this template use File | Settings | File Templates.
 */
public class Webservice {


    final String webservicePath;

    public Webservice(String webservicePath) {
        this.webservicePath = webservicePath;
    }

    public GetApkInfo getApkInfo() {
        return new GetApkInfo(webservicePath);
    }

    public CheckUserCredentials getCheckUserCredentials(){
        return new CheckUserCredentials(webservicePath);
    }

    public GetRepositoryInfo getRepositoryInfo() {
        return new GetRepositoryInfo(webservicePath);
    }

    public ListApkComments listApkComments() {
        return new ListApkComments(webservicePath);
    }

    public ListRepositoryChange listRepositoryChange() {
        return new ListRepositoryChange(webservicePath);
    }

    public ListRepositoryComments listRepositoryComments() {
        return new ListRepositoryComments(webservicePath);
    }

    public ListRepositoryLikes listRepositoryLikes() {
        return new ListRepositoryLikes(webservicePath);
    }

    public ListRepositoryLocalApkNames listRepositoryLocalApkNames() {
        return new ListRepositoryLocalApkNames(webservicePath);
    }
}
