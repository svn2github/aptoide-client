package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.ptdev.dialogs.AddStoreDialog;
import cm.aptoide.ptdev.model.Server;
import cm.aptoide.ptdev.webservices.GetApkInfo;
import cm.aptoide.ptdev.webservices.Webservice;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

public class MainActivity extends SherlockFragmentActivity {


    private static final String TAG = MainActivity.class.getName();
    private ArrayList<Server> server;

    static Toast toast;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
