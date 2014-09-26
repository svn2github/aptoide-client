package cm.aptoide.ptdev.database;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.fragments.Home;
import cm.aptoide.ptdev.fragments.HomeBucket;
import cm.aptoide.ptdev.fragments.HomeCategory;
import cm.aptoide.ptdev.fragments.HomeFooter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.Collection;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.model.MultiStoreItem;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;
import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-10-2013
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class Database {


    public SQLiteDatabase getDatabaseInstance() {
        return database;
    }
    private static final String SELECT_START = "select * from (select apk.package_name as package_name, repo.name as repo_name, apk.name as name, apk.downloads as downloads, apk.rating as rating, apk.price as price, apk.date as date,apk.id_apk as _id, apk.downloads as count,apk.version_name as version_name,'0' as type, apk.icon as icon, repo.icons_path as iconpath, repo.theme as theme from apk";
    private final SQLiteDatabase database;


    public Database(SQLiteDatabase database) {
        this.database = database;

    }

    public List<SQLiteStatement> compileStatements(List<String> statements) {

        ArrayList<SQLiteStatement> SQLiteStatements = new ArrayList<SQLiteStatement>(statements.size());

        for (String string : statements) {
            //Log.d("TAG1", "Compiling statement: " + string);
            SQLiteStatements.add(database.compileStatement(string));
        }

        return SQLiteStatements;
    }

 /*   public void endTransaction() {
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void startTransaction() {
        database.beginTransaction();
    }

    public void yield() {
        if (database.yieldIfContendedSafely(1000)) {
            Log.d("TAG1", "Database yielded");
        }
    }
*/
    public void addToExcludeUpdate(long itemId) {
        ContentValues values = new ContentValues();
        Cursor c = null;
        try{
            c = getApkInfo(itemId);
            if(c.moveToFirst()){
                String apkid = c.getString(c.getColumnIndex("package_name"));
                int vercode = c.getInt(c.getColumnIndex("version_code"));
                String name = c.getString(c.getColumnIndex("name"));
                String iconpath = c.getString(c.getColumnIndex("iconpath"));
                String icon = c.getString(c.getColumnIndex("icon"));
                values.put(Schema.Excluded.COLUMN_PACKAGE_NAME, apkid);
                values.put(Schema.Excluded.COLUMN_VERCODE, vercode);
                values.put(Schema.Excluded.COLUMN_NAME, name);
                values.put(Schema.Excluded.COLUMN_ICONPATH, iconpath+icon);
                database.insert(Schema.Excluded.getName(), null, values);
            }
        }finally {
            if(c!=null) c.close();
        }
    }

    public void deleteFromExcludeUpdate(String apkid, int vercode) {
        database.delete(Schema.Excluded.getName(),
                "package_name = ? and vercode = ?",
                new String[] { apkid, vercode + "" });
    }

    public Cursor getExcludedApks() {
        return database.query(Schema.Excluded.getName(), null, null, null, null, null, null);
    }

    public Cursor getServers() {
        Cursor c = database.rawQuery("select * from repo where is_user = 1", null);
        c.getCount();
        return c;
    }

    private ContentValues BuildContentValuesFromStore(Store store){
        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_AVATAR, store.getAvatar());
        values.put(Schema.Repo.COLUMN_DOWNLOADS, store.getDownloads());
        values.put(Schema.Repo.COLUMN_THEME, store.getTheme());
        values.put(Schema.Repo.COLUMN_DESCRIPTION, store.getDescription());
        values.put(Schema.Repo.COLUMN_ITEMS, store.getItems());
        values.put(Schema.Repo.COLUMN_VIEW, store.getView());

        if (store.getLogin() != null) {
            values.put(Schema.Repo.COLUMN_USERNAME, store.getLogin().getUsername());
            values.put(Schema.Repo.COLUMN_PASSWORD, store.getLogin().getPassword());
        }
        values.put(Schema.Repo.COLUMN_IS_USER, true);
        return values;
    }

    public long insertStore(Store store) {
        ContentValues values = BuildContentValuesFromStore(store);

        if(store.getId()!=0)
            values.put(Schema.Repo.COLUMN_ID, store.getId());
        values.put(Schema.Repo.COLUMN_URL, store.getBaseUrl());
        values.put(Schema.Repo.COLUMN_NAME, store.getName());

        return database.insert(Schema.Repo.getName(), "error", values);
    }

    public long updateStore(Store store) {
        ContentValues values = BuildContentValuesFromStore(store);
        return database.update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(store.getId())});
    }

    public Cursor getCategories(long storeid, long parentid) {

        Cursor c = null;
        if(storeid!=-1){
            c = database.rawQuery("select cat.name as name, id_real_category as _id, apps_count as count, null as version_name, '1' as type, null as icon, null as iconpath, repo.theme as theme, repo.name as repo_name, repo.items as items from category as cat join repo on cat.id_repo = repo.id_repo where cat.id_repo = ? and id_category_parent = ? order by order_column desc, _id asc", new String[]{String.valueOf(storeid), String.valueOf(parentid) });
            c.getCount();

            if(parentid==0){

                MatrixCursor cursor = new MatrixCursor(new String[]{"name", "_id", "count", "version_name", "type", "icon", "iconpath", "theme", "repo_name"});
                String[] itemsList = null;

                if (c.moveToFirst()) {
                    String items = c.getString(c.getColumnIndex("items"));
                    if (items != null) {
                        itemsList = items.split(",");
                    }
                }


                for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

                    if (itemsList != null && ArrayUtils.contains(itemsList, c.getString(0).replaceAll(" ", "_").toLowerCase(Locale.ENGLISH))
                            || itemsList == null){
                        cursor.addRow(new String[]{c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8)});
                    }

                }

                if(itemsList!=null && ArrayUtils.contains(itemsList, "Latest Likes".replaceAll(" ", "_").toLowerCase(Locale.ENGLISH))){
                    cursor.addRow(new String[]{"Latest Likes", "502" , "0", "", "1","","","",""});
                }

                if(itemsList!=null && ArrayUtils.contains(itemsList, "Latest Comments".replaceAll(" ", "_").toLowerCase(Locale.ENGLISH))){
                    cursor.addRow(new String[]{"Latest Comments", "503" , "0", "", "1","","","",""});
                }


                c.close();
                return cursor;
            }
        }
        return c;
    }

    public Cursor getApks(long storeid, long parentid, StoreActivity.SortObject sortObject) {
        String sort = "name";

        switch (sortObject.getSort()){
            case NAMEAZ:
                //this sort does the same as the one in use
                //sort = "case when name like '[A-Z]%' then 1 else 0 end asc,name collate nocase";

                sort = "name collate nocase";
                break;
            case NAMEZA:
                //This sort puts numbers first then Asian then Z-A
                //sort = "name+0<>0 DESC,name collate nocase desc";

                // this sort puts Asian first then Z-A and numbers last
                sort = "name collate nocase desc";
                break;
            case DATE:
                sort = "date desc";
                break;
            case DOWNLOADS:
                sort = "count desc";
                break;
            case RATING:
                sort = "rating desc";
                break;
            case PRICE:
                sort = "price desc";
                break;
        }

        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);
        /*Cursor c;

        if(storeid != -1){

            if(parentid==500 || parentid == 501){
                String order = " name";
                if(parentid==500){
                    order = "downloads desc";
                }else if (parentid == 501){
                    order = "date desc";
                }

                c = database.rawQuery("select * from (select apk.package_name as package_name, apk.name as name, apk.downloads as downloads, apk.rating as rating, apk.price as price, apk.date as date,apk.id_apk as _id, apk.downloads as count,apk.version_name as version_name,'0' as type, apk.icon as icon, repo.icons_path as iconpath, repo.theme as theme from apk join category_apk on apk.id_apk = category_apk.id_apk join repo on apk.id_repo = repo.id_repo where category_apk.id_real_category = ? and category_apk.id_repo = ? " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " order by apk.sdk asc ) group by package_name order by " + order , new String[]{String.valueOf(parentid),String.valueOf(storeid)  });
            }else{
                c = database.rawQuery("select * from (select apk.package_name as package_name, apk.name as name, apk.downloads as downloads, apk.rating as rating, apk.price as price, apk.date as date,apk.id_apk as _id, apk.downloads as count,apk.version_name as version_name,'0' as type, apk.icon as icon, repo.icons_path as iconpath, repo.theme as theme from apk join category_apk on apk.id_apk = category_apk.id_apk join repo on apk.id_repo = repo.id_repo where category_apk.id_real_category = ? and category_apk.id_repo = ? " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " order by apk.sdk asc ) group by package_name order by " + sort, new String[]{String.valueOf(parentid),String.valueOf(storeid)  });
            }

        }else{
            c = database.rawQuery("select * from (select apk.package_name as package_name, apk.name as name, apk.downloads as downloads, apk.rating as rating, apk.price as price, apk.date as date,apk.id_apk as _id, apk.downloads as count,apk.version_name as version_name,'0' as type, apk.icon as icon, repo.icons_path as iconpath, repo.theme as theme from apk, repo where apk.id_repo = repo.id_repo " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " order by apk.sdk) group by package_name order by " + sort, null);
        }
        */
        String[] selectionArgs =null;

        StringBuilder query = new StringBuilder(SELECT_START);
        if(storeid != -1){
            query.append(" join category_apk on apk.id_apk = category_apk.id_apk join repo on apk.id_repo = repo.id_repo where category_apk.id_real_category = ? and category_apk.id_repo = ? "+
                    (filterCompatible ? "and apk.is_compatible='1'": "") + " " +
                    (filterMature ? "and apk.mature='0'": "") +
                    " order by apk.sdk asc ) group by package_name order by ");
            if(parentid==500){
                query.append("downloads desc");
            }else if (parentid == 501){
                query.append("date desc");
            }else{
                query.append(sort);
            }
            selectionArgs=new String[]{String.valueOf(parentid),String.valueOf(storeid)};
        }else{
            query.append(", repo where apk.id_repo = repo.id_repo " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " order by apk.sdk) group by package_name order by " + sort);
        }
        Cursor c = database.rawQuery(query.toString(),selectionArgs);
        c.getCount();

        return c;
    }

    public Cursor getAllStoreApks(long storeid, StoreActivity.SortObject sortObject) {
        String sort = "name";

        switch (sortObject.getSort()){
            case NAMEAZ:
                sort = "name collate nocase";
                break;
            case NAMEZA:
                sort = "name collate nocase desc";
                break;
            case DATE:
                sort = "date desc";
                break;
            case DOWNLOADS:
                sort = "downloads desc";
                break;
            case RATING:
                sort = "rating desc";
                break;
            case PRICE:
                sort = "price desc";
                break;
        }

        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);
        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        Cursor c = database.rawQuery(SELECT_START+" join repo on apk.id_repo = repo.id_repo where apk.id_repo = ? " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " order by apk.sdk asc ) group by package_name order by " + sort, new String[]{String.valueOf(storeid)  });
        c.getCount();

        return c;
    }



    public Cursor getStore(long storeid) {
        Cursor c = database.rawQuery("select * from repo where id_repo = ?", new String[]{String.valueOf(storeid)});
        c.getCount();
        return c;
    }

    public void updateAppsCount(long repoId) {
        Cursor c = database.rawQuery("select id_real_category from category where id_category_parent = 0 and id_repo = ?", new String[]{String.valueOf(repoId)});
        while (c.moveToNext()) {
            getAppsCount(c.getLong(0), repoId);
        }
        c.close();
    }

    private synchronized long getAppsCount(long id_real_category, long id_repo) {
        long apps = 0;
        Cursor c = database.rawQuery("select id_real_category from category where id_category_parent = ? and id_repo = ?", new String[]{String.valueOf(id_real_category), String.valueOf(id_repo)});
        if(c.getCount()>0){
            while (c.moveToNext()){
                apps += getAppsCount(c.getLong(0), id_repo);
            }
        }else{
            c.close();
            c = database.rawQuery("select count(id_real_category) from category_apk where id_real_category = ? and id_repo = ?", new String[]{String.valueOf(id_real_category),String.valueOf(id_repo)});
            if(c.moveToFirst()){
                apps = c.getInt(0);
            }
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(Schema.Category.COLUMN_APPS_COUNT, apps);
        database.update(Schema.Category.getName(), values, "id_real_category = ? and id_repo = ?", new String[]{String.valueOf(id_real_category),String.valueOf(id_repo)});

        return apps;
    }


    public Boolean clearStore(long id_store) {
        //Log.d("Aptoide-", "Clearing " + id_store);

        database.beginTransaction();

            Cursor c = database.rawQuery("select id_real_category, name from category where id_repo = ? ", new String[]{String.valueOf(id_store)});


            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

                if((c.getString(1).equals("Top Apps") && c.getString(1).equals("Latest Apps"))){
                    database.delete("category_apk", "id_real_category=? and id_repo = ?", new String[]{String.valueOf(c.getLong(0)),String.valueOf(id_store)});
                    //Log.d("Aptoide-", "Deleting " + c.getLong(0));
                }
            }

            c.close();

        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }

    public Boolean clearCategories(long id_store) {
        //Log.d("Aptoide-", "Deleting categories " + id_store);

        database.beginTransaction();

        database.delete("category", "id_repo = ? and id_real_category != 500 and id_real_category != 501", new String[]{String.valueOf(id_store)});

        database.setTransactionSuccessful();
        database.endTransaction();

        return true;
    }

    public Boolean removeStores(Set<Long> checkedItems) {
        //Log.d("Aptoide-", "Deleting " + checkedItems);

        database.beginTransaction();
        for(Long id_store : checkedItems){
            Cursor c = database.rawQuery("select id_real_category from category where id_repo = ?", new String[]{String.valueOf(id_store)});

            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
                database.delete("category_apk","id_real_category=? and id_repo = ?", new String[]{String.valueOf(c.getLong(0)),String.valueOf(id_store)});
                //Log.d("Aptoide-", "Deleting " + c.getLong(0));
            }
            c.close();
            database.delete("category"," id_repo = ? ", new String[]{String.valueOf(id_store)});
            database.delete("apk"," id_repo = ? ", new String[]{String.valueOf(id_store)});
            database.delete("repo","id_repo = ? ", new String[]{String.valueOf(id_store)});

        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }



    public Cursor getUpdates() {

        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);
        boolean filterUpdates = AptoideUtils.getSharedPreferences().getBoolean("showAllUpdates", true);
        //final long startTime = System.currentTimeMillis();

        //select  apk.package_name, (installed.version_code < apk.version_code) as is_update, apk.version_code as repoVC from apk join installed on  apk.package_name = installed.package_name group by apk.package_name, is_update order by is_update desc
        Cursor c = database.rawQuery("select * from (select " +
                " (installed.version_code < apk.version_code) as is_update, " +
                " (installed.signature = apk.signature or apk.signature='')  as signature_valid, " +
                " apk.id_apk as _id, " +
                " apk.name as name, " +
                " apk.version_code as version_code, " +
                " apk.package_name as package_name, " +
                " apk.downloads as count, " +
                " apk.version_name as version_name, " +
                " installed.version_name as installed_version_name, " +
                " apk.icon as icon, " +
                " repo.name as repo_name, " +
                " repo.icons_path as iconpath " +
                " from apk inner " +
                " join installed on apk.package_name = installed.package_name " +
                " join repo on apk.id_repo = repo.id_repo where not exists (select 1 from excluded as d where apk.package_name = d.package_name ) "+
                    (filterCompatible ? " and apk.is_compatible='1' " :" "   ) +
                    (filterMature ?  " and apk.mature='0' " : " "  )   +
                    (filterUpdates? " and (installed.signature = apk.signature or apk.signature='') " : " ") +
                " order by apk.version_code asc) as firstQuery " +
                " group by package_name, is_update order by is_update desc, name collate nocase ", null);

        //final long endTime = System.currentTimeMillis();
        c.getCount();

        return c;
    }

    public List<InstalledPackage> getStartupInstalled() {

        ArrayList<InstalledPackage> installedPackages = new ArrayList<InstalledPackage>();

        Cursor c = database.rawQuery("select package_name, version_code from installed", null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            installedPackages.add(new InstalledPackage(null, c.getString(0), c.getInt(1), null, null));
        }
        c.close();

        return installedPackages;
    }

    public void insertInstalled(InstalledPackage apk) {

        ContentValues values = new ContentValues();

        values.put(Schema.Installed.COLUMN_APKID, apk.getPackage_name());
        values.put(Schema.Installed.COLUMN_NAME, apk.getName());
        values.put(Schema.Installed.COLUMN_VERCODE, apk.getVersion_code());
        values.put(Schema.Installed.COLUMN_VERNAME, apk.getVersion_name());
        values.put(Schema.Installed.COLUMN_SIGNATURE, apk.getSignature());
        database.insert(Schema.Installed.getName(), null, values);

    }

