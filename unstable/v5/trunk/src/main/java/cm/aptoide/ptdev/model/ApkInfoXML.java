package cm.aptoide.ptdev.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.ptdev.database.StatementHelper;
import cm.aptoide.ptdev.database.schema.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 14-10-2013
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */
public class ApkInfoXML extends Apk {

    @Override
    public List<String> getStatements() {

        ArrayList<String> statements = new ArrayList<String>();
        ContentValues values = new ContentValues();

        values.put(Schema.Apk.COLUMN_APKID, "");
        values.put(Schema.Apk.COLUMN_ICON, "");
        values.put(Schema.Apk.COLUMN_ID, "");
        values.put(Schema.Apk.COLUMN_NAME, "");
        values.put(Schema.Apk.COLUMN_REPO_ID, "");
        statements.add(StatementHelper.getStatment(Schema.Apk.getName(), values));
        values.clear();

        values.put(Schema.Repo.COLUMN_HASH, "");
        values.put(Schema.Repo.COLUMN_ICONS_PATH, "");
        values.put(Schema.Repo.COLUMN_LATEST_TIMESTAMP, "");
        values.put(Schema.Repo.COLUMN_ID, "");
        values.put(Schema.Repo.COLUMN_NAME, "");
        statements.add(StatementHelper.getStatment(Schema.Repo.getName(), values));


        return statements;
    }

    @Override
    public void databaseInsert(List<SQLiteStatement> sqLiteStatements) {


        Log.d("TAG1", sqLiteStatements.get(0).toString());

    }
}
