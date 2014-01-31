package cm.aptoide.ptdev;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.configuration.Constants;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.ChannelN;

import java.io.IOException;

/**
 * Created by j-pac on 27-01-2014.
 */
public class WebInstallSyncAdapter extends AbstractThreadedSyncAdapter {

    public WebInstallSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("Aptoide-WebInstall", "onPerformSync()");

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constants.WEBINSTALL_HOST);
        factory.setConnectionTimeout(20000);
        factory.setVirtualHost("webinstall");
        factory.setUsername("public");
        factory.setPassword("public");

        try {
            AMQConnection connection = (AMQConnection) factory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(0);

            GetResponse response;
            while((response = channel.basicGet(sPref.getString(Constants.WEBINSTALL_QUEUE_NAME, null), false)) != null) {
                String message = new String(response.getBody(), "UTF-8");
                handleMessage(message);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }

            connection.close();

            //sPref.edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, false);

        } catch (IOException e) {
            e.printStackTrace();
            //sPref.edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, true).commit();
        }

    }

    void handleMessage(String body) {
        Log.d("Aptoide-WebInstall", body);
    }



}
