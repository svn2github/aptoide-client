package cm.aptoide.pt.dev.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "aptoide.db", null, 15);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE apk (column1 text unique, column2 text, column3 text, column4 text, column5 text, column6 text, column7 text, column8 text, column9 text, column10 text, column11 text, column12 text, column13 text, column14 text, column15 text, column16 text, column17 text, column18 text, column19 text, column20 text)");
        db.execSQL("CREATE TABLE category (column1 text, column2 text, column3 text, column4 text, column5 text, column6 text, column7 text, column8 text, column9 text, column10 text, column11 text, column12 text, column13 text, column14 text, column15 text, column16 text, column17 text, column18 text, column19 text, column20 text)");
        db.execSQL("CREATE TABLE category_rel (column1 text, column2 text, UNIQUE (column1, column2))");
        db.execSQL("CREATE index indexname on apk (column1)");
        db.execSQL("CREATE index indexname1 on category (column1)");
        db.execSQL("CREATE index indexname2 on category_rel (column1)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
