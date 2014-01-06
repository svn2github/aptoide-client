package cm.aptoide.ptdev;

import android.accounts.*;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.*;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentAppView;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromId;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromMd5;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class AppViewActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOGIN_REQUEST_CODE = 123;

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    private GetApkInfoJson json;
    private String name;
    private RequestListener<GetApkInfoJson> requestListener = new RequestListener<GetApkInfoJson>() {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(AppViewActivity.this , "Error request", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(final GetApkInfoJson getApkInfoJson) {
            AppViewActivity.this.json = getApkInfoJson;

            if ("OK".equals(json.getStatus())) {


                name = getApkInfoJson.getMeta().getTitle();
                versionName = getApkInfoJson.getApk().getVername();
                package_name = getApkInfoJson.getApk().getPackage();
                repoName = getApkInfoJson.getApk().getRepo();
                if (getApkInfoJson.getApk().getIconHd() != null) {
                    icon = getApkInfoJson.getApk().getIconHd();
                    String sizeString = IconSizes.generateSizeString(AppViewActivity.this);
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                } else {
                    icon = getApkInfoJson.getApk().getIcon();
                }
                appName.setText(name);
                appVersionName.setText(versionName);
                ImageLoader.getInstance().displayImage(icon, appIcon);
                bindService(new Intent(AppViewActivity.this, DownloadService.class), downloadConnection, BIND_AUTO_CREATE);


                findViewById(R.id.btinstall).setOnClickListener(new InstallListener(icon, name, versionName, package_name));


                findViewById(R.id.ic_action_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (service != null) {
                            service.stopDownload(id);
                        }


                    }
                });
                publishEvents();

                latestVersion = (TextView) findViewById(R.id.app_get_latest);
                if (json.getLatest() != null) {
                    latestVersion.setVisibility(View.VISIBLE);
                    SpannableString spanString = new SpannableString(getString(R.string.get_latest));
                    spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
                    latestVersion.setText(spanString);
                    latestVersion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = json.getLatest();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            url = url.replaceAll(" ", "%20");
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });
                }else{
                    latestVersion.setVisibility(View.GONE);
                }

            } else {
                for (Error error : json.getErrors()) {
                    Toast.makeText(AppViewActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
                }
            }

            show(true, true);

        }
    };
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersionName;
    private TextView latestVersion;

    private long id;
    private int downloads;
    private String repoName;
    private int minSdk;
    private DownloadService service;
    private String icon;
    private boolean isUpdate;

    public boolean isUpdate() {
        return isUpdate;
    }


    public class InstallListener implements View.OnClickListener{

        private String icon;
        private String name;
        private String versionName;
        private String package_name;

        public InstallListener(String icon, String name, String versionName, String package_name) {
            this.icon = icon;
            this.name = name;
            this.versionName = versionName;
            this.package_name = package_name;
        }

        @Override
        public void onClick(View v) {
            Download download = new Download();
            download.setId(id);
            download.setName(this.name);
            download.setVersion(this.versionName);
            download.setIcon(this.icon);
            download.setPackageName(this.package_name);
            service.startDownloadFromJson(json, id, download);
        }
    }

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
    private String screen;
    private String package_name;
    private String versionName;
    private Object cacheKey;
    private String token;

    public String getRepoName() {
        return repoName;
    }

    public String getPackage_name() {
        return package_name;
    }

    public String getVersionName() {
        return versionName;
    }

    @Produce
    public ScreenshotsEvent publishScreenshots(){

        Screenshots screenshots = new Screenshots();
        Log.d("Aptoide-AppView", "PublishScreenshots");
        if (json != null) {

            if (!json.getStatus().equals("FAIL")) {
                screenshots.setScreenshots(json.getMedia().getSshots());
            } else {
//                for(String error : json.getErrors()){
//                    Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
//                }
            }
        }

        return new ScreenshotsEvent(screenshots);

    }


    @Produce
    public DetailsEvent publishDetails() {

        Details details = new Details();
        Log.d("Aptoide-AppView", "PublishingDetails");
        if (json != null) {

            if (!json.getStatus().equals("FAIL")) {
                if (json.getMeta().getDeveloper() != null) details.setDeveloper(json.getMeta().getDeveloper());
                if (json.getMeta().getNews() != null) details.setNews(json.getMeta().getNews());
                Log.d("Aptoide-AppView", "Description: " + json.getMeta().getDescription());
                details.setDescription(json.getMeta().getDescription());
                details.setSize(json.getApk().getSize().longValue());
                details.setStore(repoName);
                details.setDownloads(downloads);
                details.setScreenshots(json.getMedia().getSshots());
                details.setRating("" + json.getMeta().getLikevotes().getRating());
                details.setLikes("" + json.getMeta().getLikevotes().getLikes());
                details.setDontLikes("" + json.getMeta().getLikevotes().getDislikes());
            } else {

                //for(String error : json.getErrors()){
                //  Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                //}
            }
        }

        return new DetailsEvent(details);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(service!=null) unbindService(downloadConnection);
    }

    @Produce
    public RelatedAppsEvent publishRelatedApps(){

        Related relatedApps = new Related();

        return new RelatedAppsEvent(relatedApps);

    }

    @Produce
    public SpecsEvent publishSpecs() {

        SpecsEvent specs = new SpecsEvent();
        if (json != null) {

            specs.setPermissions(new ArrayList<String>(json.getApk().getPermissions()));
            specs.setMinSdk(minSdk);
            specs.setMinScreen(Filters.Screen.lookup(screen));

        }
        return specs;

    }

    @Produce
    public RatingEvent publishRating() {

        RatingEvent event = new RatingEvent();
        if (json != null) {
            event.setComments(new ArrayList<Comment>(json.getMeta().getComments()));
            event.setCacheString(json.getApk().getPackage() + json.getApk().getVername());

            if (json.getMeta().getLikevotes().getUservote() != null) {
                event.setUservote(json.getMeta().getLikevotes().getUservote());
            }

        }
        return event;

    }


    private void publishEvents() {
        BusProvider.getInstance().post(publishScreenshots());
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
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_view);



