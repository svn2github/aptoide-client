package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.services.RabbitMqService;
import cm.aptoide.ptdev.services.RabbitMqService.RabbitMqBinder;
import cm.aptoide.ptdev.webservices.Webservice;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

public class MainActivity extends SherlockFragmentActivity {


    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    private ArrayList<Server> server;
    private RabbitMqService rabbitMqService;
    private ServiceConnection wConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RabbitMqBinder wBinder = (RabbitMqBinder) service;
            rabbitMqService = wBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //testWebservicesCalls();
        Intent i = new Intent(this, RabbitMqService.class);
        bindService(i, wConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(wConnection);
    }

    private void testWebservicesCalls() {
        Webservice webservice = new Webservice("http://webservices.aptoide.com/");

        FutureCallback callback = new FutureCallback() {
            @Override
            public void onCompleted(Exception e, Object o) {
                if (e != null) {
                    e.printStackTrace();
                } else {

                    Log.d("main", "Response: " + o.toString());
                    ((TextView) findViewById(R.id.textView)).setText(o.toString());
                }

            }
        };

        //webservice.getApkInfo().setRepoName("teresa-deus").setPackageName("com.gameloft.android.ANMP.GloftMKHM").setVersionName("1.1.5").setCallback(callback).execute();
        //webservice.getCheckUserCredentials().setUsername("jpa1costa@gmail.com").setPassword().setCallback(callback).execute();
        //webservice.getRepositoryInfo().setRepo("savou").setCallback(callback).execute();
        //webservice.listApkComments().setRepo("savou").setApkid("cm.aptoide.pt").setApkversion("4.1.3").setCallback(callback).execute();
        /*List<Server> repos = new ArrayList<Server>();
        Server server = new Server(){}.setName("apps").setHash("firstHash");
        repos.add(server);
        repos.add(server);
        webservice.listRepositoryChange().setRepos(repos).setCallback(callback).execute();
        */
        //webservice.listRepositoryComments().setRepo("savou").setOffset(2).setLimit(2).setCallback(callback).execute();
        //webservice.listRepositoryLikes().setRepo("savou").setLimit(2).setOffset(1).setCallback(callback).execute();
        //webservice.listRepositoryLocalApkNames().setRepo("savou").setLang("pt_PT").setCallback(callback).execute();

    }

    void showDialog() {
        DialogFragment newFragment = new AddStoreDialog();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }

    public void doNegativeClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Negative click!");
    }


}
