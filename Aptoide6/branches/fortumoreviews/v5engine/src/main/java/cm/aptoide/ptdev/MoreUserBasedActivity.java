package cm.aptoide.ptdev;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.ListUserbasedApkRequest;
import cm.aptoide.ptdev.webservices.json.ListRecomended;

/**
 * Created by rmateus on 06-02-2014.
 */
public class MoreUserBasedActivity extends ActionBarActivity implements DownloadInterface {

    private DownloadService downloadService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d("Aptoide-TopAppsActivity", "ServiceBound");
            downloadService = ((DownloadService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        bindService(new Intent(this, DownloadService.class), conn, Context.BIND_AUTO_CREATE);

        Fragment fragment = new MoreUserBasedFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.recommended_for_you));

    }

    @Override
    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
    }

    public static class MoreUserBasedFragment extends ListFragment {

        ArrayList<HomeItem> items = new ArrayList<>();

        HomeBucketAdapter adapter;
        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            spiceManager.start(getActivity());
        }

        @Override
        public void onDetach() {
            super.onDetach();
            spiceManager.shouldStop();

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new HomeBucketAdapter(getActivity(), items);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, AptoideUtils.getPixels(getActivity(), 10), 0, 0);
            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
            getListView().setItemsCanFocus(true);

            final ListUserbasedApkRequest request = new ListUserbasedApkRequest(getActivity());


            spiceManager.execute(request,  new RequestListener<ListRecomended>() {
                @Override
                public void onRequestFailure(SpiceException e) {

                }

                @Override
                public void onRequestSuccess(ListRecomended listRecomended) {

                    items.clear();
                    final boolean matureCheck = PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean("matureChkBox", true);
                    for(ListRecomended.Repository repository : listRecomended.getRepository()){

                        String repoName = repository.getName();
                        String iconPath = repository.getIconspath();
                        for(ListRecomended.Repository.Package aPackage : repository.getPackage()){

                            String icon;

                            if(aPackage.getIcon_hd()!=null){
                                icon = aPackage.getIcon_hd();
                            }else{
                                icon = aPackage.getIcon();
                            }
                            HomeItem item = new HomeItem(aPackage.getName(), aPackage.getCatg2(), iconPath + icon, 0, String.valueOf(aPackage.getDwn()), aPackage.getRat().floatValue(), aPackage.getCatg2());
                            item.setRecommended(true);
                            item.setRepoName(repoName);
                            item.setMd5(aPackage.getMd5h());

                            if (matureCheck) {
                                if (!aPackage.getAge().equals("Mature")) {
                                    items.add(item);
                                }
                            } else {
                                items.add(item);
                            }

                        }

                    }

                    setListAdapter(adapter);

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
