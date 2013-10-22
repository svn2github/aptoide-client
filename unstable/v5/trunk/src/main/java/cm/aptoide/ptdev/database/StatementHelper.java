package cm.aptoide.ptdev.database;

import android.content.ContentValues;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 11-10-2013
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public class StatementHelper {

    public static String getStatment(String table, ContentValues initialValues){

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");

        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');



        int size = (initialValues != null && initialValues.size() > 0)
                ? initialValues.size() : 0;
        if (size > 0) {
            int i = 0;




            for (Map.Entry<String, Object> colName : initialValues.valueSet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName.getKey());
                i++;
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? ",?" : "?");
            }
        }
        sql.append(')');
        return sql.toString();

    }

}
