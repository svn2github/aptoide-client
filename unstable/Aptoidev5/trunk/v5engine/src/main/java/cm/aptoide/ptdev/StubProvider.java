package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import com.google.api.client.util.Data;

import java.io.IOException;

/**
 * Created by j-pac on 29-01-2014.
 */
public class StubProvider extends ContentProvider {

    //private static final String URL = "content://" + Constants.STUB_PROVIDER_AUTHORITY;
    //private static final Uri CONTENT_URI = Uri.parse(URL);

    private static final int TOKEN = 1;
    private static final int REPO = 2;

    private static final String BACKUP_PACKAGE =  "pt.aptoide.backupapps";
    private static final String UPLOADER_PACKAGE = "pt.caixamagica.aptoide.uploader";
    private static final String SIGNATURE = "3082030d308201f5a0030201020204707f3269300d06092a864886f70d01010b05003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f6964204465627567301e170d3134303131333133313932345a170d3135303131333133313932345a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730820122300d06092a864886f70d01010105000382010f003082010a0282010100a78ea962ea1e79728af241064212afc3b8114d77d271d37ba1d0cd6dbccba5d649379e3bc47bc22bd4703f5d919937e5319ad9698f915e9f051ce0cfabdaa51689e8c37dcb10aaa0f3434d91505c15a20fc483a44a006725ab6fecd9d8336383e37c13a86cc04f86be57a71375b373a2826d2690bddfa217116fe753f0cc39826b71e305780cf7c116a3b01caabf6daf06a311042ccd219b617f4ab9af37b499009234d7d0b2afba538002eff8f545b72e5e897fe3ebfd216885d5e0c446a3f5c9036e7ee091eaa70adabd9356b4288b7a74a98efc853397d68d12709a06477f64b1eb425e156492d0a72aebb431fdc6ee71dc4a316b91782223ce079cbe0d210203010001a321301f301d0603551d0e04160414e3d4ab84257fd096857ef6d116271a5af1ed4a4d300d06092a864886f70d01010b05000382010100a77160d2f1bcf0ddf5e304df44f77376d7f308a3c92df342188649608665737ec0f4045a29a9db17579d0fa26bc47b54aa90d92992a4b438b6fa054f6243faea70476c3f9ae3a2dfe4def49172c852c52cb0bf131e54b9f61a2d07f59f00a7bb3fd2d9d56bb5111798b0d13ff6a08fd7f6e28dc2f67eeddee190278517c0a545ee180dffef836bc55dc9eca0acc24f8b361a4a0b2f2d526251d258fe3bd27494d8da9e9dc7bc1e5f86868d63c2db7904c1fc295151ea001ee5ac7904463f6d8de4f18fd4c63b5aa0ad382057633d72701646d121c452f15bf78089254a89f2520de21cdebf14018679985fd988a83e7e0c4e34097ac9006df7f4162ff537be1c";

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(Constants.STUB_PROVIDER_AUTHORITY, "token", TOKEN);
        uriMatcher.addURI(Constants.STUB_PROVIDER_AUTHORITY, "repo", REPO);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        AccountManager accountManager = AccountManager.get(getContext());
        Account[] accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if(accounts.length != 0) {
            try {
                int uid = Binder.getCallingUid();

                PackageManager pm = getContext().getPackageManager();
                String callerPackage = pm.getPackagesForUid(uid)[0];
                String callerSignature = pm.getPackageInfo(callerPackage, PackageManager.GET_SIGNATURES).signatures[0].toCharsString();

                Log.d("StubProvider", "callerPackage: " + callerPackage);
                Log.d("StubProvider", "callerSignature: " + callerSignature);

                if((callerPackage.equals(BACKUP_PACKAGE) || callerPackage.equals(UPLOADER_PACKAGE)) && callerSignature.equals(SIGNATURE)) {

                    MatrixCursor mx;

                    switch (uriMatcher.match(uri)) {
                        case TOKEN:
                            String token = accountManager.blockingGetAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);
                            mx = new MatrixCursor(new String[]{"userToken"}, 1);
                            mx.addRow(new Object[]{token});

                            Log.d("StubProvider", "token retrieved: " + token);
                            return mx;

                        case REPO:
                            String repo = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("userRepo", "");
                            mx = new MatrixCursor(new String[]{"userRepo"}, 1);
                            mx.addRow(new Object[]{repo});

                            Log.d("StubProvider", "repo retrieved: " + repo);
                            return mx;
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d("StubProvider", "returning null");
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