/*    public void removeStore(long id) {
        Set<Long> aLong = new HashSet<Long>();
        aLong.add(id);
        removeStores(aLong);
    }*/

    public long insertServer(Server server) {

        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_ICONS_PATH, server.getIconspath());
        values.put(Schema.Repo.COLUMN_WEBSERVICES_PATH, server.getWebservicespath());
        values.put(Schema.Repo.COLUMN_APK_PATH, server.getApkpath());
        values.put(Schema.Repo.COLUMN_NAME, server.getName());
        values.put(Schema.Repo.COLUMN_IS_USER, false);
        values.put(Schema.Repo.COLUMN_URL, server.getUrl());
        long id;

        try {
            id = database.insert(Schema.Repo.getName(), null, values);
        }catch (SQLiteException e){
            Cursor c = database.query(Schema.Repo.getName(), new String[]{Schema.Repo.COLUMN_ID}, "url = ?", new String[]{server.getUrl()}, null, null,null );
            id = c.getLong(0);
            c.close();
        }

        return id;
    }

/*    public void insertLegacyServer(Server server, Login login) {

        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_NAME, server.getName());
        values.put(Schema.Repo.COLUMN_IS_USER, true);
        values.put(Schema.Repo.COLUMN_URL, server.getUrl());

        if(login!=null){
            values.put(Schema.Repo.COLUMN_USERNAME, login.getUsername());
            values.put(Schema.Repo.COLUMN_PASSWORD, login.getPassword());
        }

        database.insert(Schema.Repo.getName(), null, values);
    }*/

    public void deleteFeatured(int type){

        //Log.d("Aptoide-Featured", "Deleting featured " + type);

        database.beginTransaction();

        Cursor c = database.query(Schema.Category_Apk.getName(), new String[]{Schema.Featured_Apk.COLUMN_APK_ID}, "id_real_category = ?", new String[]{String.valueOf(type)}, null, null, null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

            Cursor appsCursor = database.query(Schema.Apk.getName(), new String[]{"id_repo"}, "id_apk = ?", new String[]{c.getString(0)}, null,null,null);

            if (appsCursor.moveToFirst()) {
                database.delete(Schema.Repo.getName(), "id_repo = ?", new String[]{appsCursor.getString(0)});
                database.delete(Schema.Apk.getName(), "id_apk = ?", new String[]{c.getString(0)});
                database.delete(Schema.Category_Apk.getName(), "id_apk = ?", new String[]{c.getString(0)});
            }
            appsCursor.close();
        }

        c.close();

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void updateServer(Server server, long repo_id) {

        ContentValues values = new ContentValues();

        if (server.getApkpath() != null) values.put(Schema.Repo.COLUMN_APK_PATH, server.getApkpath());
        if (server.getIconspath() != null) values.put(Schema.Repo.COLUMN_ICONS_PATH, server.getIconspath());
        if (server.getWebservicespath() != null) values.put(Schema.Repo.COLUMN_WEBSERVICES_PATH, server.getWebservicespath());
        if (server.getHash() != null) values.put(Schema.Repo.COLUMN_HASH, server.getHash());
        if (values.size() > 0)
            database.update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(repo_id)});

    }

    public ArrayList<HomeItem> getTopFeatured(int bucketSize) {
        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select apk.package_name as package_name,  apk.sdk, apk.id_apk as id, apk.name as name, apk.downloads as downloads, apk.rating as rating, repo.icons_path as iconpath, apk.icon as icon from category_apk as cat1  join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo where cat1.id_real_category = 511 " + (filterCompatible ? " and apk.is_compatible='1' ": "") + " " +(filterMature ? " and apk.mature='0' ": "") + " order by apk.sdk asc) as apk group by package_name order by id asc", null);

        int i = 0;
        ArrayList<HomeItem> items = new ArrayList<HomeItem>();
        for(c.moveToFirst();!c.isAfterLast() && i < bucketSize ;c.moveToNext()){
            i++;
            String iconPath = c.getString(c.getColumnIndex("iconpath"));
            String icon = c.getString(c.getColumnIndex("icon"));
            long id = c.getLong(c.getColumnIndex("id"));
            items.add(new HomeItem(c.getString(c.getColumnIndex("name")), "0", iconPath + icon, id, c.getString(c.getColumnIndex("downloads")), c.getFloat(c.getColumnIndex("rating")), ""));

        }

        c.close();

        return items;
    }


    public ArrayList<Home> getAllTopFeatured(int bucketSize) {

        ArrayList<Home> items = new ArrayList<Home>();
        fillElementsWithoutFooter(bucketSize, "Top Apps", items, getTopFeatured(Integer.MAX_VALUE), -2);

        return items;
    }



    public Cursor getAllFeaturedGraphics(){
        final boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        final boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select featedchoice.featured_graphic as featgraph, apk.date as date, apk.package_name as package_name, catname.id_real_category as mycatnameid, catname.name as mycatname, apk.id_apk as _id, catparentname.id_real_category as parentid, catparentname.name as catname, apk.name as name, repo.icons_path as iconpath, apk.icon as icon, apk.rating as rating, apk.downloads as downloads from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo left natural join featurededitorschoice as featedchoice where featedchoice.id_apk not null and cat1.id_real_category = 510 and cat2.id_real_category != 510  " + (filterCompatible ? " and apk.is_compatible='1' " : "") + " " + (filterMature ? " and apk.mature='0' " : "") + " order by apk.sdk asc) group by package_name order by  date desc", null);
        c.getCount();

        return c;
    }

    public ArrayList<Home> getFeaturedGraphics(int bucketSize){
        final boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        final boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);


        Cursor c = database.rawQuery("select * from (select featedchoice.highlightorder as highlightorder, featedchoice.featured_graphic as featgraph, apk.date as date, apk.package_name as package_name, catname.id_real_category as mycatnameid, catname.name as mycatname, apk.id_apk as id, catparentname.id_real_category as parentid, catparentname.name as catname, apk.name as name, repo.icons_path as iconpath, apk.icon as icon, apk.rating as rating, apk.downloads as downloads from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo left join featurededitorschoice as featedchoice on featedchoice.id_apk = apk.id_apk where featedchoice.id_apk not null and cat1.id_real_category = 510 and cat2.id_real_category != 510  " + (filterCompatible ? " and apk.is_compatible='1' " : "") + " " + (filterMature ? " and apk.mature='0' " : "") + " order by apk.sdk asc) group by package_name order by  date desc", null);

        int i;
        ArrayList<Home> items = new ArrayList<Home>();
        for(c.moveToFirst(), i= 0 ; !c.isAfterLast() ;c.moveToNext(), i++){
            long id = c.getLong(c.getColumnIndex("id"));
            HomeItem item = new HomeItem(c.getString(c.getColumnIndex("name")), "", c.getString(c.getColumnIndex("featgraph")), id, c.getString(c.getColumnIndex("downloads")), c.getFloat(c.getColumnIndex("rating")), "");
            item.setPriority(c.getInt(c.getColumnIndex("highlightorder")));

            items.add(item);
        }
        c.close();
        ArrayList<Home> ret_items = new ArrayList<Home>();

        if(!items.isEmpty()){
            Collections.shuffle(items, new Random(System.currentTimeMillis()/900000));

            Collections.sort(items, new Comparator<Home>() {
                @Override
                public int compare(Home lhs, Home rhs) {
                    return rhs.getSortPriority() - lhs.getSortPriority();
                }
            });

            for(int j = 0; j < bucketSize && j < items.size();j++){
                ret_items.add(items.get(j));
            }
        }

        return ret_items;
    }

    public synchronized HashMap<String, ArrayList<Home>> getFeatured2(final int editorsChoiceBucketSize) {

        ArrayList<Home> items = new ArrayList<Home>();

        final boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        final boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        ArrayList<Home> featuredGraphics = getFeaturedGraphics(5);

        Cursor c = database.rawQuery("select * from (select apk.date as date, apk.package_name as package_name, catname.id_real_category as mycatnameid, catname.name as mycatname, apk.id_apk as id, catparentname.id_real_category as parentid, catparentname.name as catname, apk.name as name, repo.icons_path as iconpath, apk.icon as icon, apk.rating as rating, apk.downloads as downloads from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo where cat1.id_real_category = 510 and cat2.id_real_category != 510  " + (filterCompatible ? " and apk.is_compatible='1' " : "") + " " + (filterMature ? " and apk.mature='0' " : "") + " order by apk.sdk asc) group by package_name order by  date desc", null);
        c.getCount();

        long[] ids = new long[5];

        //Log.d("Aptoide-Database", "GetFeatured count " + c.getCount());

        for(int i = 0; i!=ids.length && i < featuredGraphics.size() ;i++){
            ids[i] = ((HomeItem)featuredGraphics.get(i)).getId();
        }


        HashMap<String, Integer> collectionIdList = new HashMap<String, Integer>();

        LinkedHashMap<String, ArrayList<HomeItem>> collections = new LinkedHashMap<String, ArrayList<HomeItem>>();

        collections.put("Top Apps", getTopFeatured(editorsChoiceBucketSize * 2));
        collectionIdList.put("Top Apps", -2);

        collections.put("New Editors' Choice", new ArrayList<HomeItem>());
        collectionIdList.put("New Editors' Choice", -1);

        for(c.moveToFirst();!c.isAfterLast() ;c.moveToNext()){
            String collection = "New Editors' Choice";
            if( collections.get(collection).size() < editorsChoiceBucketSize*2 ){
                if(!ArrayUtils.contains(ids,c.getLong(c.getColumnIndex("id")) )) {
                    collections.get(collection).add(new HomeItem(c));
                }
            }
            else {
                break;
            }
        }

        c.moveToFirst();
        for(c.move(editorsChoiceBucketSize*2);!c.isAfterLast();c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));
            if(!collections.containsKey(collection)){
                ArrayList<HomeItem> itemsList = new ArrayList<HomeItem>();
                collections.put(collection, itemsList);
                collectionIdList.put(collection, c.getInt(c.getColumnIndex("parentid")));
            }
        }

        c.moveToFirst();
        for(c.move(editorsChoiceBucketSize*2);!c.isAfterLast()  ;c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));
            if(collections.get(collection).size() < editorsChoiceBucketSize*2 ){
                if(!ArrayUtils.contains(ids,c.getLong(c.getColumnIndex("id")))) {
                    collections.get(collection).add(new HomeItem(c));
                }
            }

        }

        for(String collection : collections.keySet()){
            fillElements(editorsChoiceBucketSize, collection, items, collections.get(collection), collectionIdList.get(collection));
        }

