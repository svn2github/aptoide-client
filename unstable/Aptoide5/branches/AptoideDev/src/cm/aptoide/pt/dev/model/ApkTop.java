package cm.aptoide.pt.dev.model;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteStatement;
import cm.aptoide.pt.dev.database.Database;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class ApkTop extends Apk {

    public final static String insertStatement = "INSERT INTO apk VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public final static String categoryStatement = "INSERT INTO category VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public final static String category2Statement = "INSERT INTO category_rel VALUES (?,?)";
    private String storeId;


    public ApkTop() {}

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void insert(SQLiteStatement[] statements) {

        try {
            bindAllArgsAsStrings(statements[0], new String[]{getName() + getStoreId(), "asdfasdfasdf", "asdfasdfadf", "asdfasdfafd", "asdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadf", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});
            statements[0].executeInsert();

            bindAllArgsAsStrings(statements[1], new String[]{getName() + getStoreId(), "asdfasdfasdf", "asdfasdfadf", "asdfasdfafd", "asdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadfasdfasdfadf", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""});
            statements[1].executeInsert();

            bindAllArgsAsStrings(statements[2], new String[]{getName(), getStoreId()});
            statements[2].executeInsert();

        } catch (SQLiteConstraintException e) {
            Database.getInstance().get(getName());
        }


    }

    @Override
    public String[] getStatements() {
        return new String[]{insertStatement, categoryStatement, category2Statement};
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
