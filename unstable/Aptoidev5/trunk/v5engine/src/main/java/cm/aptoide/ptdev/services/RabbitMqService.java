package cm.aptoide.ptdev.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.Constants;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.ChannelN;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

                final Account account = AccountManager.get(getApplicationContext()).getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String queueName = sharedPreferences.getString("queueName", null);

                String host = Constants.WEBINSTALL_HOST;

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

                            try {
                                JSONObject object = new JSONObject(body);

                                Intent i = new Intent(getApplicationContext(), AppViewActivity.class);
                                String authToken = AccountManager.get(getApplicationContext()).getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null).getResult().getString(AccountManager.KEY_AUTHTOKEN);

                                String repo = object.getString("repo");
                                long id = object.getLong("id");
                                String md5sum = object.getString("md5sum");
                                i.putExtra("fromMyapp", true);
                                i.putExtra("repoName", repo);
                                i.putExtra("id", id);
                                i.putExtra("md5sum", md5sum);

                                String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                String hmac = object.getString("hmac");
                                String calculatedHmac = AptoideUtils.Algorithms.computeHmacSha1(repo+id+md5sum, authToken+deviceId);
                                if(hmac.equals(calculatedHmac)){
                                    getApplicationContext().startActivity(i);
                                }else{
                                    Log.d("Aptoide-WebInstall", "Error validating message: received: " + hmac + " calculated:" + calculatedHmac);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (AuthenticatorException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (OperationCanceledException e) {
                                e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            }


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
            isRunning = false;
            if(connection != null && connection.isOpen()){
                if(channel!=null) channel.close();
                connection.disconnectChannel(channel);
                connection.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ShutdownSignalException e){
            e.printStackTrace();
        }

        if (AccountManager.get(getApplicationContext()).getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {
            Account account = AccountManager.get(getApplicationContext()).getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
            ContentResolver.setIsSyncable(account, Constants.WEBINSTALL_SYNC_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, Constants.WEBINSTALL_SYNC_AUTHORITY, true);
            if (Build.VERSION.SDK_INT >= 8) {
                ContentResolver.addPeriodicSync(account, Constants.WEBINSTALL_SYNC_AUTHORITY, new Bundle(), Constants.WEBINSTALL_SYNC_POLL_FREQUENCY);
            }
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
        task.setConsumer(consumer);
        thread_pool.execute(task);
    }

    private boolean isRunning = true;

    public abstract class AMQHandler implements Runnable {

        private QueueingConsumer consumer;

        public AMQHandler() {
        }

        @Override
        public void run() {

            while(isRunning){
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String body = new String(delivery.getBody(), "UTF-8");
                    Log.d("Aptoide-RabbitMqService", "MESSAGE: " + body);
                    handleMessage(body);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    isRunning = false;
                    e.printStackTrace();
                } catch (ShutdownSignalException e){
                    isRunning = false;
                    Log.d("Aptoide-WebInstall", "Connection closed with reason " + e.getReason().toString());
                } catch (ConsumerCancelledException e){
                    isRunning = false;
                    Log.d("Aptoide-WebInstall", "Connection was canceled");
                }

            }
            try{
                if(channel != null && channel.isOpen()){
                    channel.close();
                    connection.disconnectChannel(channel);
                    connection.close();
                }

            }catch (IOException e) {
                e.printStackTrace();
            }


        }

        abstract void handleMessage(String body);



        public void setConsumer(QueueingConsumer consumer) {
            this.consumer = consumer;
        }
    }

}


