package cm.aptoide.ptdev.database.schema;


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

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_DATE = "date";

        @ColumnDefinition(type = SQLType.INTEGER) public final static String COLUMN_DOWNLOADS = "downloads";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_RATING = "rating";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_MATURE = "mature";

        @ColumnDefinition(type = SQLType.INTEGER) public final static String COLUMN_SDK = "sdk";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_SCREEN = "screen";

        @ColumnDefinition(type = SQLType.TEXT) public final static String COLUMN_GLES = "gles";

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "0") public final static String COLUMN_PRICE = "price";

        @ColumnDefinition(type = SQLType.INTEGER) public static final String COLUMN_REPO_ID = Repo.COLUMN_ID;


        @ColumnDefinition(type = SQLType.BOOLEAN) public static final String COLUMN_IS_COMPATIBLE = "is_compatible";
        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "") public final static String COLUMN_SIGNATURE = "signature";




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

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "") public final static String COLUMN_SIGNATURE = "signature";

        public static String getName() {
            return "installed";
        }
    }



    @TableDefinition(
            uniques = @TableDefinition.Composite_Unique(
                    fields = {
                            Category.COLUMN_RID,
                            Category.COLUMN_REPO_ID,
                            Category.COLUMN_ID_PARENT
                    })
            )

    public static class Category {




        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_RID = "id_real_category";

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
                    fields = {Category_Apk.COLUMN_CATEGORY_ID, Category_Apk.COLUMN_APK_ID, Category_Apk.COLUMN_REPO_ID})
    )

    public static class Category_Apk {

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_CATEGORY_ID = Category.COLUMN_RID;

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_APK_ID = Apk.COLUMN_ID;

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_REPO_ID = Repo.COLUMN_ID;

        public static String getName() {
            return "category_apk";
        }

    }

    @TableDefinition(
            uniques = @TableDefinition.Composite_Unique(
                    fields = {Featured_Apk.COLUMN_EDITORS_ID, Category_Apk.COLUMN_APK_ID})
    )

    public static class Featured_Apk {

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true)
        public final static String COLUMN_EDITORS_ID = "id_editors";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_APK_ID = Apk.COLUMN_ID;

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_CATEGORY = "category";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_TYPE = "type";

        public static String getName() {
            return "featured_apk";
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

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_USERNAME = "username";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_PASSWORD = "password";

        @ColumnDefinition(type = SQLType.INTEGER, primaryKey = true, autoIncrement = true)
        public final static String COLUMN_ID = "id_repo";

        public static String getName() {
            return "repo";
        }
    }

    @TableDefinition(
            indexes = {
                    @TableDefinition.Index(index_name = "RollbackIdx",
                            keys = @TableDefinition.Key(field = RollbackTbl.COLUMN_APKID))
            })
    public static class RollbackTbl {

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ACTION = "action";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_TIMESTAMP = "timestamp";

        @ColumnDefinition(type = SQLType.TEXT, defaultValue = "")
        public final static String COLUMN_MD5 = "md5";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ICONPATH = "icon_path";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_APKID = "package_name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_VERSION = "version";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_PREVIOUS_VERSION = "previous_version";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_CONFIRMED = "confirmed";


        public static String getName() {
            return "rollbacktbl";
        }

    }


    public static class Excluded {


        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_PACKAGE_NAME = "package_name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ICONPATH = "iconpath";

        @ColumnDefinition(type = SQLType.INTEGER)
        public final static String COLUMN_VERCODE = "vercode";


        public static String getName() {
            return "excluded";
        }

    }

    public static class Scheduled {

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_PACKAGE_NAME = "package_name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_NAME = "name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_VERSION_NAME = "version_name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_MD5 = "md5";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_REPO = "repo_name";

        @ColumnDefinition(type = SQLType.TEXT)
        public final static String COLUMN_ICON = "icon";

        public static String getName() {
            return "scheduled";
        }

    }

}
