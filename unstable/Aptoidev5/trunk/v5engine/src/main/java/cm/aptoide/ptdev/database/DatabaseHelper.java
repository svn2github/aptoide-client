package cm.aptoide.ptdev.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.database.schema.OnConflict;
import cm.aptoide.ptdev.database.schema.SQLType;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.database.schema.anotations.ColumnDefinition;
import cm.aptoide.ptdev.database.schema.anotations.TableDefinition;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.Server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 04-10-2013
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper sInstance;

    private boolean primaryKeyDefined;


    public static DatabaseHelper getInstance(Context context) {

        synchronized (DatabaseHelper.class){
            if (sInstance == null) {
                sInstance = new DatabaseHelper(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, "aptoide.db", null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            createDb(db);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        dropTables(db, 0);
        dropIndexes(db, 0);

        try {
            createDb(db);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    private void createDb(SQLiteDatabase db) throws IllegalAccessException {
        Class[] db_tables = Schema.class.getDeclaredClasses();

        String sql_stmt = null;

        // Table
        TableDefinition table_definition;
        ColumnDefinition column_definition;
        Field[] table_columns;
        for (Class table : db_tables) {
            primaryKeyDefined = false;
            table_columns = table.getDeclaredFields();
            table_definition = ((TableDefinition) table.getAnnotation(TableDefinition.class));

            sql_stmt = "CREATE TABLE IF NOT EXISTS " + table.getSimpleName().toLowerCase(Locale.ENGLISH) + " (";

            // Table_collumns
            Field column;

            Iterator<Field> it = Arrays.asList(table_columns).iterator();

            while (it.hasNext()) {
                column = it.next();
                column_definition = column.getAnnotation(ColumnDefinition.class);
                column.setAccessible(true);

                sql_stmt += column.get(null) + " " + column.getAnnotation(ColumnDefinition.class).type();

                if (!column_definition.defaultValue().equals("")) {
                    sql_stmt += " DEFAULT \"" + column_definition.defaultValue() + "\"";
                }

                sql_stmt += getColumnConstraints(column_definition);

                if (it.hasNext()) {
                    sql_stmt += ", ";
                }
            }

            // ------------------------- Table primary key -------------------------------


            if (!primaryKeyDefined) {
                if (table_definition != null && table_definition.primaryKey().length != 0) {
                    sql_stmt += ", ";
                    sql_stmt += getPrimaryKey(table_definition);
                }
            } else {
                if (table_definition != null && table_definition.primaryKey().length != 0) {
                    throw new IllegalArgumentException("PRIMARY KEY defined twice, at column and table level!");

                }
            }


            // --------------------------------- Table Unique Composite Fields --------------------------------------------
            if (table_definition != null && table_definition.uniques().length != 0) {
                sql_stmt += ", ";
                sql_stmt += getCompositeUniques(table_definition);
            }
            sql_stmt += ")";

            db.execSQL(sql_stmt);

            // --------------------------------------------------- Indexes Creation ----------------------------------------------

            if (table_definition != null) {
                createTableIndexes(table_definition, table.getSimpleName(), db);
            }


        }
    }

    private void createTableIndexes(TableDefinition table_definition, String table_name, SQLiteDatabase db) {
        TableDefinition.Index[] indexes = table_definition.indexes();
        String indexes_stmt = "";

        Iterator<TableDefinition.Index> iterator = Arrays.asList(indexes).iterator();

        TableDefinition.Index index;
        while (iterator.hasNext()) {
            indexes_stmt = "CREATE ";
            index = iterator.next();

            if (index.unique()) {
                indexes_stmt += "UNIQUE ";
            }

            indexes_stmt += "INDEX IF NOT EXISTS " + index.index_name() + " ON " + table_name + " (";

            TableDefinition.Key[] keys = index.keys();
            Iterator<TableDefinition.Key> keys_iterator = Arrays.asList(keys).iterator();

            TableDefinition.Key key;
            while (keys_iterator.hasNext()) {
                key = keys_iterator.next();
                indexes_stmt += key.field();
                if (key.descending()) {
                    indexes_stmt += " DESC";
                }
                if (keys_iterator.hasNext()) {
                    indexes_stmt += ", ";
                }
            }
            indexes_stmt += ");";

            db.execSQL(indexes_stmt);
        }
    }

    private String getCompositeUniques(TableDefinition table_definition) {
        TableDefinition.Composite_Unique[] uniques = table_definition.uniques();

        String uniques_stmt = "";
        String[] unique_fields;
        Iterator<TableDefinition.Composite_Unique> iterator = Arrays.asList(uniques).iterator();
        while (iterator.hasNext()) {
            uniques_stmt = "UNIQUE (";
            unique_fields = iterator.next().fields();

            Iterator<String> iterator1 = Arrays.asList(unique_fields).iterator();
            while (iterator1.hasNext()) {
                uniques_stmt += iterator1.next();
                if (iterator1.hasNext()) {
                    uniques_stmt += ", ";
                }
            }
            uniques_stmt += ")";

            if (iterator.hasNext()) {
                uniques_stmt += ", ";
            }
        }
        return uniques_stmt;
    }

    private String getPrimaryKey(TableDefinition table_definition) {
        String[] primary_key = table_definition.primaryKey();
        String pk = "PRIMARY KEY (";

        Iterator<String> iterator = Arrays.asList(primary_key).iterator();
        while (iterator.hasNext()) {

            pk += iterator.next();
            if (iterator.hasNext()) {
                pk += ", ";
            }
        }
        pk += ")";
        return pk;
    }

    private String getColumnConstraints(ColumnDefinition column_definition) {
        String column_constraints = "";
        if (column_definition.primaryKey()) {
            if (primaryKeyDefined) {
                throw new IllegalArgumentException("Can only define one PRIMARY KEY, to define a composite PRIMARY KEY, use @TableDefinition annotation");
            }
            primaryKeyDefined = true;
            column_constraints += " PRIMARY KEY";
        }
        if (column_definition.autoIncrement()) {
            if (!column_definition.primaryKey() || column_definition.type() != SQLType.INTEGER) {
                throw new IllegalArgumentException("AUTOINCREMENT only allowed to PRIMARY KEYs with type INTEGER");
            }
            column_constraints += " AUTOINCREMENT";
        }
        if (column_definition.unique()) {
            column_constraints += " UNIQUE";
        }
        if (column_definition.notNull()) {
            column_constraints += " NOT NULL";
        }
        if(!column_definition.onConflict().equals(OnConflict.NONE)){
            column_constraints += " ON CONFLICT " + column_definition.onConflict().name();
        }
        return column_constraints;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DatabaseHelper onUpgrade()");
        ArrayList<Server> oldServers = new ArrayList<Server>();

        if (oldVersion >= 13 && oldVersion <= 20 && Aptoide.getConfiguration().isSaveOldRepos()) {

            try {
                Cursor c = db.query("repo", new String[]{"url", "name", "username", "password"}, null, null, null, null, null);
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

                    Server server = new Server();
                    server.setUrl(c.getString(0));
                    server.setName(c.getString(1));

                    if(c.getString(2)!=null){
                        server.login = new Login();
                        server.login.setUsername(c.getString(2));
                        server.login.setPassword(c.getString(3));
                    }

                    oldServers.add(server);
                }
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (oldVersion >= 21 && Aptoide.getConfiguration().isSaveOldRepos()){
            try {
                Cursor c = db.query("repo", new String[]{"url", "name", "username", "password"}, Schema.Repo.COLUMN_IS_USER +"=?", new String[]{"1"}, null, null, null);
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

                    Server server = new Server();
                    server.setUrl(c.getString(0));
                    server.setName(c.getString(1));

                    if(c.getString(2)!=null){
                        server.login = new Login();
                        server.login.setUsername(c.getString(2));
                        server.login.setPassword(c.getString(3));
                    }

                    oldServers.add(server);
                }
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if( oldVersion == 21 ){
            db.execSQL("ALTER TABLE " +Schema.RollbackTbl.getName()+ " ADD COLUMN reponame TEXT");
            db.delete(Schema.RollbackTbl.getName(), "confirmed = ?", new String[]{"0"});
        }




        dropIndexes(db, oldVersion);
        dropTables(db, oldVersion);

        try {
            createDb(db);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (oldVersion >= 13 && oldVersion <= 21 && Aptoide.getConfiguration().isSaveOldRepos()) {

            for (Server server : oldServers) {
                ContentValues values = new ContentValues();

                values.put(Schema.Repo.COLUMN_NAME, server.getName());
                values.put(Schema.Repo.COLUMN_IS_USER, true);
                values.put(Schema.Repo.COLUMN_URL, server.getUrl());

                if(server.login!=null){
                    values.put(Schema.Repo.COLUMN_USERNAME, server.login.getUsername());
                    values.put(Schema.Repo.COLUMN_PASSWORD, server.login.getPassword());
                }

                db.insert(Schema.Repo.getName(), null, values);
            }
        }





        removeSharedPreferences();

    }

    private void removeSharedPreferences() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext());
        preferences.edit().remove("editorschoiceTimestamp").remove("topappsTimestamp").commit();

    }

    private void dropIndexes(SQLiteDatabase db, int newVersion) {
        Class[] db_tables = Schema.class.getDeclaredClasses();

        String drop_stmt = "";
        for (Class table : db_tables) {
            TableDefinition td = ((TableDefinition) table.getAnnotation(TableDefinition.class));
            if (td != null) {
                for (TableDefinition.Index index : td.indexes()) {
                    drop_stmt = "DROP INDEX IF EXISTS " + index.index_name();
                    db.execSQL(drop_stmt);
                }
            }
        }
    }

    private void dropTables(SQLiteDatabase db, int oldVersion) {
        Class[] db_tables = Schema.class.getDeclaredClasses();


        String drop_stmt;

        boolean dropRollback = oldVersion < 21 ;


        for (Class table : db_tables) {
            String tableName = table.getSimpleName().toLowerCase(Locale.ENGLISH);

            if (dropRollback) {
                drop_stmt = "DROP TABLE IF EXISTS " + tableName;
            } else if (!tableName.equals(Schema.RollbackTbl.getName())) {
                drop_stmt = "DROP TABLE IF EXISTS " + tableName;
            }else{
                continue;
            }

            Log.d("Aptoide-Database", "executing " + drop_stmt);

            db.execSQL(drop_stmt);
        }
    }


}
