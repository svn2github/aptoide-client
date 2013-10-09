package cm.aptoide.pt.database.table;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-10-2013
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public class TableApkMetadata {

    public static String getName(){
        return "apk_metadata";
    }

    public final static String COLUMN_ID = "id";
    public final static String COLUMN_SIZE = "size";
    public final static String COLUMN_DOWNLOADS = "downloads";
    public final static String COLUMN_LIKES = "likes";
    public final static String COLUMN_DISLIKES = "dislikes";
    public final static String COLUMN_RATING = "rating";
    public final static String COLUMN_VERNAME = "vername";
    public final static String COLUMN_NAME = "name";

}
