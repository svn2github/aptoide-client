package cm.aptoide.ptdev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.RepoAddedEvent;
import cm.aptoide.ptdev.model.Store;
import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.List;

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

        Log.d("Aptoide-", String.valueOf(storeid));

        return database.rawQuery("select name, id_category, apps_count from category as cat where id_repo = ? and id_category_parent = ? order by apps_count desc", new String[]{String.valueOf(storeid), String.valueOf(parentid) });
    }

    public Cursor getStore(long storeid) {
        return database.rawQuery("select * from repo where id_repo = ?", new String[]{String.valueOf(storeid)});
    }

    public void updateAppsCount(long repoId) {

        Cursor c = database.rawQuery("select id_category from category where id_category_parent = 0 and id_repo = ?", new String[]{String.valueOf(repoId)});

        while(c.moveToNext()){
            getApps(c.getLong(0), repoId);
        }

        c.close();

    }

    private long getApps(long id_category, long id_repo) {

        long apps = 0;
        Cursor c = database.rawQuery("select id_category from category where id_category_parent = ? and id_repo = ?", new String[]{String.valueOf(id_category), String.valueOf(id_repo)});
        if(c.getCount()>0){
            while (c.moveToNext()){
                apps += getApps(c.getLong(0), id_repo);
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
}

