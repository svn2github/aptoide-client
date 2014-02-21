package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.services.ParserService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.octo.android.robospice.SpiceManager;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.ChannelN;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by j-pac on 27-01-2014.
 */
public class UpdatesSyncAdapter extends AbstractThreadedSyncAdapter {




    public UpdatesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (sharedPreferences.getBoolean("showUpdatesNotification", true)) {
            Log.d("Aptoide-UpdateSync", "onPerformSync()");
            ParserHelper helper = new ParserHelper();
            helper.parse(getContext());
        }



    }



}
