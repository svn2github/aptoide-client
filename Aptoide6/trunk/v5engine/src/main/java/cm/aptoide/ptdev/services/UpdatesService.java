package cm.aptoide.ptdev.services;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.fragments.FragmentUpdates2;
import cm.aptoide.ptdev.utils.AptoideUtils;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

/**
 * Created by rmateus on 15-12-2014.
 */
public class UpdatesService extends Service {



    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        timer = new Timer();
        timer.schedule(new GetUpdates(), 0, 60 * 1000 * 5);


        return START_STICKY_COMPATIBILITY;
    }


    public class GetUpdates extends TimerTask{

        @Override
        public void run() {

            try {

                Database database = new Database(Aptoide.getDb());

                FragmentUpdates2.UpdatesApi api = new FragmentUpdates2.UpdatesApi();
                api.mature = !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);

                //aPackage.packageName = "cm.aptoide.pt";
                //aPackage.vercode = 447;
                //aPackage.signature = "D5:90:A7:D7:92:FD:03:31:54:2D:99:FA:F9:99:76:41:79:07:73:A9";

                Cursor servers = database.getServers();

                for (servers.moveToFirst(); !servers.isAfterLast(); servers.moveToNext()) {
                    api.store_names.add(servers.getString(servers.getColumnIndex("name")));
                }

                servers.close();


                PackageManager packageManager = Aptoide.getContext().getPackageManager();
                List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);

                for (PackageInfo anInstalledPackage : installedPackages) {
                    FragmentUpdates2.UpdatesApi.Package aPackage = new FragmentUpdates2.UpdatesApi.Package();
                    aPackage.signature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(anInstalledPackage.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                    aPackage.vercode = anInstalledPackage.versionCode;
                    aPackage.packageName = anInstalledPackage.packageName;
                    database.insertInstalled(aPackage);
                }

                List<FragmentUpdates2.UpdatesApi.Package> updates1 = database.getUpdates(20);
                api.apks_data.addAll(updates1);


                ObjectMapper mapper = new ObjectMapper();

                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                RestAdapter adapter = new RestAdapter.Builder().setConverter(new JacksonConverter()).setEndpoint("http://").build();

                FragmentUpdates2.UpdatesResponse updates = adapter.create(FragmentUpdates2.Webservice.class).getUpdates(api);


                for (FragmentUpdates2.UpdatesApi.Package aPackage : updates1) {

                    database.updateApkUpdateTimestamp(aPackage.packageName);

                    for(FragmentUpdates2.UpdatesResponse.UpdateApk aPackage2 : updates.data.list){
                        if(aPackage2.packageName.equals(aPackage.packageName)){
                            database.updatePackage(aPackage2);
                        }
                    }

                }

            }catch (Exception e){
                e.printStackTrace();
            }



        }

    }




}
