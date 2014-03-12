package cm.aptoide.ptdev.configuration;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 14-10-2013
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class Constants {

    public static final int DATABASE_VERSION = 21;
    public static final String LOGIN_USER_ID 	= "useridLogin";
    public static final String LOGIN_PASSWORD 	= "passwordLogin";
    public static final String LOGIN_USER_LOGIN 	= "usernameLogin";
    public static final String LOGIN_USER_TOKEN = "usernameToken";
    public static final String LOGIN_USER_USERNAME = "userName";
    public static final String LOGIN_DEFAULT_REPO = "defaultRepo";

    public static final String WEBINSTALL_HOST = "amqp.webservices.aptoide.com";
    public static final String WEBINSTALL_QUEUE_NAME = "queueName";
    public static final String WEBINSTALL_SERVICE_RUNNING = "wiServiceRunning";
    public static final String WEBINSTALL_QUEUE_EXCLUDED =  "wiQueueExcluded";
    public static final String WEBINSTALL_SYNC_AUTHORITY = "cm.aptoide.ptdev.StubProvider";
    public static final long WEBINSTALL_SYNC_POLL_FREQUENCY = 360;

}
