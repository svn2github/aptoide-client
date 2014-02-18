package cm.aptoide.ptdev;

import android.accounts.*;
import android.annotation.TargetApi;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
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
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ErrorDialog;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.downloadmanager.event.DownloadStatusEvent;
import cm.aptoide.ptdev.downloadmanager.state.ErrorState;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.FragmentAppView;
import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.GetApkInfoRequest;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromId;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromMd5;
import cm.aptoide.ptdev.webservices.UpdateUserRequest;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.mopub.mobileads.MoPubView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import javax.xml.transform.ErrorListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class AppViewActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, MyAppsAddStoreInterface {

    private static final int LOGIN_REQUEST_CODE = 123;
    public static final int DOWGRADE_REQUEST_CODE = 456;

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private MoPubView moPubView;

    private GetApkInfoJson json;
    private String name;
    private boolean isFromActivityResult;
    private String wUrl;
    private String md5;

    public GetApkInfoJson.Malware.Reason getReason() {
        return reason;
    }

    private static GetApkInfoJson.Malware.Reason reason;
    private RequestListener<GetApkInfoJson> requestListener = new RequestListener<GetApkInfoJson>() {

        @Override
        public void onRequestFailure(SpiceException e) {
            AptoideDialog.errorDialog().show(getSupportFragmentManager(), "errorDialog");
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onRequestSuccess(final GetApkInfoJson getApkInfoJson) {

            Log.d("Aptoide-AppView", "Json is null? " + String.valueOf(getApkInfoJson==null));
            if (getApkInfoJson != null) {
                AppViewActivity.this.json = getApkInfoJson;
                if ("OK".equals(json.getStatus())) {

                    name = getApkInfoJson.getMeta().getTitle();
                    versionName = getApkInfoJson.getApk().getVername();
                    package_name = getApkInfoJson.getApk().getPackage();
                    repoName = getApkInfoJson.getApk().getRepo();
                    wUrl = getApkInfoJson.getMeta().getWUrl();
                    screen = getApkInfoJson.getApk().getMinScreen();
                    minSdk = getApkInfoJson.getApk().getMinSdk().intValue();
                    Log.d("AppView", "wUrl " + wUrl);
                    boolean showLatestString = false;
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



                    try {
                        PackageInfo info = getPackageManager().getPackageInfo(package_name, PackageManager.GET_SIGNATURES);

                        if (getApkInfoJson.getApk().getVercode().intValue() > info.versionCode) {
                            isUpdate=true;
                            ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.update));
                            findViewById(R.id.btinstall).setOnClickListener(new InstallListener(icon, name, versionName, package_name));

                        } else if (getApkInfoJson.getApk().getVercode().intValue() < info.versionCode) {

                            ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.downgrade));
                            findViewById(R.id.btinstall).setOnClickListener(new DowngradeListener(icon, name, info.versionName, versionName, info.packageName));

                        } else {


                            final Intent i = getPackageManager().getLaunchIntentForPackage(package_name);

                            ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.open));

                            if (i != null) {
                                findViewById(R.id.btinstall).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(i);
                                    }
                                });

                            } else {
                                findViewById(R.id.btinstall).setEnabled(false);
                            }


                        }


                    } catch (PackageManager.NameNotFoundException e) {
                        findViewById(R.id.btinstall).setOnClickListener(new InstallListener(icon, name, versionName, package_name));

                    }





                    findViewById(R.id.ic_action_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (service != null) {
                                service.stopDownload(downloadId);
                            }


                        }
                    });

                    findViewById(R.id.ic_action_resume).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (service != null) {
                                service.resumeDownload(downloadId);
                            }


                        }
                    });

                    if (getApkInfoJson.getMalware() != null) {
                        boolean showBadgeLayout = false;
                        Log.d("AppViewActivity-malwareStatus","status: "+(getApkInfoJson.getMalware().getStatus()));
                        if (getApkInfoJson.getMalware().getStatus().equals("scanned")) {
                            ((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.ic_trusted);
                            ((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.trusted));
                            showBadgeLayout = true;
                            showLatestString = true;
                        } else if (getApkInfoJson.getMalware().getStatus().equals("warn")) {
                            ((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.ic_warning);
                            ((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.warning));
                            showBadgeLayout = true;
                            showLatestString = false;
                        } else if (getApkInfoJson.getMalware().getStatus().equals("unknown")) {
                            showBadgeLayout = false;
                            showLatestString = true;
                        }

                        reason =  getApkInfoJson.getMalware().getReason();

                        if (showBadgeLayout) {
                            findViewById(R.id.badge_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.badge_layout).startAnimation(AnimationUtils.loadAnimation(AppViewActivity.this, android.R.anim.fade_in));
                            findViewById(R.id.badge_layout).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AptoideDialog.badgeDialog(name, getApkInfoJson.getMalware().getStatus()).show(getSupportFragmentManager(), "badgeDialog");

                                }
                            });
                        }
                    }



                    versionCode = json.getApk().getVercode().intValue();
                    latestVersion = (TextView) findViewById(R.id.app_get_latest);
                    if (json.getLatest() != null) {
                        latestVersion.setVisibility(View.VISIBLE);

                        String getLatestString;
                        if (showLatestString) {
                            getLatestString = getString(R.string.get_latest_version);
                        } else {
                            getLatestString = getString(R.string.get_latest_version_and_trusted);
                        }

                        SpannableString spanString = new SpannableString(getLatestString);
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
                    } else {
                        latestVersion.setVisibility(View.GONE);
                    }

                    if (getIntent().getBooleanExtra("fromMyapp", false)) {
                        getIntent().removeExtra("fromMyapp");
                        AptoideDialog.myappInstall( name).show(getSupportFragmentManager(), "myApp");
                    }
                    md5 = json.getApk().getMd5sum();
                    publishEvents();
                    supportInvalidateOptionsMenu();
                    //if (!isShown) show(true, true);

                    if (isFromActivityResult) {

                        Log.d("Downgrade", "iffromactivityresult");
                        Download download = new Download();
                        download.setId(downloadId);
                        download.setName(name);
                        download.setVersion(versionName);
                        download.setIcon(icon);
                        download.setPackageName(package_name);
                        service.startDownloadFromJson(json, downloadId, download);

                        isFromActivityResult = false;
                    }

                    moPubView = (MoPubView) findViewById(R.id.adview);
                    if (Build.VERSION.SDK_INT > 11) {
                        moPubView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                    moPubView.setVisibility(View.VISIBLE);
                    moPubView.setAdUnitId("18947d9a99e511e295fa123138070049");
                    moPubView.loadAd();
                    downloadId = json.getApk().getMd5sum().hashCode();

                    if(service !=null && service.getDownload(downloadId).getDownload() != null){
                        onDownloadUpdate(service.getDownload(downloadId).getDownload());
                    }

                } else {
                    for (Error error : json.getErrors()) {
                        Toast.makeText(AppViewActivity.this, error.getMsg(), Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
            }


        }
    };

    DialogInterface.OnDismissListener onDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {

            if (!new Database(Aptoide.getDb()).existsServer(AptoideUtils.RepoUtils.formatRepoUri(repoName + ".store.aptoide.com/"))) {
                AptoideDialog.addMyAppStore("http://" + repoName + ".store.aptoide.com/").show(getSupportFragmentManager(), "myAppStore");
            }
        }
    };

    public DialogInterface.OnClickListener getOnMyAppAddStoreListener(String repo) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> repo = new ArrayList<String>();
                repo.add("http://" + repoName + ".store.aptoide.com/");
                Intent i = new Intent(AppViewActivity.this, MainActivity.class);
                i.putExtra("nodialog", true);
                i.putExtra("newrepo", repo);
                i.addFlags(12345);
                startActivity(i);
            }
        };
    }



    public DialogInterface.OnDismissListener getOnDismissListener(){
        return onDismissListener;
    }

    public DialogInterface.OnClickListener getMyAppListener(){
        return new InstallListener(icon, name, versionName, package_name);
    }


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
    private int versionCode;
    private boolean isShown = false;
    private int downloadId;

    public boolean isUpdate() {
        return isUpdate;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DialogInterface.OnClickListener getCancelListener() {

        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
    }

    public DialogInterface.OnClickListener getTryAgainListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                continueLoading(null);
            }
        };
    }

    public void updateUsername(final String username){

        UpdateUserRequest request = new UpdateUserRequest(this);
        request.setName(username);

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(request, new RequestListener<CreateUserJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(fragment!=null)fragment.dismiss();
            }

            @Override
            public void onRequestSuccess(CreateUserJson createUserJson) {
                DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(fragment!=null)fragment.dismiss();

                if(createUserJson.getStatus().equals("OK")){
                    Toast.makeText(AppViewActivity.this, R.string.username_success, Toast.LENGTH_LONG).show();
                    PreferenceManager.getDefaultSharedPreferences(AppViewActivity.this).edit().putString("username", username).commit();
                }else{
                    for(String error: createUserJson.getErrors()){
                        Toast.makeText(AppViewActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }


            }
        });
    }


    public class InstallListener implements View.OnClickListener, DialogInterface.OnClickListener {

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

            download.setId(downloadId);
            download.setName(this.name);
            download.setVersion(this.versionName);
            download.setIcon(this.icon);
            download.setPackageName(this.package_name);

            service.startDownloadFromJson(json, downloadId, download);
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            onClick(null);
        }
    }

    public class InstallFromUrlListener implements View.OnClickListener, DialogInterface.OnClickListener {

        private String icon;
        private String name;
        private String versionName;
        private String package_name;
        private final String md5;
        private final String url;


        public InstallFromUrlListener(String icon, String name, String versionName, String package_name, String md5, String url) {
            this.icon = icon;
            this.name = name;
            this.versionName = versionName;
            this.package_name = package_name;
            this.md5 = md5;
            this.url = url;
        }

        @Override
        public void onClick(View v) {

            Download download = new Download();

            download.setId(downloadId);
            download.setName(this.name);
            download.setVersion(this.versionName);
            download.setIcon(this.icon);
            download.setPackageName(this.package_name);

            service.startDownloadFromUrl(url, md5, downloadId, download);
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            onClick(null);
        }
    }



    public class DowngradeListener implements  View.OnClickListener{

        private String icon;
        private String name;
        private String versionName;
        private String downgradeVersion;
        private String package_name;


        public DowngradeListener(String icon, String name, String versionName, String downgradeVersion, String package_name) {
            this.icon = icon;
            this.name = name;
            this.versionName = versionName;
            this.downgradeVersion = downgradeVersion;
            this.package_name = package_name;
        }

        @Override
        public void onClick(View v) {
            Fragment downgrade = new UninstallRetainFragment(name, package_name, versionName, downgradeVersion, icon);
            getSupportFragmentManager().beginTransaction().add(downgrade, "downgrade").commit();
        }
    }

    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder downloadService) {
            service = ((DownloadService.LocalBinder)downloadService).getService();

            if(service.getDownload(downloadId).getDownload()!=null){
                onDownloadUpdate(service.getDownload(downloadId).getDownload());
            }else{
                findViewById(R.id.btinstall).setVisibility(View.VISIBLE);
                findViewById(R.id.btinstall).startAnimation(AnimationUtils.loadAnimation(AppViewActivity.this, android.R.anim.fade_in));
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private String screen;
    private String package_name;
    private String versionName;
    private String cacheKey;
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
    public DetailsEvent publishDetails() {

        Details details = new Details();
        Log.d("Aptoide-AppView", "PublishingDetails");
        details.setDownloads(downloads);
        details.setStore(repoName);

        if (json != null) {

            if (!json.getStatus().equals("FAIL")) {
                if (json.getMeta().getDeveloper() != null) details.setDeveloper(json.getMeta().getDeveloper());
                if (json.getMeta().getNews() != null) details.setNews(json.getMeta().getNews());
                Log.d("Aptoide-AppView", "Description: " + json.getMeta().getDescription());
                details.setDescription(json.getMeta().getDescription());
                details.setSize(json.getApk().getSize().longValue());

                if (json.getMedia().getSshots_hd() != null) {
                    details.setScreenshotsHd(json.getMedia().getSshots_hd());
                } else {
                    details.setScreenshots(json.getMedia().getSshots());
                }
                if (json.getMedia().getVideos() != null) {
                    details.setVideos(json.getMedia().getVideos());
                }

                details.setRating(json.getMeta().getLikevotes().getRating().floatValue());
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
        if(service!=null) unbindService(downloadConnection);
        if (moPubView != null) {
            moPubView.destroy();
        }
        super.onDestroy();
    }

    @Produce
    public RelatedAppsEvent publishRelatedApps(){

        Related relatedApps = new Related();

        return new RelatedAppsEvent(relatedApps);

    }

    @Produce
    public SpecsEvent publishSpecs() {

        SpecsEvent specs = new SpecsEvent();
        if (json != null && !json.getStatus().equals("FAIL")) {

            specs.setPermissions(new ArrayList<String>(json.getApk().getPermissions()));
            specs.setMinSdk(minSdk);
            specs.setMinScreen(Filters.Screen.lookup(screen));

        }
        return specs;

    }

    @Produce
    public RatingEvent publishRating() {

        RatingEvent event = new RatingEvent();
        if (json != null && !json.getStatus().equals("FAIL")) {

            event.setComments(new ArrayList<Comment>(json.getMeta().getComments()));
            event.setCacheString(json.getApk().getPackage() + json.getApk().getVername());

            if (json.getMeta().getLikevotes().getUservote() != null) {
                event.setUservote(json.getMeta().getLikevotes().getUservote());
            }

        }
        return event;

    }


    private void publishEvents() {
        Log.d("Aptoide-AppViewActivity", "Publishing revents");
        BusProvider.getInstance().post(publishDetails());
        BusProvider.getInstance().post(publishRelatedApps());
        BusProvider.getInstance().post(publishSpecs());
        BusProvider.getInstance().post(publishRating());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("cacheKey", cacheKey);
        outState.putString("packageName", package_name);
        outState.putInt("downloadId", downloadId);
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
    protected void onCreate(final Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_view);

        if(savedInstanceState!=null){
            package_name = savedInstanceState.getString("packageName");
            downloadId = savedInstanceState.getInt("downloadId");
            cacheKey = savedInstanceState.getString("cacheKey");
        }

        AccountManager accountManager = AccountManager.get(AppViewActivity.this);


        if (accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length > 0) {

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
                    continueLoading(savedInstanceState);
                }
            }, null);

        } else {
            continueLoading(savedInstanceState);

        }






    }

    private void continueLoading(Bundle savedInstanceState) {
        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name);
        appVersionName = (TextView) findViewById(R.id.app_version);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        id = getIntent().getExtras().getLong("id");

        if(getIntent().getExtras().containsKey("appName")){
            name = getIntent().getExtras().getString("appName");
            appName.setText(name);
        }

        if(getIntent().getExtras().containsKey("versionName")){
            versionName = getIntent().getExtras().getString("versionName");
            appVersionName.setText(versionName);
        }



        if(pager != null){
            PagerAdapter adapter = new AppViewPager(getSupportFragmentManager());
            pager.setAdapter(adapter);
            PagerSlidingTabStrip slidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            slidingTabStrip.setViewPager(pager);
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {

            if (getIntent().getBooleanExtra("fromRollback", false)) {

                GetApkInfoRequestFromMd5 request = new GetApkInfoRequestFromMd5(getApplicationContext());

                String md5sum = getIntent().getStringExtra("md5sum");
                request.setMd5Sum(md5sum);

                if (token != null) {
                    request.setToken(token);
                }
                cacheKey = md5sum;

                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, md5sum, DurationInMillis.ONE_HOUR, requestListener);

            } else if (getIntent().getBooleanExtra("fromMyapp", false)) {

                GetApkInfoRequestFromId request = new GetApkInfoRequestFromId(getApplicationContext());

                long id = getIntent().getLongExtra("id", 0);
                request.setAppId(String.valueOf(id));

                if (token != null) {
                    request.setToken(token);
                }
                cacheKey = String.valueOf(id);

                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, id, DurationInMillis.ONE_HOUR, requestListener);

            } else if (getIntent().getBooleanExtra("fromRelated", false)) {

                GetApkInfoRequestFromMd5 request = new GetApkInfoRequestFromMd5(getApplicationContext());
                repoName = getIntent().getStringExtra("repoName");
                String md5sum = getIntent().getStringExtra("md5sum");
                request.setMd5Sum(md5sum);
                request.setRepoName(repoName);
                if (token != null) {
                    request.setToken(token);
                }
                cacheKey = md5sum;
                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

            } else {
                getSupportLoaderManager().initLoader(50, getIntent().getExtras(), this);
            }

            bindService(new Intent(AppViewActivity.this, DownloadService.class), downloadConnection, BIND_AUTO_CREATE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_view, menu);
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
            if(md5 !=null){
                new Database(Aptoide.getDb()).insertScheduledDownload(package_name, md5, versionName, repoName, name, icon);
                Toast.makeText(this, R.string.addSchDown, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.menu_uninstall) {

            Fragment uninstallFragment = new UninstallRetainFragment(name, package_name, versionName, icon);

            getSupportFragmentManager().beginTransaction().add(uninstallFragment, "uninstallFrag").commit();

        } else if (i == R.id.menu_search_other) {

            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + package_name));

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast toast = Toast.makeText(this, getString(R.string.error_no_market), Toast.LENGTH_SHORT);
                toast.show();
            }


        } else if (i == R.id.menu_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.install) + " \"" + name + "\"");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, wUrl);

            if (wUrl != null) {
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
            }
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
        md5 = apkCursor.getString(apkCursor.getColumnIndex(Schema.Apk.COLUMN_MD5));
        String apkpath = apkCursor.getString(apkCursor.getColumnIndex("apk_path"));
        String path = apkCursor.getString(apkCursor.getColumnIndex("path"));
        versionCode = apkCursor.getInt(apkCursor.getColumnIndex(Schema.Apk.COLUMN_VERCODE));


        float rating = apkCursor.getFloat(apkCursor.getColumnIndex(Schema.Apk.COLUMN_RATING));

        appName.setText(Html.fromHtml(name).toString());
        appVersionName.setText(Html.fromHtml(versionName).toString());
