package cm.aptoide.pt.dev.model;

import android.database.sqlite.SQLiteStatement;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 07-10-2013
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class Apk {

    public abstract void insert(SQLiteStatement[] insertStatement);
    public abstract String[] getStatements();

    public void bindAllArgsAsStrings(SQLiteStatement statement, String[] bindArgs) {
        if (bindArgs != null) {
            for (int i = bindArgs.length; i != 0; i--) {
                statement.bindString(i, bindArgs[i - 1]);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
}