/*        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        HashMap<String, ArrayList<Home>> returnVal = new HashMap<String, ArrayList<Home>>();

        returnVal.put("editorsChoice", items);
        returnVal.put("featuredGraphic", getFeaturedGraphics(5));

        c.close();
        return returnVal;
    }

    private boolean fillElementsWithoutFooter(int editorsChoiceBucketSize, String categoryName, ArrayList<Home> items, ArrayList<HomeItem> inElements, int parentId) {
        if(inElements.isEmpty())
            return false;

        items.add(new HomeCategory(categoryName, parentId));
        while (!inElements.isEmpty()) {
            HomeBucket bucket = new HomeBucket();
            for (int j = 0; j < editorsChoiceBucketSize && !inElements.isEmpty(); j++) {
                bucket.addItem(inElements.remove(0));
            }
            items.add(bucket);
        }
        return true;
    }

    private void fillElements(int editorsChoiceBucketSize, String categoryName, ArrayList<Home> items, ArrayList<HomeItem> inElements, Integer integer) {
        if(fillElementsWithoutFooter(editorsChoiceBucketSize,categoryName, items, inElements,integer)) {
            items.add(new HomeFooter(integer));
        }
    }
/*

    public synchronized ArrayList<Collection> getFeatured(int editorsChoiceBucketSize) {
        final boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        final boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select apk.date as date, apk.package_name as package_name, catname.id_real_category as mycatnameid, catname.name as mycatname, apk.id_apk as id, catparentname.id_real_category as parentid, catparentname.name as catname, apk.name as name, repo.icons_path as iconpath, apk.icon as icon, apk.rating as rating, apk.downloads as downloads from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo left natural join featurededitorschoice as featedchoice where featedchoice.id_apk is null and cat1.id_real_category = 510 and cat2.id_real_category != 510  " + (filterCompatible ? " and apk.is_compatible='1' " : "") + " " + (filterMature ? " and apk.mature='0' " : "") + " order by apk.sdk asc) group by package_name order by  date desc", null);
        c.getCount();

        HashMap<String, Integer> tempList2 = new HashMap<String, Integer>();

        HashMap<String, ArrayList<HomeItem>> tempList = new HashMap<String, ArrayList<HomeItem>>();
        for(c.move(editorsChoiceBucketSize);!c.isAfterLast();c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));
            if(!tempList.containsKey(collection)){
                ArrayList<HomeItem> itemsList = new ArrayList<HomeItem>();
                tempList.put(collection, itemsList);
                tempList2.put(collection, c.getInt(c.getColumnIndex("parentid")));
            }
        }

        c.moveToFirst();
        for(c.move(editorsChoiceBucketSize);!c.isAfterLast();c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));

            if(tempList.get(collection).size() < editorsChoiceBucketSize){
                tempList.get(collection).add(new HomeItem(c));
            }
            else
                break;
        }

        tempList.put("New Editors' Choice", new ArrayList<HomeItem>());
        tempList2.put("New Editors' Choice", -1);

        int i=0;

        for(c.moveToFirst();!c.isAfterLast() && i < editorsChoiceBucketSize ;c.moveToNext()){
            String collection = "New Editors' Choice";
            i++;
            if(tempList.get(collection).size() < editorsChoiceBucketSize){
                tempList.get(collection).add(new HomeItem(c));
            }
            else
                break;
        }

        c.close();

        ArrayList<Collection> items = new ArrayList<Collection>();
        for(String collection : tempList.keySet()){
            Collection collection1 = new Collection();
            collection1.setName(collection);
            collection1.setAppsList(tempList.get(collection));
            collection1.setParentId(tempList2.get(collection));
            if(!collection1.getAppsList().isEmpty()){
                items.add(collection1);
            }
        }

        Collections.sort(items, new Comparator<Collection>() {
            @Override
            public int compare(Collection lhs, Collection rhs) {
                return lhs.getParentId() - rhs.getParentId();
            }
        });

        return items;
    }
*/

    public ArrayList<HomeItem> getCollectionFeatured(int id, int bucketSize) {
        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select apk.package_name as package_name , catname.name as mycatname, catname.id_real_category as mycatnameid, apk.id_apk as id, apk.downloads as downloads, apk.rating as rating, repo.icons_path as iconpath, apk.icon as icon, cat1.id_apk, catparentname.id_real_category as parentid, catname.name as catname,catname.id_real_category as catnameid, apk.name as name from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo where cat1.id_real_category = 510 and cat2.id_real_category != 510 and catname.id_real_category = ? "+ (filterCompatible ? " and apk.is_compatible='1' ": "") + " " +(filterMature ? " and apk.mature='0' ": "") +" order by apk.sdk asc) group by package_name", new String[]{String.valueOf(id)});
        ArrayList<HomeItem> items = new ArrayList<HomeItem>();

        int i = 0;
        for(c.moveToFirst();!c.isAfterLast() && i < bucketSize;c.moveToNext()){
            i++;
            items.add(new HomeItem(c));
        }
        c.close();

        return items;
    }


    public ArrayList<Collection> getNewEditorsFeatured() {

        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select apk.package_name as package_name, apk.date as timestamp, catname.name as mycatname, catname.id_real_category as mycatnameid, apk.id_apk as id, catparentname.id_real_category as parentid, catparentname.name as catname, apk.name as name, repo.icons_path as iconpath, apk.icon as icon, apk.rating as rating, apk.downloads as downloads from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo where cat1.id_real_category = 510 and cat2.id_real_category != 510 " +(filterCompatible ? " and apk.is_compatible='1'": "") + " " +(filterMature ? " and apk.mature='0'": "") + " order by apk.sdk asc) group by package_name"  , null);
        HashMap<String, Integer> tempList2 = new HashMap<String, Integer>();

        HashMap<String, ArrayList<HomeItem>> tempList = new HashMap<String, ArrayList<HomeItem>>();
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            int weeks = Weeks.weeksBetween(
                        new DateTime(new Date(c.getLong(c.getColumnIndex("timestamp")))).withDayOfWeek(1),
                        DateTime.now().withDayOfWeek(1)
                ).getWeeks();

            String collection = weeks + "";
            if(!tempList.containsKey(collection)){
                ArrayList<HomeItem> itemsList = new ArrayList<HomeItem>();
                tempList.put(collection, itemsList);
                tempList2.put(collection, -100);
            }
        }

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            int weeks =  Weeks.weeksBetween(
                        new DateTime(new Date(c.getLong(c.getColumnIndex("timestamp")))).withDayOfWeek(1),
                        DateTime.now().withDayOfWeek(1)
                ).getWeeks();

            String collection = weeks + "";
            tempList.get(collection).add(new HomeItem(c));
        }
        c.close();

        //Log.d("Aptoide-EditorsChoice", tempList.toString());

        ArrayList<Collection> items = new ArrayList<Collection>();
        for(String collection : tempList.keySet()){
            Collection collection1 = new Collection();
            collection1.setName(collection);
            collection1.setWeeks(Integer.parseInt(collection));
            collection1.setAppsList(tempList.get(collection));
            collection1.setParentId(tempList2.get(collection));
            collection1.setHasMore(false);
            items.add(collection1);
        }

        Collections.sort(items, new Comparator<Collection>() {
            @Override
            public int compare(Collection lhs, Collection rhs) {
                return lhs.getWeeks()-rhs.getWeeks();
            }
        });

        return items;
    }


    public ArrayList<Collection> getSpecificFeatured(int id, int editorsChoiceBucketSize) {

        if(id == -1){
            return getNewEditorsFeatured();
        }

        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);
        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        Cursor c = database.rawQuery("select * from (select apk.package_name as package_name, catname.name as mycatname, catname.id_real_category as mycatnameid, apk.id_apk as id, apk.downloads as downloads, apk.rating as rating, repo.icons_path as iconpath, apk.icon as icon, catname.id_real_category as parentid, catname.name as catname, apk.name as name from category_apk as cat1 join category_apk as cat2 on cat1.id_apk = cat2.id_apk join category as catname on cat2.id_real_category = catname.id_real_category and catname.id_repo  = 0 join category as catparentname on catname.id_category_parent = catparentname.id_real_category and catparentname.id_repo = 0 join apk on cat1.id_apk = apk.id_apk join repo on apk.id_repo = repo.id_repo where cat1.id_real_category = 510 and cat2.id_real_category != 510 and catparentname.id_real_category = ? " +(filterCompatible ? " and apk.is_compatible='1'": "") + " " +(filterMature ? " and apk.mature='0'": "") + " order by apk.sdk asc) group by package_name" , new String[]{String.valueOf(id)});
        HashMap<String, Integer> tempList2 = new HashMap<String, Integer>();

        ArrayList<String> tempList3 = new ArrayList<String>();

        HashMap<String, ArrayList<HomeItem>> tempList = new HashMap<String, ArrayList<HomeItem>>();
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));
            if(!tempList.containsKey(collection)){
                ArrayList<HomeItem> itemsList = new ArrayList<HomeItem>();
                tempList.put(collection, itemsList);
                tempList2.put(collection, c.getInt(c.getColumnIndex("parentid")));
            }
        }


        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            String collection = c.getString(c.getColumnIndex("catname"));

            if(tempList.get(collection).size() < editorsChoiceBucketSize){
                tempList.get(collection).add(new HomeItem(c));
            }else{
                tempList3.add(collection);
            }

        }

        c.close();

        ArrayList<Collection> items = new ArrayList<Collection>();
        for(String collection : tempList.keySet()){
            Collection collection1 = new Collection();
            collection1.setName(collection);
            collection1.setAppsList(tempList.get(collection));
            collection1.setParentId(tempList2.get(collection));
            collection1.setHasMore(tempList3.contains(collection));
            items.add(collection1);
        }

        return items;
    }

    public Cursor getSearchResults(String searchQuery, StoreActivity.Sort sortEnum) {

        boolean filterCompatible = AptoideUtils.getSharedPreferences().getBoolean("hwspecsChkBox", true);

        boolean filterMature = AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true);


        String sort = "apk.name";

        switch (sortEnum){
            case NAMEAZ:
                sort = "apk.name collate nocase";
                break;
            case NAMEZA:
                sort = "apk.name desc collate nocase";
                break;
            case DATE:
                sort = "apk.date desc";
                break;
            case DOWNLOADS:
                sort = "apk.downloads desc";
                break;
            case RATING:
                sort = "apk.rating desc";
                break;
            case PRICE:
                sort = "apk.price desc";
                break;
        }


        Cursor c = database.rawQuery("select  apk.price, apk.date, apk.name as name, apk.id_apk as _id, apk.downloads as count, apk.version_name as version_name,'0' as type, apk.icon as icon, repo.icons_path as iconpath, apk.rating as rating from apk  join repo on apk.id_repo = repo.id_repo where apk.name LIKE '%" + searchQuery + "%' " +(filterCompatible ? "and apk.is_compatible='1'": "") + " " +(filterMature ? "and apk.mature='0'": "") + " group by apk.package_name order by " + sort, null);
        c.getCount();
        return c;
    }

    public Cursor getApkInfo(long id) {
        Cursor c = database.rawQuery("select apk.price as price, repo.apk_path as apk_path, apk.path as path, apk.md5, apk.version_code as version_code, apk.package_name as package_name, apk.name as name, apk.version_name as version_name, apk.rating as rating, apk.downloads as downloads, apk.sdk as sdk, apk.screen as screen, apk.icon as icon, repo.icons_path as iconpath, repo.name as reponame from apk join repo on apk.id_repo = repo.id_repo where apk.id_apk = ?", new String[]{String.valueOf(id)});
        c.moveToFirst();

        return c;
    }

    public void setLatestTimestamp(long repoId, long timestamp) {

        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_LATEST_TIMESTAMP, timestamp);

        database.update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(repoId)});
    }

    public void setTopTimestamp(long repoId, long timestamp) {

        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_TOP_TIMESTAMP, timestamp);

        database.update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(repoId)});
    }

    public void deleteLatest(long id) {

        Cursor c = database.rawQuery("select id_real_category from category where id_repo = ? and name = ?", new String[]{String.valueOf(id), "Latest Apps"});
        if (c.moveToFirst()) {
            long id_real_category = c.getLong(0);

            c.close();
            String[] whereArgs = new String[]{String.valueOf(id_real_category), String.valueOf(id)};
            database.delete(Schema.Category_Apk.getName(), "id_real_category = ? and id_repo = ?",whereArgs );

            database.delete(Schema.Category.getName(), "id_real_category = ? and id_repo = ?",whereArgs);
        }
        c.close();


    }

    public void deleteTop(long id) {

        Cursor c = database.rawQuery("select id_real_category from category where id_repo = ? and name = ?", new String[]{String.valueOf(id), "Top Apps"});

        if (c.moveToFirst()) {
            long id_real_category = c.getLong(0);

            c.close();
            String[] whereArgs = new String[]{String.valueOf(id_real_category), String.valueOf(id)};
            database.delete(Schema.Category_Apk.getName(), "id_real_category = ? and id_repo = ?",whereArgs );

            database.delete(Schema.Category.getName(), "id_real_category = ? and id_repo = ?",whereArgs);
        }
        c.close();


    }

