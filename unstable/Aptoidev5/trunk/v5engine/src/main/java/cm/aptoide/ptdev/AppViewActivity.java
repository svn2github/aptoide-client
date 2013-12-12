package cm.aptoide.ptdev;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentAppView;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class AppViewActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);

    private GetApkInfoJson json;
    private RequestListener<GetApkInfoJson> requestListener = new RequestListener<GetApkInfoJson>() {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(AppViewActivity.this , "Error request", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(GetApkInfoJson getApkInfoJson) {
            AppViewActivity.this.json = getApkInfoJson;
            publishEvents();




        }
    };
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersionName;
    private long id;
    private int downloads;
    private String repoName;
    private DownloadService service;
    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder downloadService) {
            service = ((DownloadService.LocalBinder)downloadService).getService();
            if(service.getDownload(id).getDownload()!=null){
                onDownloadUpdate(service.getDownload(id).getDownload());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Produce
    public DetailsEvent publishDetails(){

        Details details = new Details();
        Log.d("Aptoide-AppView", "PublishingDetails");
        if (json != null) {

            if (!json.getStatus().equals("FAIL")) {

                Log.d("Aptoide-AppView", "Description: " + json.getMeta().getDescription());
                details.setDescription(json.getMeta().getDescription());
                details.setSize(json.getApk().getSize().longValue());
                details.setStore(repoName);
                details.setDownloads(downloads);
            } else {

                for(String error : json.getErrors()){
                    Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        }

        return new DetailsEvent(details);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(downloadConnection);
    }

    @Produce
    public RelatedAppsEvent publishRelatedApps(){

        Related relatedApps = new Related();

        return new RelatedAppsEvent(relatedApps);

    }

    @Produce
    public SpecsEvent publishSpecs(){

        Specs specs = new Specs();

        return new SpecsEvent(specs);

    }

    @Produce
    public RatingEvent publishRating(){

        RatingEvent event = new RatingEvent();
        if(json!=null){
            event.setComments(new ArrayList<Comment>(json.getComments()));
        }
        return event;

    }


    private void publishEvents() {
        BusProvider.getInstance().post(publishDetails());
        BusProvider.getInstance().post(publishRelatedApps());
        BusProvider.getInstance().post(publishSpecs());
        BusProvider.getInstance().post(publishRating());
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_view);
        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name);
        appVersionName = (TextView) findViewById(R.id.app_version);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        id = getIntent().getExtras().getLong("id");
        if(pager == null){

        }else{
            PagerAdapter adapter = new AppViewPager(getSupportFragmentManager());
            pager.setAdapter(adapter);
            PagerSlidingTabStrip slidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            slidingTabStrip.setViewPager(pager);
        }



        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getSupportLoaderManager().initLoader(50, getIntent().getExtras(), this);




    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.abs__home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, final Bundle bundle) {
        return new SimpleCursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                long id = bundle.getLong("id");
                Log.d("Aptoide-AppView", "getapk id: " + id);

                return new Database(Aptoide.getDb()).getApkInfo(id);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor apkCursor) {

        repoName = apkCursor.getString(apkCursor.getColumnIndex("reponame"));
        final String name = apkCursor.getString(apkCursor.getColumnIndex("name"));
        String package_name = apkCursor.getString(apkCursor.getColumnIndex("package_name"));
        final String versionName = apkCursor.getString(apkCursor.getColumnIndex("version_name"));
        String icon = apkCursor.getString(apkCursor.getColumnIndex("icon"));
        final String iconpath = apkCursor.getString(apkCursor.getColumnIndex("iconpath"));
        downloads = apkCursor.getInt(apkCursor.getColumnIndex("downloads"));


        appName.setText(name);
        appVersionName.setText(versionName);
        String sizeString = IconSizes.generateSizeString(this);
        if(icon.contains("_icon")){
            String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
            icon = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(iconpath + icon, appIcon);
        GetApkInfoRequest request = new GetApkInfoRequest();

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, package_name + repoName, DurationInMillis.ONE_HOUR, requestListener);
        bindService(new Intent(this, DownloadService.class), downloadConnection, BIND_AUTO_CREATE);
        final String finalIcon = icon;
        findViewById(R.id.btinstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(json!=null && service != null){
                    Download download = new Download();
                    download.setId(id);
                    download.setName(name);
                    download.setVersion(versionName);
                    download.setIcon(iconpath + finalIcon);
                    service.startDownloadFromJson(json, id, download);
                }


            }
        });

        findViewById(R.id.ic_action_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(json!=null && service != null){
                    service.stopDownload(id);
                }


            }
        });

    }

    @Subscribe
    public void onDownloadStatusUpdate(DownloadStatusEvent download) {

        if (download.getId() == id) {
            onDownloadUpdate(service.getDownload(id).getDownload());
        }

    }

    @Subscribe
    public void onDownloadUpdate(Download download) {


        if (download.getId() == id) {

            TextView progressText = (TextView) findViewById(R.id.progress);
            ProgressBar pb = (ProgressBar) findViewById(R.id.downloading_progress);
            findViewById(R.id.download_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.btinstall).setVisibility(View.GONE);
            findViewById(R.id.badge_layout).setVisibility(View.GONE);

            switch(download.getDownloadState()){

                case ACTIVE:
                    pb.setIndeterminate(false);
                    pb.setProgress(download.getProgress());
                    progressText.setText(download.getProgress() + "% - " + Utils.formatBytes((long) download.getSpeed()));
                    break;
                case INACTIVE:
                    break;
                case COMPLETE:
                    findViewById(R.id.download_progress).setVisibility(View.GONE);
                    findViewById(R.id.btinstall).setVisibility(View.VISIBLE);
                    findViewById(R.id.badge_layout).setVisibility(View.VISIBLE);
                    pb.setProgress(download.getProgress());
                    progressText.setText(download.getDownloadState().name());
                    break;
                case NOSTATE:
                    break;
                case PENDING:
                    break;
                case ERROR:
                    progressText.setText(download.getDownloadState().name());
                    break;
            }



        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {

    }



    public class AppViewPager extends FixedFragmentStatePagerAdapter{

        private final String[] TITLES = {"Info", "Rating", "Related", "Advanced"};

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        public AppViewPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {


            switch (i){
                case 0:
                return new FragmentAppView.FragmentAppViewDetails();
                case 1:
                    return new FragmentAppView.FragmentAppViewRating();
                case 2:
                    return new FragmentAppView.FragmentAppViewRelated();
                case 3:
                    return new FragmentAppView.FragmentAppViewSpecs();
                default:
                    return null;
            }



        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    public static class DetailsEvent {

        Details details;

        public DetailsEvent(Details details) {
            this.details = details;
        }

        public String getDescription(){
            return details.getDescription();
        }

        public String getVersionName(){
            return details.getVersion();
        }

        public String getPublisher(){
            return "Publisher";
        }

        public long getSize(){
            return details.getSize();
        }

        public int getDownloads(){
            return details.getDownloads();
        }


    }



    public static class RatingEvent {
        private ArrayList<Comment> comments;

        public RatingEvent(){

        }

        public ArrayList<Comment> getComments() {
            return comments;
        }

        public void setComments(ArrayList<Comment> comments) {
            this.comments = comments;
        }
    }

    public static class SpecsEvent {
        private SpecsEvent(Specs specs) {

        }
    }

    public static class RelatedAppsEvent {
        private RelatedAppsEvent(Related related) {

        }
    }

    private static class Details {

        public void setDescription(String description) {
            this.description = description;
        }

        private String description;

        public String getStore() {
            return store;
        }

        public void setStore(String store) {
            this.store = store;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public int getDownloads() {

            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
        }

        private int downloads;
        private String store;
        private String publisher;
        private String version;
        private long size;

        public String getDescription() {
            return description;
        }

    }

    private static class Rating {
    }

    private static class Specs {
    }

    private static class Related {
    }

}
