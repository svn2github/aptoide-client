package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.services.MainService;
import cm.aptoide.ptdev.tutorial.Tutorial;
import cm.aptoide.ptdev.webservices.Webservice;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

public class MainActivity extends SherlockFragmentActivity {


    private static final String TAG = MainActivity.class.getName();
    static Toast toast;
    private ArrayList<Server> server;
    private MainService service;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((MainService.MainServiceBinder)binder).getService();
            Log.d("Aptoide-MainActivity", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Intent i = new Intent(this, MainService.class);
        if(savedInstanceState==null){
            startService(i);
        }

        bindService(i, conn, BIND_AUTO_CREATE);

        SQLiteDatabase db = ((Aptoide)getApplication()).getDb();
        Database database = new Database(db);

        testTutorial();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void testTutorial() {
        /*
        final ArrayList<TutorialFragment> fragments = new ArrayList<TutorialFragment>();

        fragments.add(TutorialFragment.newInstance(0, R.layout.tutorial_fragment_layout));
        fragments.add(TutorialFragment.newInstance(1, R.layout.tutorial_fragment_layout));
        fragments.add(TutorialFragment.newInstance(2, R.layout.tutorial_fragment_layout));
        fragments.add(TutorialFragment.newInstance(3, R.layout.tutorial_fragment_layout));
        fragments.add(TutorialFragment.newInstance(4, R.layout.tutorial_fragment_layout));
        fragments.add(TutorialFragment.newInstance(5, R.layout.tutorial_fragment_layout));
*/


        ((Button)findViewById(R.id.show_tutorial)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Tutorial.class);
               // intent.putParcelableArrayListExtra(Tutorial.FRAGMENTS_INTENT_KEY, fragments);
                startActivity(intent);
            }
        });
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
                    //((TextView) findViewById(R.id.textView)).setText(o.toString());
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