/*    public void putCategoriesIds(HashMap<Integer, Integer> categoriesIds, long repoId) {
        Cursor c = database.rawQuery("select id_real_category, id_real_category from category where id_repo = ? ", new String[]{String.valueOf(repoId)});


        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            categoriesIds.put(c.getInt(0), c.getInt(1));
        }

    }*/

    public void deleteApk(String packageName, int versionCode, long repoId) {

        Cursor c = database.rawQuery("select id_apk from apk where package_name = ? and version_code = ? and id_repo = ?",new String[]{packageName, String.valueOf(versionCode), String.valueOf(repoId)});

        if(c.moveToFirst()){
            database.delete(Schema.Category_Apk.getName(), "id_apk = ?", new String[]{String.valueOf(c.getLong(c.getColumnIndex("id_apk")))});
        }

        database.delete(Schema.Apk.getName(), "package_name = ? and version_code = ? and id_repo = ?", new String[]{packageName, String.valueOf(versionCode), String.valueOf(repoId)});
        c.close();
    }

    public void deleteInstalledApk(String packageName) {

        database.delete(Schema.Installed.getName(), "package_name = ?", new String[]{packageName});

    }

    public void insertCategory(String name, int parent, int real_id, int order, long repoId) {

        //Log.d("Aptoide-Database", "Inserting category " +  name + " on " + repoId );

        ContentValues values = new ContentValues();

        values.put(Schema.Category.COLUMN_ID_PARENT, parent);
        values.put(Schema.Category.COLUMN_NAME, name);
        values.put(Schema.Category.COLUMN_RID, real_id);
        values.put(Schema.Category.COLUMN_REPO_ID, repoId);
        values.put(Schema.Category.COLUMN_ORDER, order);
        try{
            database.insert(Schema.Category.getName(), null, values);
        }catch (SQLiteConstraintException e){

        }
    }

    public void insertRollbackAction(RollBackItem rollBackItem) {
        ContentValues values = new ContentValues();

        values.put(Schema.RollbackTbl.COLUMN_NAME, rollBackItem.getName());
        values.put(Schema.RollbackTbl.COLUMN_APKID, rollBackItem.getPackageName());
        values.put(Schema.RollbackTbl.COLUMN_VERSION, rollBackItem.getVersion());
        values.put(Schema.RollbackTbl.COLUMN_PREVIOUS_VERSION, rollBackItem.getPreviousVersion());
        values.put(Schema.RollbackTbl.COLUMN_ICONPATH, rollBackItem.getIconPath());
        values.put(Schema.RollbackTbl.COLUMN_MD5, rollBackItem.getMd5());
        values.put(Schema.RollbackTbl.COLUMN_ACTION, (rollBackItem.getAction() == null ? "" : rollBackItem.getAction().toString()));
        values.put(Schema.RollbackTbl.COLUMN_CONFIRMED, 0);
        values.put(Schema.RollbackTbl.COLUMN_REPO, rollBackItem.getRepoName());

        Cursor cursor = database.rawQuery("select 1 from rollbacktbl  where package_name = ? and confirmed = 0", new String[]{rollBackItem.getPackageName()});
        if (cursor.getCount() == 0) {
            database.insert(Schema.RollbackTbl.getName(), null, values);
        } else {
            database.update(Schema.RollbackTbl.getName(), values, "package_name = ? and confirmed = 0", new String[]{rollBackItem.getPackageName()});
        }
        cursor.close();
    }


    public String getRollbackRepo(String packageName){
        String repo = null;

        Cursor c = database.rawQuery("select reponame from rollbacktbl where package_name = ? and action='Updated' or action='Installed'", new String[]{ packageName });

        if(c.moveToFirst()){
            repo = c.getString(c.getColumnIndex("reponame"));
        }

        c.close();
        return repo;
    }


    public void confirmRollBackAction(String packageName, String oldAction, String newAction) {
        ContentValues values = new ContentValues();
        values.put(Schema.RollbackTbl.COLUMN_TIMESTAMP, Long.toString(System.currentTimeMillis() / 1000));
        values.put(Schema.RollbackTbl.COLUMN_ACTION, newAction);
        values.put(Schema.RollbackTbl.COLUMN_CONFIRMED, 1);

        database.update(Schema.RollbackTbl.getName(), values, "package_name = ? and action = ?", new String[]{packageName, oldAction});
        //Log.d("InstalledBroadcastReceiver", "Trying to update " + packageName + " with action completed " + newAction + " RESULT: " + ((result == 1) ? "Success" : "Fail"));

    }

    public String getUnistallingActionMd5(String packageName) {
        Cursor cursor = database.rawQuery("select md5 from rollbacktbl  where package_name = ? and action = ?", new String[]{packageName, RollBackItem.Action.UNINSTALLING.toString()});
        int resultsCount = cursor.getCount();

        String md5 = null;
        if (resultsCount != 0) {
            cursor.moveToFirst();
            md5 = cursor.getString(0);
        }
        cursor.close();
        return md5;
    }

    public boolean updateDowngradingAction(String packageName) {

        //boolean success = false;

        ContentValues values = new ContentValues();
        values.put(Schema.RollbackTbl.COLUMN_ACTION, RollBackItem.Action.DOWNGRADING.toString());
        int updatedRows = database.update(Schema.RollbackTbl.getName(), values, "package_name = ? and action = ?", new String[]{packageName, ""});

        //Cursor cursor = database.rawQuery("UPDATE rollbacktbl SET action = ? WHERE package_name = ? and action = ?", new String[]{RollBackItem.Action.DOWNGRADING.toString(), packageName, "''"});

/*
        if(updatedRows > 0) {
            success = true;
        }
        */

        return updatedRows > 0;

    }

    public String getNotConfirmedRollbackAction(String packageName) {
        Cursor cursor = database.rawQuery("select action from rollbacktbl where package_name = ? and confirmed = ?", new String[]{packageName, Integer.toString(0)});
        int resultsCount = cursor.getCount();

        String action = null;
        if(resultsCount != 0) {
            cursor.moveToFirst();
            action = cursor.getString(0);
        }
        cursor.close();
        return action;
    }

    public Cursor getRollbackActions() {

        Cursor c = database.rawQuery("select rowid as _id, icon_path, version, previous_version, name, strftime('%d-%m-%Y', datetime(timestamp, 'unixepoch')) as cat_timestamp, action, package_name, md5, rollbacktbl.timestamp as real_timestamp from rollbacktbl  where rollbacktbl.confirmed = 1 order by rollbacktbl.timestamp desc", null);
        c.getCount();

        return c;
    }

    public void updateApkName(Apk apk, SQLiteStatement statement) {

        StatementHelper.bindAllArgsAsStrings(statement, new String[]{apk.getName(), apk.getPackageName(), String.valueOf(apk.getRepoId())} );

        statement.execute();

    }

    public boolean existsServer(String repoUrl) {
        Cursor c = null;
        try {
            c = database.rawQuery("select 1 from repo where url = ?", new String[]{repoUrl});
            return c.moveToFirst();
        } finally {
            if (c != null) c.close();
        }
    }

    private long getApkFromPackage(String sql, String[] selectionArgs){
        Cursor c = null;
        try {
            c = database.rawQuery(sql, selectionArgs);
            if(c.moveToFirst()){
                return c.getInt(0);
            }else{
                return 0;
            }
        } finally {
            if (c != null) c.close();
        }
    }

    public long getApkFromPackage(String param) {
        return getApkFromPackage(
                "select id_apk from apk where package_name = ? and is_compatible = 1 order by version_code ",
                new String[]{param});
    }


    public long getApkFromPackage(String param, String repo) {
        return getApkFromPackage(
                "select id_apk from apk join repo where package_name = ? and is_compatible = 1 and repo.name = ? order by version_code ",
                new String[]{param, repo});
    }

    public Cursor getScheduledDownloads() {

        Cursor c = null;

        try {
            c = database.rawQuery("select rowid as _id, * from scheduled", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

/*    public boolean isScheduledDownload(String repo_name, String md5) {

        Cursor c = null;
        yield();
        try {
            c = database.query(Schema.Scheduled.getName(), new String[]{"1"}, "repo_name = ? and md5 = ?", new String[]{repo_name, md5},null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null)
                c.close();
        }

        boolean isScheduledDOwnload = c != null && c.moveToFirst();

        if (c != null) {
            c.close();
        }

        return isScheduledDOwnload;

    }*/

    public void scheduledDownloadIfMd5(String apkid, String md5, String vername, String repoName, String name, String icon) {
        if (md5 != null) {
            insertScheduledDownload(apkid, md5, vername, repoName, name, icon);
            Toast.makeText(Aptoide.getContext(), R.string.added_to_scheduled , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Aptoide.getContext(), R.string.please_wait, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertScheduledDownload(String apkid, String md5, String vername, String repoName, String name, String icon) {

        Cursor c = database.query(Schema.Scheduled.getName(), null,
                "repo_name = ? and md5 = ?",
                new String[] { repoName, md5 + "" }, null, null, null);

        if (c.moveToFirst()) {
            c.close();
            return;
        }

        c.close();

        ContentValues values = new ContentValues();
        values.put(Schema.Scheduled.COLUMN_NAME, name);
        values.put(Schema.Scheduled.COLUMN_PACKAGE_NAME, apkid);
        values.put(Schema.Scheduled.COLUMN_VERSION_NAME, vername);
        values.put(Schema.Scheduled.COLUMN_REPO, repoName);
        values.put(Schema.Scheduled.COLUMN_ICON, icon);
        values.put(Schema.Scheduled.COLUMN_MD5, md5);
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
        sPref.edit().putBoolean("schTrigger", true).commit();

        database.insert(Schema.Scheduled.getName(), null, values);
    }

    public void deleteScheduledDownload(String id) {
        database.delete(Schema.Scheduled.getName(), "md5 = ?", new String[] { id });
    }

    public void deleteScheduledDownloadByPackageName(String id) {
        database.delete(Schema.Scheduled.getName(), "package_name = ?", new String[] { id });
    }

    public void deleteRollbackItems(){
        database.delete(Schema.RollbackTbl.getName(), null, null);
    }

    public void setFailedRepo(long repoId) {

        ContentValues values = new ContentValues();
        values.put(Schema.Repo.COLUMN_FAILED, 1);
        database.update(Schema.Repo.getName(), values, Schema.Repo.COLUMN_ID + " = ?", new String[]{String.valueOf(repoId)});

    }

    public boolean isStoreError(long storeid) {

        Cursor c = database.rawQuery("select 1 from repo where id_repo = ? and is_failed = 1 and hash IS NULL", new String[]{String.valueOf(storeid)});

        boolean isStoreError = c.moveToFirst();
        c.close();

        return isStoreError;
    }

    public ArrayList<Number> getFailedStores() {

        ArrayList<Number> ids = new ArrayList<Number>();
        Cursor c = database.rawQuery("select id_repo from repo where is_failed = 1 and hash IS NULL", null);


        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            ids.add(c.getLong(c.getColumnIndex("id_repo")));
        }

        c.close();

        return ids;
    }

 /*   public boolean isRepoParsed(long storeId) {

        Cursor c = database.rawQuery("select 1 from repo where id_repo = ? and hash IS NOT NULL", new String[]{String.valueOf(storeId)});

        boolean result = c.moveToFirst();

        c.close();
        return result;
    }

    public void clearInstalled() {
        database.delete(Schema.Installed.getName(), null, null);
    }
*/
    public MultiStoreItem[] getOtherReposVersions(long id, String packageName, String version, String repoName, int versionCode){

        Cursor c = database.rawQuery("select * from (select apk.downloads as downloads, apk.version_code as version_code, apk.version_name as version, id_apk as id, repo.name as name from apk join repo on apk.id_repo = repo.id_repo where package_name = ? and repo.is_user = 1 and is_compatible = 1 order by apk.sdk) group by version, name", new String[]{packageName});
        ArrayList<MultiStoreItem> items = new ArrayList<MultiStoreItem>();
        items.add(new MultiStoreItem(version, repoName, versionCode, packageName));
        ArrayList<MultiStoreItem> itemsTemp = new ArrayList<MultiStoreItem>();

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

            String apkVersion = c.getString(c.getColumnIndex("version"));
            String apkRepoName = c.getString(c.getColumnIndex("name"));
            //long apkId = c.getLong(c.getColumnIndex("id"));
            int apkVersionCode = c.getInt(c.getColumnIndex("version_code"));
            int downloads = c.getInt(c.getColumnIndex("downloads"));
            if(!repoName.equals(apkRepoName) || versionCode!=apkVersionCode) {
                itemsTemp.add(new MultiStoreItem(apkVersion, apkRepoName, apkVersionCode, packageName, downloads));
            }
        }

        Collections.sort(itemsTemp, new Comparator<MultiStoreItem>() {
            @Override
            public int compare(MultiStoreItem lhs, MultiStoreItem rhs) {
                return lhs.getVersionCode()-rhs.getVersionCode();
            }
        });

        items.addAll(itemsTemp);

        c.close();

        return items.toArray(new MultiStoreItem[items.size()]);
    };

    public void deleteFeaturedGraphics() {
        database.delete(Schema.FeaturedEditorsChoice.getName(), null,null);
    }
}
