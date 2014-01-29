package cm.aptoide.ptdev;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
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
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(extras.getString("host"));
        factory.setConnectionTimeout(20000);


        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(0);

            GetResponse response = channel.basicGet(extras.getString("queueId"), false);

            if (response != null) {
                String message = new String(response.getBody(), "UTF-8");
                //handleMessage(message);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }

            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void handleMessage(String body) {
        Log.d("Aptoide-WebInstall", body);
    }



}
