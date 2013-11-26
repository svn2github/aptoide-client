package cm.aptoide.ptdev.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.InstalledPackage;
import cm.aptoide.ptdev.model.Store;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return database.rawQuery("select * from repo", null);
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
        values.put(Schema.Repo.COLUMN_IS_USER, true);

        return database.insert(Schema.Repo.getName(), "error", values);
    }

    public Cursor getCategories(long storeid, long parentid) {

        return database.rawQuery("select name as name, id_category as _id, apps_count as count, '1' as type from category as cat where id_repo = ? and id_category_parent = ?  union select apk.name, apk.id_apk as _id, '0' ,'0' as type from apk join category_apk on apk.id_apk = category_apk.id_apk where category_apk.id_category = ? order by type desc", new String[]{String.valueOf(storeid), String.valueOf(parentid), String.valueOf(parentid) });

    }

    public Cursor getStore(long storeid) {
        return database.rawQuery("select * from repo where id_repo = ?", new String[]{String.valueOf(storeid)});
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
        return database.rawQuery("select 0 as _id, 'Installed' as name union select id_apk as _id, installed.name from apk inner join installed on apk.package_name = installed.package_name", null);
    }

    public Cursor getUpdates() {
        return database.rawQuery("select 0 as _id , 'Updates' as name union select id_apk as _id, installed.name from apk inner join installed on apk.package_name = installed.package_name where installed.version_code > apk.version_code",null);
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
}


