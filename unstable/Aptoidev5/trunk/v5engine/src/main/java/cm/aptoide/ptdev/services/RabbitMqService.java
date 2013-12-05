package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
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
    public IBinder onBind(Intent intent) {

        String host = intent.getStringExtra("host");

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setConnectionTimeout(20000);
            connection = (AMQConnection) factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Aptoide-RabbitMqService", "RabbitMqService created!");
        thread_pool = Executors.newCachedThreadPool();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Aptoide-RabbitMqService", "RabbitMqService Destroyed!");

        thread_pool.shutdownNow();
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
        channel.queueDeclare(queue_id, true, false, false, null);
        channel.basicQos(0);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue_id, false, consumer);
        thread_pool.submit(task);
    }


    public abstract class AMQHandler implements Runnable {

        private boolean isRunning = true;
        private final QueueingConsumer consumer;

        public AMQHandler(QueueingConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {

            while(isRunning){
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String body = new String(delivery.getBody(), Charset.forName("UTF-8"));
                    handleMessage(body);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        abstract void handleMessage(String body);

        public void setRunning(boolean running) {
            isRunning = running;
        }

    }

}


