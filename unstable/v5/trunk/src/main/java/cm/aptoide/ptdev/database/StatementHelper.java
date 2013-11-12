package cm.aptoide.ptdev.database;

import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 11-10-2013
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public class StatementHelper {

    public static void bindAllArgsAsStrings(SQLiteStatement statement, String[] bindArgs){
        if (bindArgs != null) {
            for (int i = bindArgs.length; i != 0; i--) {
                statement.bindString(i, bindArgs[i - 1]);
            }
        }
    }


    public static String getInsertStatment(String table, ArrayList<String> initialValues){

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");

        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');



        int size = (initialValues != null && initialValues.size() > 0)
                ? initialValues.size() : 0;
        if (size > 0) {
            int i = 0;

            for (String colName : initialValues) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName);
                i++;
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? ",?" : "?");
            }
        }
        sql.append(')');
        initialValues.clear();
        return sql.toString();

    }



}