//            AccountManager accountManager = AccountManager.get(AppViewActivity.this);
//            Account account = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
//            accountManager.getAuthToken(account, AccountGeneral.ACCOUNT_TYPE, null, AppViewActivity.this, new AccountManagerCallback<Bundle>() {
//                @Override
//                public void run(AccountManagerFuture<Bundle> future) {
//                    try {
//                        token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
//                        Log.d("Aptoide-Login", "Token is" + token);
//                        Toast.makeText(AppViewActivity.this, "Token is: " + token, Toast.LENGTH_LONG).show();
//                    } catch (OperationCanceledException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (AuthenticatorException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, null);


            AccountManager accountManager = AccountManager.get(AppViewActivity.this);

        if(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length>0){

            Account account = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];
            accountManager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, AppViewActivity.this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }
                    continueLoading();
                }
            }, null);

        }else{
            continueLoading();

        }






    }

    private void continueLoading() {
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


        if(getIntent().getBooleanExtra("fromRollback", false)){

            GetApkInfoRequestFromMd5 request = new GetApkInfoRequestFromMd5(getApplicationContext());

            String md5sum = getIntent().getStringExtra("md5sum");
            request.setMd5Sum(md5sum);

            if(token!=null){
                request.setToken(token);
            }
            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, md5sum, DurationInMillis.ONE_HOUR, requestListener);

        }else if(getIntent().getBooleanExtra("fromMyapp", false)){

            GetApkInfoRequestFromId request = new GetApkInfoRequestFromId(getApplicationContext());

            String id = getIntent().getStringExtra("id");
            request.setAppId(id);

            repoName = getIntent().getStringExtra("repoName");
            request.setRepoName(repoName);

            if(token!=null){
                request.setToken(token);
            }

            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, id, DurationInMillis.ONE_HOUR, requestListener);

        }else{
            getSupportLoaderManager().initLoader(50, getIntent().getExtras(), this);
        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_view, menu);



        // Locate MenuItem with ShareActionProvider
        SupportMenuItem item = (SupportMenuItem) menu.findItem(R.id.menu_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("type/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"SUBJECT");
        shareIntent.putExtra(Intent.EXTRA_TEXT,"TEXT TEXT");


        mShareActionProvider.setShareIntent(shareIntent);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        } else if (i == R.id.menu_schedule) {

        } else if (i == R.id.menu_share) {

        } else if (i == R.id.menu_uninstall) {

            Fragment uninstallFragment = new UninstallRetainFragment(name, package_name, versionName, icon);

            getSupportFragmentManager().beginTransaction().add(uninstallFragment, "uninstallFrag").commit();

        } else if (i == R.id.menu_search_other) {

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
        name = apkCursor.getString(apkCursor.getColumnIndex(Schema.Apk.COLUMN_NAME));
        package_name = apkCursor.getString(apkCursor.getColumnIndex("package_name"));
        versionName = apkCursor.getString(apkCursor.getColumnIndex(Schema.Apk.COLUMN_VERNAME));
        String localIcon = apkCursor.getString(apkCursor.getColumnIndex(Schema.Apk.COLUMN_ICON));
        final String iconpath = apkCursor.getString(apkCursor.getColumnIndex("iconpath"));
        downloads = apkCursor.getInt(apkCursor.getColumnIndex(Schema.Apk.COLUMN_DOWNLOADS));
        minSdk = apkCursor.getInt(apkCursor.getColumnIndex(Schema.Apk.COLUMN_SDK));
        screen = apkCursor.getString(apkCursor.getColumnIndex(Schema.Apk.COLUMN_SCREEN));
        float rating = apkCursor.getFloat(apkCursor.getColumnIndex(Schema.Apk.COLUMN_RATING));

        appName.setText(name);
        appVersionName.setText(versionName);
//        ratingBar.setRating(rating);
        String sizeString = IconSizes.generateSizeString(this);
        if(localIcon.contains("_icon")){
            String[] splittedUrl = localIcon.split("\\.(?=[^\\.]+$)");
            localIcon = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(icon = iconpath + localIcon , appIcon);
        GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);

        if(token!=null){
            request.setToken(token);
        }
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, package_name + repoName, DurationInMillis.ONE_HOUR, requestListener);





    }

    @Subscribe
    public void onDownloadStatusUpdate(DownloadStatusEvent download) {

        if (download.getId() == id) {
            onDownloadUpdate(service.getDownload(id).getDownload());
        }

    }

    @Subscribe
    public void onRefresh(AppViewRefresh event) {

        GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);
        if(token!=null)request.setToken(token);
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, package_name + repoName, DurationInMillis.ONE_HOUR, requestListener);


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
                    progressText.setText(download.getProgress() + "% - " + Utils.formatBits((long) download.getSpeed())+"/s");
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

    public SpiceManager getSpice() {
        return spiceManager;
    }

    public String getCacheKey() {
        return package_name+repoName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public class ScreenshotsEvent {

        Screenshots screenshots;

        public ScreenshotsEvent(Screenshots screenshots) {
            this.screenshots = screenshots;
        }

        public List getScreenshots(){
            return screenshots.getScreenshots();
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

            if(details.getDeveloper()!=null){
                return details.getDeveloper().getInfo().getName();
            }else{
                return "N/A";
            }
        }

        public GetApkInfoJson.Meta.Developer getDeveloper(){
            return details.getDeveloper();
        }

        public long getSize(){
            return details.getSize();
        }

        public int getDownloads(){
            return details.getDownloads();
        }

        public List getScreenshots(){
            return details.getScreenshots();
        }

        public String getLikes(){
            return details.getLikes();
        }

        public String getDontLikes(){
            return details.getDontLikes();
        }

        public String getNews(){
            return details.getNews();
        }

        public String getRating() {
            return details.rating;
        }
    }



    public static class RatingEvent {
        private ArrayList<Comment> comments;
        private String uservote;

        public String getCacheString() {
            return cacheString;
        }

        public void setCacheString(String cacheString) {
            this.cacheString = cacheString;
        }

        private String cacheString;
        public RatingEvent(){

        }

        public ArrayList<Comment> getComments() {
            return comments;
        }

        public void setComments(ArrayList<Comment> comments) {
            this.comments = comments;
        }

        public void setUservote(String uservote) {
            this.uservote = uservote;
        }

        public String getUservote() {
            return uservote;
        }
    }

    public static class SpecsEvent {
        private ArrayList<String> permissions;
        private Filters.Screen minScreen= Filters.Screen.normal;
        private int minSdk;

        private SpecsEvent() {

        }

        public ArrayList getPermissions(){ return permissions; }

        public void setPermissions(ArrayList<String> permissions) { this.permissions = permissions; }

        public Filters.Screen getMinScreen() { return minScreen; }

        public int getMinSdk() { return minSdk; }

        public void setMinSdk(int minSdk) { this.minSdk = minSdk; }

        public void setMinScreen(Filters.Screen minScreen) { this.minScreen = minScreen; }
    }

    public static class RelatedAppsEvent {
        private RelatedAppsEvent(Related related) {

        }
    }

    private static class Screenshots {
        private List<String> screenshots;

        public List<String> getScreenshots() { return screenshots; }

        public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }
    }

    private static class Details {

        private GetApkInfoJson.Meta.Developer developer;
        private String news;
        public String rating;

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
        private List<String> screenshots;
        private String likes;
        private String dontLikes;

        public String getDescription() {
            return description;
        }

        public List<String> getScreenshots() { return screenshots; }

        public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

        public String getLikes() { return likes; }

        public void setLikes(String likes) { this.likes = likes; }

        public String getDontLikes() { return dontLikes; }

        public void setDontLikes(String dontLikes) { this.dontLikes = dontLikes; }

        public void setDeveloper(GetApkInfoJson.Meta.Developer developer) {
            this.developer = developer;
        }

        public GetApkInfoJson.Meta.Developer getDeveloper() {
            return developer;
        }

        public void setNews(String news) {
            this.news = news;
        }

        public String getNews() {
            return news;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getRating() {
            return rating;
        }
    }

    private static class Rating {
    }

    private static class Specs {
    }

    private static class Related {
    }

    private Account getCurrentAccount() {
        Account[] account = AccountManager.get(this).getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if(account.length != 0) {
            return account[0];

        } else {

            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("login", true);
            startActivityForResult(i, LOGIN_REQUEST_CODE);
        }

        return null;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LOGIN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {

            } else {

            }
        }
    }


    private void show(boolean shown, boolean animate) {


        View mListContainer = findViewById(R.id.pager_host);
        View mProgressContainer = findViewById(R.id.progressBar);
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }


}
