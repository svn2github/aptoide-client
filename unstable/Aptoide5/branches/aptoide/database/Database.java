package cm.aptoide.pt.dev.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.pt.dev.Aptoide;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-10-2013
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class Database {


    private static final Database INSTANCE = new Database();

    private final SQLiteDatabase database;


    private Database() {
        this.database = new DatabaseHelper(Aptoide.getContext()).getWritableDatabase();
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public SQLiteStatement[] compileStatement2(String[] statements) {

        SQLiteStatement[] SQLiteStatements = new SQLiteStatement[statements.length];

        for(int i = 0; i < statements.length; i++){
            Log.d("TAG1", "Compiling statement: " + statements[i]);
            SQLiteStatements[i] = database.compileStatement(statements[i]);
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

    public void get(String md5) {
        Cursor c = database.query("apk", new String[]{"column1"}, "column1 = ?",new String[]{md5},null,null,null);

        Log.d("TAG1", "Count: " + c.getCount() + "");

        c.close();

    }
}
