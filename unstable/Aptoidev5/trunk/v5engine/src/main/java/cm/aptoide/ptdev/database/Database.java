package cm.aptoide.ptdev.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.model.Store;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-10-2013
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class Database {


    private final SQLiteDatabase database;


    public Database(SQLiteDatabase database) {
        this.database = database;
        database.rawQuery("pragma synchronous = 0", null);
    }



    public List<SQLiteStatement> compileStatements(List<String> statements) {

        ArrayList<SQLiteStatement> SQLiteStatements = new ArrayList<SQLiteStatement>(statements.size());

        for(String string : statements){
            Log.d("TAG1", "Compiling statement: " + string);
            SQLiteStatements.add(database.compileStatement(string));
        }

        return SQLiteStatements;
    }


    public void endTransaction() {
        database.setTransactionSuccessful();
        database.endTransaction();

    }

    public void startTransaction() {
        database.beginTransaction();
    }

    public void yield() {
        if(database.yieldIfContendedSafely(1000)){
            Log.d("TAG1", "Database yielded");
        };
    }



    public Cursor getServers() {
        Cursor c = database.rawQuery("select * from repo where is_user = 1", null);
        c.getCount();
        return c;
    }



    public long insertStore(Store store) {
        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_URL, store.getBaseUrl());
        values.put(Schema.Repo.COLUMN_NAME, store.getName());
        values.put(Schema.Repo.COLUMN_AVATAR, store.getAvatar());
        values.put(Schema.Repo.COLUMN_DOWNLOADS, store.getDownloads());
        values.put(Schema.Repo.COLUMN_THEME, store.getTheme());
        values.put(Schema.Repo.COLUMN_DESCRIPTION, store.getDescription());
        values.put(Schema.Repo.COLUMN_ITEMS, store.getItems());

        if (store.getLogin() != null) {
            values.put(Schema.Repo.COLUMN_USERNAME, store.getLogin().getUsername());
            values.put(Schema.Repo.COLUMN_PASSWORD, store.getLogin().getPassword());
        }

        Log.d("Aptoide-Inserting store", String.valueOf(store.getBaseUrl() + " " + store.getLogin()!=null));

        values.put(Schema.Repo.COLUMN_IS_USER, true);

        return database.insert(Schema.Repo.getName(), "error", values);
    }

    public Cursor getCategories(long storeid, long parentid) {

        Cursor c = database.rawQuery("select name as name, id_category as _id, apps_count as count, null as version_name, '1' as type, null as icon, null as iconpath  from category as cat where id_repo = ? and id_category_parent = ? order by count desc", new String[]{String.valueOf(storeid), String.valueOf(parentid) });
        c.getCount();

        return c;

    }

    public Cursor getApks(long storeid, long parentid, StoreActivity.SortObject sortObject) {


        String sort = "apk.name";

        switch (sortObject.getSort()){

            case NAME:
                sort = "apk.name collate nocase";
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

        Cursor c = database.rawQuery("select apk.name, apk.downloads, apk.rating, apk.price, apk.date ,apk.id_apk as _id, apk.downloads as count,apk.version_name ,'0' as type, apk.icon, repo.icons_path as iconpath from apk join category_apk on apk.id_apk = category_apk.id_apk join repo on apk.id_repo = repo.id_repo where category_apk.id_category = ? order by " + sort, new String[]{String.valueOf(parentid) });
        c.getCount();

        return c;

    }



    public Cursor getStore(long storeid) {
        Cursor c = database.rawQuery("select * from repo where id_repo = ?", new String[]{String.valueOf(storeid)});
        c.getCount();
        return c;
    }

    public void updateAppsCount(long repoId) {

        Cursor c = database.rawQuery("select id_category from category where id_category_parent = 0 and id_repo = ?", new String[]{String.valueOf(repoId)});

        while(c.moveToNext()){
            getAppsCount(c.getLong(0), repoId);
        }

        c.close();

    }

    private long getAppsCount(long id_category, long id_repo) {

        long apps = 0;
        Cursor c = database.rawQuery("select id_category from category where id_category_parent = ? and id_repo = ?", new String[]{String.valueOf(id_category), String.valueOf(id_repo)});
        if(c.getCount()>0){
            while (c.moveToNext()){
                apps += getAppsCount(c.getLong(0), id_repo);
            }
        }else{
            c = database.rawQuery("select count(id_category) from category_apk where id_category = ?", new String[]{String.valueOf(id_category)});
            if(c.moveToFirst()){
                apps = c.getInt(0);
            }
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(Schema.Category.COLUMN_APPS_COUNT, apps);
        database.update(Schema.Category.getName(), values, "id_category = ?", new String[]{String.valueOf(id_category)});

        return apps;
    }


    public Boolean clearStore(long id_store) {

        Log.d("Aptoide-", "Deleting " + id_store);

        database.beginTransaction();

            Cursor c = database.rawQuery("select id_category, name from category where id_repo = ? ", new String[]{String.valueOf(id_store)});

            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){

                if((c.getString(1).equals("Top Apps") && c.getString(1).equals("Latest Apps"))){
                    database.delete("category_apk", "id_category=?", new String[]{String.valueOf(c.getLong(0))});
                    Log.d("Aptoide-", "Deleting " + c.getLong(0));
                }
            }
            c.close();

            //database.delete("apk"," id_repo = ?", new String[]{String.valueOf(id_store)});



        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }

    public Boolean removeStores(Set<Long> checkedItems) {

        Log.d("Aptoide-", "Deleting " + checkedItems);

        database.beginTransaction();
        for(Long id_store : checkedItems){
            Cursor c = database.rawQuery("select id_category from category where id_repo = ?", new String[]{String.valueOf(id_store)});

            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
                database.delete("category_apk","id_category=?", new String[]{String.valueOf(c.getLong(0))});
                Log.d("Aptoide-", "Deleting " + c.getLong(0));
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

    public Cursor getInstalled() {
        Cursor c = database.rawQuery("select 0 as _id , 'Installed' as name, null as count, null as version_name, null as icon, null as iconpath, null as package_name union select  apk.id_apk as _id,apk.name, apk.downloads as count, installed.version_name , apk.icon as icon, repo.icons_path as iconpath, apk.package_name as package_name from apk inner join installed on apk.package_name = installed.package_name join repo on apk.id_repo = repo.id_repo group by apk.package_name", null);
        c.getCount();
        return c;
    }

    public Cursor getUpdates() {
        Cursor c = database.rawQuery("select 0 as _id , 'Updates' as name, null as count, null as version_name, null as icon, null as iconpath union select apk.id_apk as _id,apk.name,  apk.downloads as count,apk.version_name , apk.icon as icon, repo.icons_path as iconpath from apk inner join installed on apk.package_name = installed.package_name join repo on apk.id_repo = repo.id_repo  where installed.version_code < apk.version_code group by apk.package_name",null);
        c.getCount();
        return c;
    }

    public List<InstalledPackage> getStartupInstalled() {

        ArrayList<InstalledPackage> installedPackages = new ArrayList<InstalledPackage>();

        Cursor c = database.rawQuery("select package_name, version_code from installed", null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            installedPackages.add(new InstalledPackage(null, null, c.getString(0), c.getInt(1), null));
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
        database.insert(Schema.Installed.getName(), null, values);

    }

    public void removeStore(long id) {
        Set<Long> aLong = new HashSet<Long>();
        aLong.add(id);
        removeStores(aLong);
    }

    public long insertServer(Server server) {

        ContentValues values = new ContentValues();

        values.put(Schema.Repo.COLUMN_ICONS_PATH, server.getIconspath());
        values.put(Schema.Repo.COLUMN_WEBSERVICES_PATH, server.getWebservicespath());
        values.put(Schema.Repo.COLUMN_NAME, server.getName());
        values.put(Schema.Repo.COLUMN_IS_USER, false);
        values.put(Schema.Repo.COLUMN_URL, server.getUrl());
        long id;

        try{
            id = database.insert(Schema.Repo.getName(), null, values);
        }catch (SQLiteException e){
            Cursor c = database.query(Schema.Repo.getName(), new String[]{Schema.Repo.COLUMN_ID}, "url = ?", new String[]{server.getUrl()}, null, null,null );
            id = c.getLong(0);
            c.close();
        }

        return id;
    }

    public void deleteFeatured(int type){


        database.beginTransaction();

        Cursor c = database.query(Schema.Featured_Apk.getName(), new String[]{Schema.Featured_Apk.COLUMN_APK_ID}, "type = ?", new String[]{String.valueOf(type)}, null, null, null);

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){

            Cursor appsCursor = database.query(Schema.Apk.getName(), new String[]{"id_repo"}, "id_apk = ?", new String[]{c.getString(0)}, null,null,null);

            if(appsCursor.moveToFirst()){
                database.delete(Schema.Repo.getName(), "id_repo = ?", new String[]{appsCursor.getString(0)});
                database.delete(Schema.Apk.getName(), "id_apk = ?", new String[]{c.getString(0)});
            }
            appsCursor.close();
        }



        c.close();

        database.delete(Schema.Featured_Apk.getName(), "type = ?", new String[]{String.valueOf(type)});


        database.setTransactionSuccessful();
        database.endTransaction();


    }

    public void updateServer(Server server, long repo_id) {

        ContentValues values = new ContentValues();

        if (server.getIconspath() != null) values.put(Schema.Repo.COLUMN_ICONS_PATH, server.getIconspath());
        if (server.getWebservicespath() != null)
            values.put(Schema.Repo.COLUMN_WEBSERVICES_PATH, server.getWebservicespath());
        if (server.getHash() != null) values.put(Schema.Repo.COLUMN_HASH, server.getHash());
        if (values.size() > 0)
            database.update(Schema.Repo.getName(), values, "id_repo = ?", new String[]{String.valueOf(repo_id)});

    }

    public ArrayList<HomeItem> getFeatured(int type, int editorsChoiceBucketSize) {

        Cursor c = database.rawQuery("select apk.id_apk, featured_apk.category, apk.name, apk.icon, repo.icons_path   from apk join featured_apk on apk.id_apk=featured_apk.id_apk join repo on apk.id_repo = repo.id_repo where featured_apk.type = ?", new String[]{String.valueOf(type)});
        ArrayList<HomeItem> items = new ArrayList<HomeItem>();
        int size = c.getCount();
        int itemsToAdd = size - ( size % editorsChoiceBucketSize);
        int i = 0;
        for(c.moveToFirst();!c.isAfterLast() && i <  itemsToAdd;c.moveToNext()){
            i++;
            String name = c.getString(c.getColumnIndex("name"));
            String category = c.getString(c.getColumnIndex("category"));
            String icon = c.getString(c.getColumnIndex("icon"));
            String iconpath = c.getString(c.getColumnIndex("icons_path"));
            items.add(new HomeItem(name, category, iconpath + icon, c.getLong(c.getColumnIndex(Schema.Apk.COLUMN_ID))));

        }

        c.close();




        return items;
    }

    public Cursor getSearchResults(String searchQuery) {

        Cursor c = database.rawQuery("select apk.name, apk.id_apk as _id, apk.downloads as count,apk.version_name ,'0' as type, apk.icon as icon, repo.icons_path as iconpath from apk  join repo on apk.id_repo = repo.id_repo where apk.name LIKE '%"+ searchQuery+"%' group by apk.package_name order by apk.name ", null);
        c.getCount();
        return c;
    }

    public Cursor getApkInfo(long id) {

        Cursor c = database.rawQuery("select apk.package_name, apk.name as name, apk.version_name, apk.downloads, apk.icon as icon, repo.icons_path as iconpath, repo.name as reponame from apk join repo on apk.id_repo = repo.id_repo where apk.id_apk = ?", new String[]{String.valueOf(id)});
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

        Cursor c = database.rawQuery("select id_category from category where id_repo = ? and name = ?", new String[]{String.valueOf(id), "Latest Apps"} );
        if(c.moveToFirst()){
            long id_category = c.getLong(0);

            c.close();
            database.delete(Schema.Category_Apk.getName(), "id_category = ?", new String[]{String.valueOf(id_category)});

            database.delete(Schema.Category.getName(), "id_category = ?", new String[]{String.valueOf(id_category)});
        }


    }

    public void deleteTop(long id) {

        Cursor c = database.rawQuery("select id_category from category where id_repo = ? and name = ?", new String[]{String.valueOf(id), "Top Apps"} );

        if(c.moveToFirst()){
            long id_category = c.getLong(0);

            c.close();
            database.delete(Schema.Category_Apk.getName(), "id_category = ?", new String[]{String.valueOf(id_category)});

            database.delete(Schema.Category.getName(), "id_category = ?", new String[]{String.valueOf(id_category)});
        }


    }

    public void putCategoriesIds(HashMap<String, Long> categoriesIds, long repoId) {
        Cursor c = database.rawQuery("select name, id_category from category where id_repo = ? ", new String[]{String.valueOf(repoId)});


        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            categoriesIds.put(c.getString(0), c.getLong(1));
        }

    }

    public void deleteApk(String packageName, int versionCode, long repoId) {

        database.delete(Schema.Apk.getName(), "package_name = ? and version_code = ? and id_repo = ?", new String[]{packageName, String.valueOf(versionCode), String.valueOf(repoId)});

    }

    public void deleteInstalledApk(String packageName) {

        database.delete(Schema.Installed.getName(), "package_name = ?", new String[]{packageName});

    }
}


