package cm.aptoide.ptdev.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.database.schema.SQLType;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.database.schema.anotations.ColumnDefinition;
import cm.aptoide.ptdev.database.schema.anotations.TableDefinition;

import java.lang.reflect.Field;
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

        synchronized (sInstance){
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

            sql_stmt = "CREATE TABLE " + table.getSimpleName().toLowerCase(Locale.ENGLISH) + " (";

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
                } else {
                    throw new IllegalArgumentException("X--> " + table.getSimpleName() + " table doesn't have a PRIMARY KEY");
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

            indexes_stmt += "INDEX " + index.index_name() + " ON " + table_name + " (";

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
        return column_constraints;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "DatabaseHelper onUpgrade()");

        dropIndexes(db);
        dropTables(db);

        try {
            createDb(db);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void dropIndexes(SQLiteDatabase db) {
        Class[] db_tables = Schema.class.getDeclaredClasses();

        String drop_stmt = "";
        for (Class table : db_tables) {
            TableDefinition td = ((TableDefinition) table.getAnnotation(TableDefinition.class));
            if (td != null) {
                for (TableDefinition.Index index : td.indexes()) {
                    drop_stmt = "DROP INDEX " + index.index_name();
                    db.execSQL(drop_stmt);
                }
            }
        }
    }

    private void dropTables(SQLiteDatabase db) {
        Class[] db_tables = Schema.class.getDeclaredClasses();

        String drop_stmt = "";
        for (Class table : db_tables) {
            drop_stmt = "DROP TABLE " + table.getSimpleName().toLowerCase(Locale.ENGLISH);
            db.execSQL(drop_stmt);
        }
    }


}
