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

            uniques = @TableDefinition.Composite_Unique(
                    fields = {Apk.COLUMN_APKID, Apk.COLUMN_VERCODE, Apk.COLUMN_REPO_ID}),
            indexes = {
                    @TableDefinition.Index(index_name = "nameIdx",
                            keys = @TableDefinition.Key(field = Apk.COLUMN_NAME, descending = true)),
                    @TableDefinition.Index(index_name = "repoIdx",
                            keys = @TableDefinition.Key(field = Apk.COLUMN_REPO_ID))
            })
    public static class Apk {

        public static String getName(){
            return "apk";
        }

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true) public static final String COLUMN_ID = "id_apk";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_APKID = "package_name";

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "") public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "") public final static String COLUMN_VERNAME = "version_name";

        @ColumnDefinition(type = SQLType.INTEGER, defaultValue = "0") public final static String COLUMN_VERCODE = "version_code";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_ICON = "icon";

        @ColumnDefinition(type = SQLType.INTEGER) public static final String COLUMN_REPO_ID = Repo.COLUMN_ID;


    }

    @TableDefinition(
            indexes = {
                    @TableDefinition.Index(index_name = "installedIdx",
                            keys = @TableDefinition.Key(field = Installed.COLUMN_APKID))
            })
    public static class Installed {

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true) public static final String COLUMN_ID = "id_installed";
        @ColumnDefinition(type = SQLType.TEXT, unique = true, onConflict = OnConflict.REPLACE) public final static String COLUMN_APKID = "package_name";
        @ColumnDefinition(type = SQLType.TEXT ) public final static String COLUMN_NAME = "name";
        @ColumnDefinition(type = SQLType.INTEGER, defaultValue = "0") public final static String COLUMN_VERCODE = "version_code";
        @ColumnDefinition(type = SQLType.INTEGER, defaultValue = "") public final static String COLUMN_VERNAME = "version_name";

        public static String getName() {
            return "installed";
        }
    }



    @TableDefinition(

            uniques = @TableDefinition.Composite_Unique(
                    fields = {Category.COLUMN_NAME, Category.COLUMN_REPO_ID, Category.COLUMN_ID_PARENT})
            )

    public static class Category {

        @ColumnDefinition(type = SQLType.INTEGER, autoIncrement = true, primaryKey = true)
        public final static String COLUMN_ID = "id_category";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.INTEGER)
        public static final String COLUMN_REPO_ID = Repo.COLUMN_ID;

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_ID_PARENT = "id_category_parent";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_APPS_COUNT = "apps_count";


        public static String getName() {
            return "category";
        }
    }

    @TableDefinition(

            uniques = @TableDefinition.Composite_Unique(
                    fields = {Category_Apk.COLUMN_CATEGORY_ID, Category_Apk.COLUMN_APK_ID})
    )

    public static class Category_Apk {

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_CATEGORY_ID = Category.COLUMN_ID;

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_APK_ID = Apk.COLUMN_ID;

        public static String getName() {
            return "category_apk";

        }
    }



    public static class Repo {

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_URL = "url";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ICONS_PATH = "icons_path";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_WEBSERVICES_PATH = "webservices_path";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_HASH = "hash";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_THEME = "theme";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_AVATAR = "avatar_url";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_DOWNLOADS = "downloads";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_DESCRIPTION = "description";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_VIEW = "list";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ITEMS = "items";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_LATEST_TIMESTAMP = "latest_timestamp";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_TOP_TIMESTAMP = "top_timestamp";

        @ColumnDefinition(type = SQLType.BOOLEAN)
        public final static String COLUMN_IS_USER = "is_user";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true)
        public final static String COLUMN_ID = "id_repo";

        public static String getName() {
            return "repo";
        }
    }


}