//        ratingBar.setRating(rating);
        String sizeString = IconSizes.generateSizeString(this);
        if(localIcon.contains("_icon")){
            String[] splittedUrl = localIcon.split("\\.(?=[^\\.]+$)");
            localIcon = splittedUrl[0] + "_" + sizeString + "."+ splittedUrl[1];
        }

        ImageLoader.getInstance().displayImage(icon = iconpath + localIcon , appIcon);
        downloadId = md5.hashCode();


        GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);
        request.setVercode(versionCode);

        if(token!=null){
            request.setToken(token);
        }

        cacheKey = package_name + repoName + versionName;
        BusProvider.getInstance().post(publishDetails());


        try {
            PackageInfo info = getPackageManager().getPackageInfo(package_name, PackageManager.GET_SIGNATURES);

            if (versionCode > info.versionCode) {
                isUpdate = true;
                ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.update));
                findViewById(R.id.btinstall).setEnabled(true);
                findViewById(R.id.btinstall).setOnClickListener(new InstallFromUrlListener(icon, name, versionName, package_name, md5, apkpath + path));
            } else if (versionCode < info.versionCode) {

                ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.downgrade));
                findViewById(R.id.btinstall).setOnClickListener(new DowngradeListener(icon, name, info.versionName, versionName, info.packageName));

            } else {

                final Intent i = getPackageManager().getLaunchIntentForPackage(package_name);

                ((TextView) findViewById(R.id.btinstall)).setText(getString(R.string.open));

                if (i != null) {
                    findViewById(R.id.btinstall).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(i);
                        }
                    });

                } else {
                    findViewById(R.id.btinstall).setEnabled(false);
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            findViewById(R.id.btinstall).setOnClickListener(new InstallListener(icon, name, versionName, package_name));
        }


        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

    }

    @Subscribe
    public void onDownloadEventUpdate(DownloadEvent download) {

        if (service != null && download.getId() == downloadId) {
            onDownloadUpdate(service.getDownload(download.getId()).getDownload());
        }

    }

    @Subscribe
    public void onDownloadStatusUpdate(Download download) {

        if (download.getId() == downloadId) {
            onDownloadUpdate(download);
        }

    }

    @Subscribe
    public void onInstalledEvent(InstalledApkEvent event){
        onRefresh(null);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spiceManager.addListenerIfPending(GetApkInfoJson.class, cacheKey, requestListener);
        spiceManager.getFromCache(GetApkInfoJson.class, cacheKey, DurationInMillis.ONE_HOUR, requestListener);
    }

    @Subscribe
    public void onRefresh(AppViewRefresh event) {

        GetApkInfoRequest request = new GetApkInfoRequest(getApplicationContext());

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);
        if(token!=null)request.setToken(token);

        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);


    }

    @Subscribe
    public void onDownloadUpdate(Download download) {


        if (download.getId() == downloadId) {

            TextView progressText = (TextView) findViewById(R.id.progress);
            ProgressBar pb = (ProgressBar) findViewById(R.id.downloading_progress);

            findViewById(R.id.download_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.btinstall).setVisibility(View.GONE);
            findViewById(R.id.badge_layout).setVisibility(View.GONE);

            switch(download.getDownloadState()){

                case ACTIVE:
                    findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                    pb.setIndeterminate(false);
                    pb.setProgress(download.getProgress());
                    progressText.setText(download.getProgress() + "% - " + Utils.formatBits((long) download.getSpeed())+"/s");
                    break;
                case INACTIVE:
                    break;
                case COMPLETE:
                    findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                    findViewById(R.id.download_progress).setVisibility(View.GONE);
                    findViewById(R.id.btinstall).setVisibility(View.VISIBLE);
                    findViewById(R.id.badge_layout).setVisibility(View.VISIBLE);
                    pb.setProgress(download.getProgress());
                    progressText.setText(download.getDownloadState().name());
                    break;
                case NOSTATE:
                    break;
                case PENDING:
                    findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                    pb.setIndeterminate(false);
                    pb.setProgress(download.getProgress());
                    progressText.setText(getString(R.string.download_pending));
                    break;
                case ERROR:
                    findViewById(R.id.ic_action_resume).setVisibility(View.VISIBLE);
                    progressText.setText(download.getDownloadState().name());
                    pb.setIndeterminate(false);
                    pb.setProgress(download.getProgress());
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
        return cacheKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public class AppViewPager extends FixedFragmentStatePagerAdapter{

        private final String[] TITLES = { getString(R.string.info), getString(R.string.review), getString(R.string.related), getString(R.string.advanced)};

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
            return TITLES.length;
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

        public List<String> getScreenshots(){

            if(details.getScreenshotsHd()!=null){

                ArrayList<String> screenshotsPath = new ArrayList<String>();

                for(GetApkInfoJson.Media.Screenshots screenshot : details.getScreenshotsHd()){
                    screenshotsPath.add(screenshot.getOrient()+"|"+screenshot.getPath());
                }

                return screenshotsPath;
            }else{
                return details.getScreenshots();
            }
        }

        public ArrayList<MediaObject> getScreenshotsAndThumbVideo(){
            ArrayList<MediaObject> imagesPath = new ArrayList<MediaObject>();

            if(details.getVideos()!=null){
                for(GetApkInfoJson.Media.Videos video : details.getVideos()){
                    imagesPath.add(new Video(video.getThumb(), video.getUrl()));
                    Log.d("AppView", "media objects [thumb]: " + video.getThumb());
                    Log.d("AppView", "media objects [url]: " + video.getUrl());
                }
            }



            if(details.getScreenshotsHd()!=null){
                for(GetApkInfoJson.Media.Screenshots screenshot : details.getScreenshotsHd()){
                    imagesPath.add(new Screenshot(screenshot.getPath(), screenshot.getOrient(), true));
                }
            }else if (details.getScreenshots() != null){
                for(String screenshot : details.getScreenshots()){
                    imagesPath.add(new Screenshot(screenshot, "portrait", false));
                }
            }

            return imagesPath;
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

        public float getRating() {
            return details.rating;
        }

        public String getStore(){
            return details.getStore();
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
        public float rating;
        private List<GetApkInfoJson.Media.Screenshots> screenshotsHd;
        private List<GetApkInfoJson.Media.Videos> videos;

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

        public void setRating(float rating) {
            this.rating = rating;
        }

        public float getRating() {
            return rating;
        }

        public void setScreenshotsHd(List<GetApkInfoJson.Media.Screenshots> screenshotsHd) { this.screenshotsHd = screenshotsHd; }

        public List<GetApkInfoJson.Media.Screenshots> getScreenshotsHd() {
            return screenshotsHd;
        }

        public List<GetApkInfoJson.Media.Videos> getVideos() { return videos; }

        public void setVideos(List<GetApkInfoJson.Media.Videos> videos) { this.videos = videos; }
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
        } else if(requestCode == DOWGRADE_REQUEST_CODE) {

            Log.d("Downgrade", "OnactivityResult");
            try {
                getPackageManager().getPackageInfo(package_name, 0);

                Toast.makeText(this, getString(R.string.downgrade_requires_uninstall), Toast.LENGTH_SHORT).show();

            } catch (PackageManager.NameNotFoundException e) {
                isFromActivityResult = true;
                spiceManager.getFromCache(GetApkInfoJson.class, cacheKey, DurationInMillis.ONE_HOUR, requestListener);
            }
        }
    }


    /*private void show(boolean shown, boolean animate) {

        View mListContainer = findViewById(R.id.pager_host);
        View mProgressContainer = findViewById(R.id.progressBar);
        if (shown) {
            isShown = true;

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

    private void showError(boolean shown, boolean animate) {

        View mButtonContainer = findViewById(R.id.repeat_request);
        mButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShown = false;
                showError(false, true);
                continueLoading(null);

            }
        });
        View mProgressContainer = findViewById(R.id.progressBar);
        if (shown) {
            isShown = true;

            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
                mButtonContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mButtonContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mButtonContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
                mButtonContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mButtonContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mButtonContainer.setVisibility(View.GONE);
        }
    }*/


}
