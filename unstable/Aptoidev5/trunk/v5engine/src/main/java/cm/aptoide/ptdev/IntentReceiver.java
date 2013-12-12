package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import cm.aptoide.ptdev.services.RabbitMqService;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 10-10-2013
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class IntentReceiver extends ActionBarActivity {

    private RabbitMqService rabbitMqService;

    private ServiceConnection wConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RabbitMqService.RabbitMqBinder wBinder = (RabbitMqService.RabbitMqBinder) service;
            rabbitMqService = wBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null){
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("newrepo", "");
            startActivity(i);
        }

        Intent a2 = new Intent(this, RabbitMqService.class);
        bindService(a2, wConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isFinishing()){
            Log.d("RabbitMqService", "onDestroy");
            unbindService(wConnection);
        }

    }
}
