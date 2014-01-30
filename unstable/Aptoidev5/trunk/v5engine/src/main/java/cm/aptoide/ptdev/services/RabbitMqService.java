package cm.aptoide.ptdev.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.ChannelN;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 24-10-2013
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class RabbitMqService extends Service {

    private final IBinder wBinder = new RabbitMqBinder();

    private ExecutorService thread_pool;
    private AMQConnection connection;


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String host = intent.getStringExtra("host");
                String queueName = intent.getStringExtra("queueName");
                try {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(host);
                    factory.setUsername("public");
                    factory.setPassword("public");
                    factory.setConnectionTimeout(20000);

                    factory.setVirtualHost("webinstall");
                    connection = (AMQConnection) factory.newConnection();
                    newChannel(queueName, new AMQHandler() {
                        @Override
                        void handleMessage(String body) {
                            Log.d("Aptoide-WebInstall", body);
                        }
                    });
                } catch (IOException e) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, true).commit();
                    e.printStackTrace();
                }
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        onStartCommand(intent, 0, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Aptoide-RabbitMqService", "RabbitMqService created!");
        thread_pool = Executors.newCachedThreadPool();

        Account account = AccountManager.get(getApplicationContext()).getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
        if(Build.VERSION.SDK_INT >= 8) {
            ContentResolver.removePeriodicSync(account, Constants.WEBINSTALL_SYNC_AUTHORITY, new Bundle());
        }
        ContentResolver.setSyncAutomatically(account, Constants.WEBINSTALL_SYNC_AUTHORITY, false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Aptoide-RabbitMqService", "RabbitMqService Destroyed!");
        try {
            if(channel!=null) channel.abort();
            connection.disconnectChannel(channel);
            connection.abort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread_pool.shutdownNow();

        Account account = AccountManager.get(getApplicationContext()).getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
        ContentResolver.setIsSyncable(account, Constants.WEBINSTALL_SYNC_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, Constants.WEBINSTALL_SYNC_AUTHORITY, true);

        if(Build.VERSION.SDK_INT >= 8) {
            ContentResolver.addPeriodicSync(account, Constants.WEBINSTALL_SYNC_AUTHORITY, new Bundle(), Constants.WEBINSTALL_SYNC_POLL_FREQUENCY);
        }

    }

    public class RabbitMqBinder extends Binder {
        public RabbitMqService getService() {
            return RabbitMqService.this;
        }

    }


    private ChannelN channel;
    private QueueingConsumer consumer;

    public void newChannel(String queue_id, AMQHandler task) throws IOException {
        channel = (ChannelN) connection.createChannel();
        //channel.queueDeclare(queue_id, true, false, false, null);
        channel.basicQos(0);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue_id, false, consumer);

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.WEBINSTALL_QUEUE_EXCLUDED, false);

        task.setConsumer(consumer);
        thread_pool.submit(task);
    }


    public abstract class AMQHandler implements Runnable {

        private boolean isRunning = true;
        private QueueingConsumer consumer;

        public AMQHandler() {
        }

        @Override
        public void run() {

            while(isRunning){
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String body = new String(delivery.getBody(), Charset.forName("UTF-8"));
                    handleMessage(body);
                    Log.d("Aptoide-RabbitMqService", "MESSAGE: " + body);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        abstract void handleMessage(String body);

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public void setConsumer(QueueingConsumer consumer) {
            this.consumer = consumer;
        }
    }

}


