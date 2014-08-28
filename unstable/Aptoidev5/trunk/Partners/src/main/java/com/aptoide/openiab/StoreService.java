package com.aptoide.openiab;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;

import org.onepf.oms.IOpenAppstore;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.services.HttpClientSpiceService;


/**
 * Created by j-pac on 12-02-2014.
 */
public class StoreService extends Service {

    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private final IOpenAppstore.Stub wBinder = new StoreBinder(Aptoide.getContext(), manager);

    @Override
    public IBinder onBind(Intent intent) {
        return wBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Aptoide", "StoreService onCreate");
        if(!manager.isStarted())manager.start(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager.isStarted())manager.shouldStop();
        Log.d("Aptoide", "manager onStop");
    }
}
