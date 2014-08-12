package com.aptoide.openiab;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.octo.android.robospice.SpiceManager;

import org.onepf.oms.IOpenInAppBillingService;

import cm.aptoide.ptdev.services.HttpClientSpiceService;


/**
 * Created by j-pac on 12-02-2014.
 */
public class BillingService extends Service {
    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private final IOpenInAppBillingService.Stub wBinder = new BillingBinder(this, manager);

    @Override
    public IBinder onBind(Intent intent) {
        return wBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!manager.isStarted())manager.start(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager.isStarted())manager.shouldStop();
    }
}
