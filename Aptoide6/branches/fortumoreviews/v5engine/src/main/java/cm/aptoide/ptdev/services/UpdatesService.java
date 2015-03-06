package cm.aptoide.ptdev.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.UnInstalledApkEvent;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.utils.AptoideUtils;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

/**
 * Created by rmateus on 15-12-2014.
 */
public class UpdatesService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static ScheduledExecutorService executor;

    GetUpdates task = new GetUpdates();

    static int retries = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        synchronized (this) {

            if(AptoideUtils.isNetworkAvailable(getApplicationContext())) {

                    if (executor == null) {
                        executor = Executors.newSingleThreadScheduledExecutor();
                        executor.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
                    }

                    if (intent != null && intent.hasExtra("force")) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    executor.shutdown();
                                    Log.d("AptoideUpdates", "Awaiting previous executor to terminate");
                                    executor.awaitTermination(2, TimeUnit.MINUTES);
                                    Log.d("AptoideUpdates", "Previous terminatated");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                executor = Executors.newSingleThreadScheduledExecutor();
                                executor.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
                            }
                        }).start();

                    }



            }else{

                if(executor!=null){
                    executor.shutdown();
                }

                executor = null;
                stopSelf();
            }

        }


        return START_STICKY_COMPATIBILITY;
    }


    public class GetUpdates implements Runnable{

        @Override
        public void run() {



            try {
                    Database database = new Database(Aptoide.getDb());

                    if (!database.hasInstalled()) {
                        Log.d("AptoideUpdates", "First run install");
                        PackageManager packageManager = Aptoide.getContext().getPackageManager();
                        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);

                        for (PackageInfo anInstalledPackage : installedPackages) {
                            FragmentUpdates2.UpdatesApi.Package aPackage = new FragmentUpdates2.UpdatesApi.Package();
                            aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(anInstalledPackage.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                            aPackage.vercode = anInstalledPackage.versionCode;
                            aPackage.packageName = anInstalledPackage.packageName;
                            database.insertInstalled(aPackage);
                        }

                    }

                    List<FragmentUpdates2.UpdatesApi.Package> updates1 = database.getUpdates(50);

                    if (updates1.isEmpty()) {
                        executor.shutdown();
                        Log.d("AptoideUpdates", "Stopping service and executor is " + executor.isShutdown());

                        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("showUpdatesNotification", true)) {
                            showUpdatesNotification();
                            BusProvider.getInstance().post(new UnInstalledApkEvent(""));
                        }

                        stopSelf();
                        return;
                    }

                    FragmentUpdates2.UpdatesApi api = new FragmentUpdates2.UpdatesApi();
                    api.mature = !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);


                    Cursor servers = database.getServers();

                    for (servers.moveToFirst(); !servers.isAfterLast(); servers.moveToNext()) {
                        api.store_names.add(servers.getString(servers.getColumnIndex("name")));
                    }

                    servers.close();
                    List<FragmentUpdates2.UpdatesResponse.UpdateApk> responseList = new ArrayList<>();

                    if (!api.store_names.isEmpty()) {

                        api.apks_data.addAll(updates1);

                        ObjectMapper mapper = new ObjectMapper();

                        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        RestAdapter adapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new JacksonConverter(mapper)).setEndpoint("http://").build();

                        Log.d("AptoideUpdates", "Getting updates");
                        FragmentUpdates2.UpdatesResponse updates = adapter.create(FragmentUpdates2.Webservice.class).getUpdates(api);
                        Log.d("AptoideUpdates", "Getted updates");
                        List<FragmentUpdates2.UpdatesResponse.UpdateApk> list = updates.data.list;
                        responseList.addAll(list);
                    }

                    for (FragmentUpdates2.UpdatesApi.Package aPackage : updates1) {

                        database.resetPackage(aPackage.packageName);

                        for (FragmentUpdates2.UpdatesResponse.UpdateApk aPackage2 : responseList) {
                            if (aPackage2.packageName.equals(aPackage.packageName)) {
                                database.updatePackage(aPackage2);
                            }
                        }

                    }

                    retries = 0;

                } catch (Exception e) {
                    e.printStackTrace();

                    Log.d("AptoideUpdates", "Exception retries: " + retries);

                    if (retries == 7) {
                        executor.shutdown();
                        stopSelf();
                        Log.d("AptoideUpdates", "Service exceeded retries. Shutting down. ");

                        retries = 0;
                    }
                    retries++;

                }

                BusProvider.getInstance().post(new UnInstalledApkEvent(""));
                Log.d("AptoideUpdates", "Stopped");




        }

    }

    private void showUpdatesNotification() {
        int updates = 0;
        Cursor data=null;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {

            data = new Database(Aptoide.getDb()).getUpdates();

            updates = data.getCount();


        }finally {
            if(data!=null)
                data.close();
        }

        if(updates > 0 && updates != defaultSharedPreferences.getInt("updates", 0)){

            NotificationManager managerNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



            int icon = android.R.drawable.stat_sys_download_done;


            if(Aptoide.getConfiguration().getMarketName().equals("Aptoide")){
                icon = R.drawable.ic_stat_aptoide_notification;
            }


            CharSequence tickerText = getString(R.string.has_updates, Aptoide.getConfiguration().getMarketName());
            long when = System.currentTimeMillis();

            Context context = getApplicationContext();
            CharSequence contentTitle = Aptoide.getConfiguration().getMarketName();
            CharSequence contentText = getString(R.string.new_updates, updates);

            if(updates==1){
                contentText = getString(R.string.one_new_update, updates);
            }

            Intent notificationIntent = new Intent();

            notificationIntent.setClassName(getPackageName(), Aptoide.getConfiguration().getStartActivityClass().getName());
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationIntent.setAction("");


            notificationIntent.putExtra("new_updates", true);


            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(contentIntent)
                    .setTicker(tickerText)
                    .build();
            managerNotification.notify(546, notification);
            defaultSharedPreferences.edit().putInt("updates", data.getCount()).apply();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor = null;
        Log.d("AptoideUpdates", "OnDestroy");
    }
}
