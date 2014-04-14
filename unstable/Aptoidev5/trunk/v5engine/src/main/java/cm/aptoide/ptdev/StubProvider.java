package cm.aptoide.ptdev;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Binder;

/**
 * Created by j-pac on 29-01-2014.
 */
public class StubProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        int uid = Binder.getCallingUid();

        PackageManager pm = getContext().getPackageManager();
        String callerPackage = pm.getNameForUid(uid);

        if("PKG_NAME".equals(callerPackage)) {
            try {
                String signature = pm.getPackageInfo(callerPackage, 0).signatures[0].toCharsString();

                if(signature.equals("PKG_SIGNATURE")) {
                    MatrixCursor mx = new MatrixCursor(new String[]{"userRepo"});
                    mx.addRow();
                    return mx;
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
