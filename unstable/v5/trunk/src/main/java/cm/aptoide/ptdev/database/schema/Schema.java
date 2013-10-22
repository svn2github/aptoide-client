package cm.aptoide.ptdev.database.schema;


import cm.aptoide.ptdev.database.schema.SQLType;
import cm.aptoide.ptdev.database.schema.anotations.ColumnDefinition;
import cm.aptoide.ptdev.database.schema.anotations.TableDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 04-10-2013
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */

//@Target(ElementType.ANNOTATION_TYPE = SQLtype)
public class Schema {


    @TableDefinition(
            primaryKey = Apk.COLUMN_ID,
            uniques = @TableDefinition.Composite_Unique(
                    fields = {Apk.COLUMN_APKID, Apk.COLUMN_VERCODE, Apk.COLUMN_REPO_ID}),
            indexes = {
                    @TableDefinition.Index(index_name = "byName",
                            keys = @TableDefinition.Key(field = Apk.COLUMN_NAME, descending = true),
                            unique = true)
            })
    public static class Apk {

        public static String getName(){
            return "apk";
        }

        @ColumnDefinition(type = SQLType.TEXT) public static final String COLUMN_ID = "id_apk";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_APKID = "apkid";

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "") public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.INTEGER, defaultValue = "0") public final static String COLUMN_VERCODE = "vercode";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_ICON = "icon";

        @ColumnDefinition(type = SQLType.INTEGER) public static final String COLUMN_REPO_ID = "id_repo";

    }


    public static class Repo {

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_URL = "url";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ICONS_PATH = "iconspath";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_HASH = "hash";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_LATEST_TIMESTAMP = "latest_timestamp";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_TOP_TIMESTAMP = "top_timestamp";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true)
        public final static String COLUMN_ID = "id_repo";

        public static String getName() {
            return "repo";
        }
    }


}
