package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.Executor;
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


    @Override
    public IBinder onBind(Intent intent) {
        return wBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("RabbitMqService", "RabbitMqService created!");

        thread_pool = Executors.newCachedThreadPool();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("RabbitMqService", "RabbitMqService Destroyed!");

    }

    public class RabbitMqBinder extends Binder {
        public RabbitMqService getService() {
            return RabbitMqService.this;
        }

    }

    /*
    public void newConnection(String queue_id, Runnable task) {
        com.rabbitmq.client.Connection connection;
        com.rabbitmq.client.Channel channel;
        com.rabbitmq.client.Consumer consumer;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setConnectionTimeout(0);

        connection = factory.newConnection();

        channel = connection.createChannel();

        channel.queueDeclare(queue_id, true, false, false,
                null);

        channel.basicQos(0);

        consumer = new QueueingConsumer(channel);

        channel.basicConsume(queue_id, false, consumer);

        thread_pool

    }
*/

    public class WebInstall implements Runnable {

        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}


