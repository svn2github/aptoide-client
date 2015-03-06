package cm.aptoide.ptdev;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-10-2013
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseProvider extends ContentProvider {


    SQLiteDatabase sqLiteDatabase;



    @Override
    public boolean onCreate() {

        sqLiteDatabase = Aptoide.getDb();




        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {



        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
