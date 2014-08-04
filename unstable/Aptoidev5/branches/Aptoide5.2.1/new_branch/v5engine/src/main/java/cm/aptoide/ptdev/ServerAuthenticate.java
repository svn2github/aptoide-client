package cm.aptoide.ptdev;

/**
 * Created by rmateus on 30-12-2013.
 */
public interface ServerAuthenticate {
    public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
    public String userSignIn(final String user, final String pass, String authType) throws Exception;
}
