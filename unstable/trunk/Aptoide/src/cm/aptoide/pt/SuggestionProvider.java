package cm.aptoide.pt;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 26-09-2013
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionProvider extends ContentProvider {

    private static final String URI =  "";

    @Override
    public boolean onCreate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getType(Uri uri) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cursor query(final Uri uri, String[] projection, String selection, final String[] selectionArgs, String sortOrder) {


//        String query = uri.getLastPathSegment().toLowerCase();
//
//        Toast.makeText(getContext().getApplicationContext(), "QUERY: " + query, Toast.LENGTH_SHORT).show();
//
//

        if (Build.VERSION.SDK_INT > 7) {
            BlockingQueue<MatrixCursor> arrayBlockingQueue = new ArrayBlockingQueue<MatrixCursor>(1);
            WebSocketSingleton.getInstance().setNotificationUri(uri).setContext(getContext()).setBlockingQueue(arrayBlockingQueue);

            MatrixCursor matrix_cursor = null;
            WebSocketSingleton.send(selectionArgs[0]);
            try {
                matrix_cursor = arrayBlockingQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return matrix_cursor;
        }else{
            return null;
        }
          //To change body of implemented methods use File | Settings | File Templates.
    }

    private void addRow(MatrixCursor matrix_cursor, String string) {
        int id = matrix_cursor.getCount();
        matrix_cursor.newRow().add(id).add(string).add(string);
    }


}