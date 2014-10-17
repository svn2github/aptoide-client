package cm.aptoide.ptdev;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.flurry.android.FlurryAgent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.Data;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cm.aptoide.ptdev.SpiceStuff.AlmostGenericResponseV2RequestListener;
import cm.aptoide.ptdev.ads.AptoideAdNetworks;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.CanDownloadDialog;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;
import cm.aptoide.ptdev.downloadmanager.state.EnumState;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.events.OnMultiVersionClick;
import cm.aptoide.ptdev.fragments.FragmentAppView;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentCallback;
import cm.aptoide.ptdev.fragments.callbacks.AddCommentVoteCallback;
import cm.aptoide.ptdev.fragments.callbacks.ApkFlagCallback;
import cm.aptoide.ptdev.fragments.callbacks.SuccessfullyPostCallback;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.model.MediaObject;
import cm.aptoide.ptdev.model.MultiStoreItem;
import cm.aptoide.ptdev.model.Screenshot;
import cm.aptoide.ptdev.model.Video;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.AddApkCommentVoteRequest;
import cm.aptoide.ptdev.webservices.AddApkFlagRequest;
import cm.aptoide.ptdev.webservices.AddCommentRequest;
import cm.aptoide.ptdev.webservices.GetAdsRequest;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromId;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromMd5;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromPackageName;
import cm.aptoide.ptdev.webservices.GetApkInfoRequestFromVercode;
import cm.aptoide.ptdev.webservices.UpdateUserRequest;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;
import cm.aptoide.ptdev.webservices.json.CreateUserJson;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import roboguice.util.temp.Ln;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 15-11-2013
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class AppViewActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MyAppsAddStoreInterface, ApkFlagCallback, AddCommentCallback, AddCommentVoteCallback {

    private static final short Purchase_REQUEST_CODE = 30333;
    private static final short LOGIN_REQUEST_CODE = 123;
    public static final short DOWGRADE_REQUEST_CODE = 456;

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private View advertising, publicityView, customAdBannerView, urlAdBannerView;

    private GetApkInfoJson json;

    private String installedSignature;
    private String signature;
    private String name;
    private boolean isFromActivityResult;
    private String wUrl;
    private String md5;
    private boolean isInstalled;
    private boolean autoDownload;
    private boolean isDownloadCompleted;
    private ReentrantLock lock = new ReentrantLock();
    private Condition boundCondition = lock.newCondition();
    private boolean refreshOnResume;
    private String altPath;
    private boolean paused = false;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();
    private GetApkInfoJson.Payment payment;
    private boolean isPaidApp;
    private boolean isPaidToschedule;
    private String referrer;


    public GetApkInfoJson.Malware.Reason getReason() {
        return reason;
    }
    boolean showBadgeLayout = false;

    private static GetApkInfoJson.Malware.Reason reason;
    private boolean isSponsored = false;
    private RequestListener<GetApkInfoJson> requestListener = new RequestListener<GetApkInfoJson>() {

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d( "networkerror", e.getLocalizedMessage() );
            if(AppViewActivity.this.json==null){
                if(!paused){
                    AptoideDialog.errorDialog().show(getSupportFragmentManager(), "errorDialog");
                }
            };
        }

        @Override
        public void onRequestSuccess(final GetApkInfoJson getApkInfoJson) {

            //Log.d("Aptoide-AppView", "Json is null? " + String.valueOf(getApkInfoJson == null));
            if (getApkInfoJson != null) {

                AppViewActivity.this.json = getApkInfoJson;
                if ("OK".equals(json.getStatus())) {
                    GetApkInfoJson.Signature s = getApkInfoJson.getSignature();
                    if(s!=null){
                        signature = s.getSHA1().replace(":","");
                    }
                    final GetApkInfoJson.Apk apk = getApkInfoJson.getApk();
                    altPath = apk.getAltPath();
                    name = getApkInfoJson.getMeta().getTitle();
                    versionName = apk.getVername();
                    downloads = getApkInfoJson.getMeta().getDownloads();
                    package_name = apk.getPackage();
                    repoName = apk.getRepo();
                    wUrl = getApkInfoJson.getMeta().getWUrl();
                    screen = apk.getMinScreen();
                    minSdk = apk.getMinSdk().intValue();
                    payment= getApkInfoJson.getPayment();



                    boolean showLatestString = false;
                    if (apk.getIconHd() != null) {
                        icon = apk.getIconHd();
                        String sizeString = IconSizes.generateSizeString(AppViewActivity.this);
                        String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                        icon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                    } else {
                        icon = apk.getIcon();
                    }
                    appName.setText(name);
                    appVersionName.setText(versionName);
                    ImageLoader.getInstance().displayImage(icon, appIcon);

                    checkInstallation(getApkInfoJson);

                    if(signature!=null && !signature.equals(installedSignature) && installedSignature!=null && installedSignature.length()>0){
                        message.setVisibility(View.VISIBLE);
                    }else{
                        message.setVisibility(View.GONE);
                    }
//                        AppMsg.makeText(AppViewActivity.this, getString(R.string.row_app_update_not_safe), new AppMsg.Style(5000, android.R.color.darker_gray)).show();

                    downloadId = json.getApk().getMd5sum().hashCode();


                    findViewById(R.id.ic_action_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (service != null) {
                                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Canceled_Download");
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

                        //Log.d("AppViewActivity-malwareStatus", "status: " + (getApkInfoJson.getMalware().getStatus()));
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

                        reason = getApkInfoJson.getMalware().getReason();


                        if(showBadgeLayout || Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {
                            findViewById(R.id.extra_info_layout).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(R.id.extra_info_layout).setVisibility(View.GONE);
                        }


                        if (showBadgeLayout) {
                            View badge_layout = findViewById(R.id.badge_layout);
                            badge_layout.setVisibility(View.VISIBLE);
                            badge_layout.startAnimation(AnimationUtils.loadAnimation(AppViewActivity.this, android.R.anim.fade_in));
                            badge_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Badge_Dialog");
                                    AptoideDialog.badgeDialog(name, getApkInfoJson.getMalware().getStatus()).show(getSupportFragmentManager(), "badgeDialog");

                                }
                            });
                        }
                    }


                    versionCode = json.getApk().getVercode().intValue();

                    if(!isSponsored){
                        loadGetLatest(showLatestString);
                    }

                    if (getIntent().getBooleanExtra("fromMyapp", false)) {
                        getIntent().removeExtra("fromMyapp");
                        AptoideDialog.myappInstall(name).show(getSupportFragmentManager(), "myApp");
                    }
                    md5 = json.getApk().getMd5sum();
                    publishEvents();
                    //BusProvider.getInstance().post(new RelatedEvent());
                    supportInvalidateOptionsMenu();
                    //if (!isShown) show(true, true);

                    if (isFromActivityResult || autoDownload) {
                        Download download = new Download();

                        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).contains("allowRoot") && !Aptoide.IS_SYSTEM) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isRooted()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(!paused){
                                                    AptoideDialog.allowRootDialog().show(getSupportFragmentManager(), "allowRoot");
                                                }
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }



                        download.setId(downloadId);
                        download.setName(name);
                        download.setVersion(versionName);
                        download.setIcon(icon);
                        download.setPackageName(package_name);
                        download.setMd5(md5);

                        if(!isUpdate){
                            download.setCpiUrl(getIntent().getStringExtra("cpi"));
                        }
                        download.setReferrer(referrer);
                        try {
                            waitForServiceToBeBound();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        service.startDownloadFromJson(json, downloadId, download);

                        isFromActivityResult = false;
                        autoDownload = false;

                    }


                    if (service != null && service.getDownload(downloadId).getDownload() != null) {
                        onDownloadUpdate(service.getDownload(downloadId).getDownload());
                        if(service.getDownload(downloadId).getDownload().getDownloadState() == EnumState.COMPLETE){
                            isDownloadCompleted = true;
                            supportInvalidateOptionsMenu();
                        }
                    } else {
                        findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                        findViewById(R.id.download_progress).setVisibility(View.GONE);
                        findViewById(R.id.btinstall).setVisibility(View.VISIBLE);

                        if(showBadgeLayout){
                            findViewById(R.id.badge_layout).setVisibility(View.VISIBLE);
                        }

                    }

                    // Check if Download is already completed to disable schedule
                } else {
                    AptoideUtils.toastError(json.getErrors());
                    finish();
                }
            }
        }
    };

    public void loadGetLatest(boolean showLatestString) {
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
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Get_Latest");
                    String url = json.getLatest();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    url = url.replaceAll(" ", "%20");
                    i.setData(Uri.parse(url));
                    try{
                        startActivity(i);
                    }catch (ActivityNotFoundException ignored){

                    }
                }
            });
        } else {
            latestVersion.setVisibility(View.GONE);
        }
    }

    protected void waitForServiceToBeBound() throws InterruptedException {
        lock.lock();
        try {
            while (service == null) {
                boundCondition.await();
            }
            Ln.d("Bound ok.");
        } finally {
            lock.unlock();
        }
    }

    private PackageInfo getPackageInfo(String package_name){
        try {
            return getPackageManager().getPackageInfo(package_name, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void changebtInstalltoOpen(TextView btinstall){

        final Intent i = getPackageManager().getLaunchIntentForPackage(package_name);

        btinstall.setText(getString(R.string.open));

        if (i != null) {
            btinstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            });
        } else {
            btinstall.setEnabled(false);
        }
    }

    private void changebtInstalltoBuy(TextView btinstall){
        supportInvalidateOptionsMenu();
        btinstall.setText(getString(R.string.buy) + " (" +payment.getSymbol() + " " + payment.getAmount() + ")");
        btinstall.setEnabled(true);
        final Activity thisActivity = this;
        btinstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Buy_Button");
                final AccountManager accountManager = AccountManager.get(thisActivity);
                final Account[] accounts = accountManager.getAccountsByType(Aptoide.getConfiguration().getAccountType());

                Intent i = new Intent(thisActivity, Aptoide.getConfiguration().getPaidAppPurchaseActivityClass());

                i.putExtra("packageName", package_name);
                i.putExtra("ID", payment.getMetadata().getId());
                if(accounts.length>0) {

                    i.putExtra("user", accounts[0].name);
                    i.putParcelableArrayListExtra("PaymentServices", new ArrayList<Parcelable>(payment.getPayment_services()));
                    thisActivity.startActivityForResult(i, Purchase_REQUEST_CODE);

                } else {

                    Toast.makeText(Aptoide.getContext(), "You need to login to purchase.", Toast.LENGTH_LONG).show();

                    accountManager.addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, thisActivity, new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {

                            refreshOnResume = true;

                        }
                    }, new Handler(Looper.getMainLooper()));
                }


            }
        });
    }

    private void Updateapp_version_installed(String versionName){
        TextView app_version_installed = (TextView) findViewById(R.id.app_version_installed);
        app_version_installed.setVisibility(View.VISIBLE);
        app_version_installed.setText(getString(R.string.installed_tab) + ": " + versionName);
    }

    private void checkInstallation(GetApkInfoJson getApkInfoJson) {
        final TextView btinstall = (TextView) findViewById(R.id.btinstall);
        btinstall.setEnabled(true);
        boolean setShareButtonVisible=Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false);

        if ( payment.getAmount().doubleValue() > 0) {
            String path = getApkInfoJson.getApk().getPath();

            if (!TextUtils.isEmpty(path)) {
                isPaidToschedule=true;
                btinstall.setText(getString(R.string.install));

                InstallListener installListener = new InstallListener(icon, name, versionName, package_name, md5);

                installListener.setPaid(true);
                btinstall.setOnClickListener(installListener);
            } else {
                changebtInstalltoBuy(btinstall);
            }

        } else {
            PackageInfo info = getPackageInfo(package_name);
            if (info == null) {
                btinstall.setText(getString(R.string.install));
                btinstall.setOnClickListener(new InstallListener(icon, name, versionName, package_name, md5));
            } else {
                isInstalled = true;
                try {
                    installedSignature = AptoideUtils.Algorithms.computeSHA1sumFromBytes(info.signatures[0].toByteArray()).toUpperCase(Locale.ENGLISH);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (getApkInfoJson.getApk().getVercode().intValue() > info.versionCode) {
                    isUpdate = true;
                    btinstall.setText(getString(R.string.update));
                    btinstall.setEnabled(true);
                    btinstall.setOnClickListener(new InstallListener(icon, name, versionName, package_name, md5));
                    Updateapp_version_installed(info.versionName);
                } else if (getApkInfoJson.getApk().getVercode().intValue() < info.versionCode) {
                    btinstall.setText(getString(R.string.downgrade));
                    btinstall.setOnClickListener(new DowngradeListener(icon, name, info.versionName, versionName, info.packageName));
                    Updateapp_version_installed(info.versionName);
                } else {
                    changebtInstalltoOpen(btinstall);
                }
                supportInvalidateOptionsMenu();
            }
        }

        final CheckBox btinstallshare = (CheckBox) findViewById(R.id.btinstallshare);
        btinstallshare.setVisibility(setShareButtonVisible ? View.VISIBLE : View.GONE);
        btinstallshare.setChecked(Preferences.getBoolean(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, true));
        btinstallshare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Preferences.putBooleanAndCommit(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, isChecked);
            }
        });

    }
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_share_install) {

                    menuItem.setChecked(!menuItem.isChecked());
                    Preferences.putBooleanAndCommit(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, menuItem.isChecked());
                    return true;
                }
                return false;
            }
        });
        inflater.inflate(R.menu.menu_share_install, popup.getMenu());

        popup.getMenu().findItem(R.id.menu_share_install).setChecked(
                Preferences.getBoolean(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, true));
        popup.show();
    }

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
                Intent i = new Intent(AppViewActivity.this, startClass);
                i.putExtra("nodialog", true);
                i.putExtra("newrepo", repo);
                i.addFlags(12345);
                startActivity(i);

            }
        };
    }

    private Class startClass = Aptoide.getConfiguration().getStartActivityClass();

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public DialogInterface.OnClickListener getMyAppListener() {
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Opened_From_My_App");
        return new InstallListener(icon, name, versionName, package_name, md5);
    }

    private ImageView appIcon;
    private TextView appName;
    private TextView appVersionName;
    private TextView latestVersion;
    private TextView message;

    private long id;
    private int downloads;
    private String repoName;
    private int minSdk;
    private DownloadService service;
    private String icon;
    private boolean isUpdate;
    private int versionCode;
    //private boolean isShown = false;
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

    public void updateUsername(final String username) {

        UpdateUserRequest request = new UpdateUserRequest(this);
        request.setName(username);

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        spiceManager.execute(request, new RequestListener<CreateUserJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (fragment != null) fragment.dismiss();
            }

            @Override
            public void onRequestSuccess(CreateUserJson createUserJson) {
                DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if (fragment != null) fragment.dismiss();

                if (createUserJson.getStatus().equals("OK")) {
                    Toast.makeText(Aptoide.getContext(), R.string.username_success, Toast.LENGTH_LONG).show();
                    PreferenceManager.getDefaultSharedPreferences(AppViewActivity.this).edit().putString("username", username).commit();
                } else {
                    AptoideUtils.toastError(createUserJson.getErrors());
                }

            }
        });
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;

        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (String where : places) {
            if (new File(where + binaryName).exists()) {
                found = true;
                break;
            }
        }

        return found;
    }

    public DownloadService getService(){
        return service;
    }

    public class InstallListener implements View.OnClickListener, DialogInterface.OnClickListener {
        protected String icon;
        protected String name;
        protected String versionName;
        protected String package_name;
        protected String md5;
        private boolean paid;

        public InstallListener(String icon, String name, String versionName, String package_name, String md5) {
            this.icon = icon;
            this.name = name;
            this.versionName = versionName;
            this.package_name = package_name;
            this.md5 = md5;
        }
        protected Download makeDownLoad(){
            Download download = new Download();

            if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).contains("allowRoot") && !Aptoide.IS_SYSTEM) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isRooted()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AptoideDialog.allowRootDialog().show(getSupportFragmentManager(), "allowRoot");
                                }
                            });
                        }
                    }
                }).start();
            }

            download.setId(downloadId);
            download.setName(this.name);
            download.setVersion(this.versionName);
            download.setIcon(this.icon);
            download.setPackageName(this.package_name);
            download.setMd5(this.md5);
            if (!isUpdate) download.setCpiUrl(getIntent().getStringExtra("cpi"));
            download.setReferrer(referrer);

            return download;
        }
        protected Bundle makeBundleForDialog(){
            Bundle bundle = new Bundle();
            bundle.putLong("downloadId", downloadId);
            bundle.putSerializable("download", makeDownLoad());
            bundle.putString("md5", md5);
            bundle.putString("repoName",repoName);
            bundle.putString("Package_Name", package_name);
            bundle.putString("Version_Name", versionName);
            bundle.putString("Name", name);
            bundle.putString("Icon", icon);
            return bundle;
        }
        protected void FlurryIt(String s){
            if(Build.VERSION.SDK_INT >= 10) {
                Map<String, String> installParams = new HashMap<String, String>();
                installParams.put("Package_Name", package_name);
                installParams.put("Name", name);
                installParams.put("Version_Name", versionName);
                FlurryAgent.logEvent(s, installParams);
            }
        }
        protected boolean CanDownload(){
            return AptoideUtils.NetworkUtils.isGeneral_DownloadPermitted(Aptoide.getContext());
        }
        @Override
        public void onClick(View v) {

            Download download = makeDownLoad();
            download.setPaid(paid);

            if (service != null && json!=null) {
                if(!CanDownload()){
                    CanDownloadDialog dialog = new CanDownloadDialog(json);
                    dialog.setArguments(makeBundleForDialog());
                    dialog.show(getSupportFragmentManager(),null);
                    return;
                }
                service.startDownloadFromJson(json, downloadId, download);
                FlurryIt("App_View_Clicked_On_Install_Button");
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            onClick(null);
        }

        public void setPaid(boolean paid) {
            this.paid = paid;
        }
    }

    public class InstallFromUrlListener extends InstallListener {
        private final String url;
        private String repoName;

        public InstallFromUrlListener(String icon, String name, String versionName, String package_name, String md5, String url, String repoName) {
            super(icon,name,versionName,package_name,md5);
            this.url = url;
            this.repoName = repoName;
        }

        @Override
        public void onClick(View v) {
            Download download = new Download();

            if(!CanDownload()){
                CanDownloadDialog dialog = new CanDownloadDialog();
                Bundle bundle = makeBundleForDialog();
                bundle.putString("url", url);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(),null);
                return;
            }

            download.setId(downloadId);
            download.setName(this.name);
            download.setVersion(this.versionName);
            download.setIcon(this.icon);
            download.setPackageName(this.package_name);
            download.setMd5(this.md5);


            service.startDownloadFromUrl(url, md5, downloadId, download, repoName);
            FlurryIt("Clicked_On_Install_Button");
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.starting_download), Toast.LENGTH_LONG).show();
        }
    }

    public class DowngradeListener implements View.OnClickListener {
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
            Fragment downgrade = new UninstallRetainFragment();
            Bundle args = new Bundle(  );
            args.putString( "name", name );
            args.putString( "package", package_name );
            args.putString( "version", versionName );
            args.putString( "downgradeVersion", downgradeVersion );
            args.putString( "icon", icon );
            downgrade.setArguments( args );

            getSupportFragmentManager().beginTransaction().add(downgrade, "downgrade").commit();
            Map<String, String> installParams = new HashMap<String, String>();
            installParams.put("Package_Name", package_name);
            installParams.put("Name", name);
            installParams.put("Version_Name", versionName);
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Clicked_On_Downgrade_Button", installParams);
        }
    }

    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder downloadService) {
            service = ((DownloadService.LocalBinder) downloadService).getService();

            if (service.getDownload(downloadId).getDownload() != null) {
                onDownloadUpdate(service.getDownload(downloadId).getDownload());
            } else {
                findViewById(R.id.btinstall).setVisibility(View.VISIBLE);
                findViewById(R.id.btinstall).startAnimation(AnimationUtils.loadAnimation(AppViewActivity.this, android.R.anim.fade_in));
            }
            lock.lock();
            try {
                boundCondition.signalAll();
            } finally {
                lock.unlock();
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

    public boolean isMultipleStores(){
        return true;
    }

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
        //Log.d("Aptoide-AppView", "PublishingDetails");
        details.setDownloads(downloads);
        details.setStore(repoName);

        if (json != null) {

            if (!json.getStatus().equals("FAIL")) {
                if (json.getMeta().getDeveloper() != null) details.setDeveloper(json.getMeta().getDeveloper());
                if (json.getMeta().getNews() != null) details.setNews(json.getMeta().getNews());
                //Log.d("Aptoide-AppView", "Description: " + json.getMeta().getDescription());
                details.setDescription(json.getMeta().getDescription());

                long size = json.getApk().getSize().longValue();

                if (json.getObb() != null && !Data.isNull(json.getObb())) {
                    size += json.getObb().getMain().getFilesize().longValue();

                    if (json.getObb().getPatch() != null && !Data.isNull(json.getObb().getPatch())) {
                        size += json.getObb().getPatch().getFilesize().longValue();
                    }

                }

                details.setSize(size);

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

                //Log.d("AptoideTAG", package_name + " " + versionName + " " + repoName);

                MultiStoreItem[] items = new Database(Aptoide.getDb()).getOtherReposVersions(id, package_name, versionName, repoName, versionCode);
                details.setOtherVersions(items);
            }
        }

        return new DetailsEvent(details);

    }

    @Override
    protected void onDestroy() {
        if (service != null) unbindService(downloadConnection);
        destroyPublicity();
        super.onDestroy();
    }

    public void destroyPublicity() {
        if (publicityView != null) {
            ((MoPubView) publicityView).destroy();
        }

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
            GetApkInfoJson.Meta.Flags flags = json.getMeta().getFlags();
            if (flags != null){
                event.setFlagVotes(flags.getVotes());
                if (flags.getUservote() != null) {
                    event.setFlagUservote(flags.getUservote());
                }
                if (flags.getVeredict() != null){
                    event.setFlagVeredict(flags.getVeredict());
                }
            }

        }
        return event;

    }

    private void publishEvents() {
        //Log.d("Aptoide-AppViewActivity", "Publishing revents");
        BusProvider.getInstance().post(publishDetails());
        BusProvider.getInstance().post(publishSpecs());
        BusProvider.getInstance().post(publishRating());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("cacheKey", cacheKey);
        outState.putString("packageName", package_name);
        outState.putInt("downloadId", downloadId);
        outState.putInt("downloads", downloads);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");

    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;

        if(!spiceManager.isStarted()){
            spiceManager.start(this);
        }

        spiceManager.addListenerIfPending(GetApkInfoJson.class, cacheKey, requestListener);


        BusProvider.getInstance().register(this);
        if (json != null) {
            checkInstallation(json);
        }


        if (service != null && service.getDownload(downloadId).getDownload() != null) {
            onDownloadUpdate(service.getDownload(downloadId).getDownload());
        } else {
            View v=findViewById(R.id.btinstall);
            v.setVisibility(View.VISIBLE);
            v.startAnimation(AnimationUtils.loadAnimation(AppViewActivity.this, android.R.anim.fade_in));
        }


        if(refreshOnResume) {
            spiceManager.removeDataFromCache(GetApkInfoJson.class, getCacheKey());
            onRefresh( null );
            refreshOnResume = false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onPause() {
        paused = true;
        if(spiceManager.isStarted()){
            spiceManager.shouldStop();
        }
        try{
            BusProvider.getInstance().unregister(this);
        }catch (IllegalArgumentException e){
            //no it is not
        }
        super.onPause();
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_view);

        //SearchManager manager;

        if (savedInstanceState != null) {
            package_name = savedInstanceState.getString("packageName");
            downloadId = savedInstanceState.getInt("downloadId");
            cacheKey = savedInstanceState.getString("cacheKey");
            downloads = savedInstanceState.getInt("downloads");
        }else{
/*            new Thread(new Runnable() {
                @Override
                public void run() {
                    DateTime time = new DateTime();
                    Hours.hoursBetween(time, time);
                }
            }).start();*/
            package_name = getIntent().getStringExtra("packageName");
            repoName = getIntent().getStringExtra("repoName");


            String downloadFrom = getIntent().getStringExtra("download_from");
            if(downloadFrom!=null){
                Map<String, String> downloadParams = new HashMap<String, String>();
                downloadParams.put("App_Opened_From", downloadFrom);
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Opened_From", downloadParams);
            }

        }

        continueLoading(savedInstanceState);

    }

    public void continueLoading(Bundle savedInstanceState) {
        message = (TextView) findViewById(R.id.message);
        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name);
        appVersionName = (TextView) findViewById(R.id.app_version);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        id = getIntent().getExtras().getLong("id");


        if (getIntent().getExtras().containsKey("appName")) {
            name = getIntent().getExtras().getString("appName");
            appName.setText(name);
        }

        if (getIntent().getExtras().containsKey("versionName")) {
            versionName = getIntent().getExtras().getString("versionName");
            appVersionName.setText(versionName);
        }

        if (pager != null) {
            PagerAdapter adapter = new AppViewPager(getSupportFragmentManager(), this);
            pager.setAdapter(adapter);
            PagerSlidingTabStrip slidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            slidingTabStrip.setViewPager(pager);
        }

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.applications);

        if(savedInstanceState==null) {

            if(getIntent().getBooleanExtra("fromSponsored", false)){
                GetApkInfoRequestFromPackageName request = new GetApkInfoRequestFromPackageName(getApplicationContext());
                long id = getIntent().getLongExtra("id", 0);

                String repo = getIntent().getStringExtra("repoName");
                package_name = getIntent().getStringExtra("packageName");
                request.setRepoName(repo);

                request.setPackageName(package_name);

                cacheKey = "sponsored - " + String.valueOf(id);

                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

                isSponsored = true;

                if(getIntent().getBooleanExtra("autoinstall", false)){
                    autoDownload = true;
                }


                final ExecutorService executorService = Executors.newSingleThreadExecutor();

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        if(getIntent().hasExtra("partnerExtra")){

                            try {

                                String clickUrl = getIntent().getBundleExtra("partnerExtra").getString("partnerClickUrl");
                                Log.d("Aptoide", "InSponsoredExtras");
                                clickUrl = AptoideAdNetworks.parseAppiaString(Aptoide.getContext(), clickUrl);
                                HttpResponse response;
                                boolean repeat;
                                do  {
                                    repeat = false;
                                    GenericUrl url = new GenericUrl(clickUrl);
                                    HttpRequest httpRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildPostRequest(url, null).setFollowRedirects(false).setSuppressUserAgentSuffix(true);

                                    try{
                                        response = httpRequest.execute();
                                    } catch (HttpResponseException exception){

                                        if(exception.getStatusCode() == 302) {
                                            repeat = true;
                                            final String location = exception.getHeaders().getLocation();
                                            Log.d("CENAAS", "Response was: " + exception.getStatusCode());

                                            if (!TextUtils.isEmpty(location)) {
                                                clickUrl = location;
                                                if (clickUrl.contains("referrer=") && (clickUrl.startsWith("market://") || clickUrl.startsWith("https://play.google.com"))) {
                                                    referrer = getReferrer(clickUrl);
                                                }
                                            } else {
                                                Log.d("CENAAS", "Cenas was epmty");
                                            }
                                        }else if(exception.getStatusCode() == HttpStatusCodes.STATUS_CODE_OK && exception.getHeaders().containsKey("Refresh")){



                                        }

                                    }



                                } while ( repeat );




                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });




                try {
                    GenericUrl url = new GenericUrl(getIntent().getStringExtra("cpc"));
                    AndroidHttp.newCompatibleTransport().createRequestFactory().buildGetRequest(url).setSuppressUserAgentSuffix(true).executeAsync(executorService);
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }else if (getIntent().getBooleanExtra("getBackupApps", false)) {

                GetApkInfoRequestFromPackageName request = new GetApkInfoRequestFromPackageName(getApplicationContext());


                request.setPackageName("pt.aptoide.backupapps");

                if (token != null) {
                    request.setToken(token);
                }

                cacheKey = "pt.aptoide.backupapps";

                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

            } else if (getIntent().getBooleanExtra("fromRollback", false)) {

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
            } else if (getIntent().getBooleanExtra("fromApkInstaller", false)) {

                GetApkInfoRequestFromId request = new GetApkInfoRequestFromId(getApplicationContext());

                long id = getIntent().getLongExtra("id", 0);
                request.setAppId(String.valueOf(id));

                if (token != null) {
                    request.setToken(token);
                }
                cacheKey = String.valueOf(id);
                autoDownload = true;
                spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

            } else {
                getSupportLoaderManager().initLoader(50, getIntent().getExtras(), this);

            }
        }

        if(!getIntent().getBooleanExtra("fromSponsored", false)){
            loadPublicity();
        }

        bindService(new Intent(AppViewActivity.this, DownloadService.class), downloadConnection, BIND_AUTO_CREATE);

    }



    public String getReferrer(String uri){
        List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uri), "UTF-8");

        String referrer = null;
        for (NameValuePair param : params) {

            if(param.getName().equals("referrer")){
                referrer = param.getValue();
            }

            System.out.println(param.getName() + " : " + param.getValue());
        }
        return referrer;
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void loadPublicity() {
//        Log.d("AppViewActivity", "loadPublicity");

        advertising = findViewById(R.id.advertisement);
        advertising.setVisibility(View.VISIBLE);

        publicityView = findViewById(R.id.adview);
        customAdBannerView = findViewById(R.id.custom_ad_banner);
        urlAdBannerView = findViewById(R.id.url_ad_banner);

        if (Build.VERSION.SDK_INT > 11) {
            publicityView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        final GetAdsRequest getAdsRequest = new GetAdsRequest(AppViewActivity.this);

        getAdsRequest.setLocation("appview");
        getAdsRequest.setKeyword("__NULL__");
        getAdsRequest.setRepo(repoName);
        getAdsRequest.setPackage_name(package_name);
        getAdsRequest.setLimit(1);
        getAdsRequest.setTimeout(2000);

        spiceManager.execute(getAdsRequest, new RequestListener<ApkSuggestionJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                loadMoPub();
            }

            @Override
            public void onRequestSuccess(ApkSuggestionJson apkSuggestionJson) {

                if (apkSuggestionJson!=null && apkSuggestionJson.getAds()!=null && apkSuggestionJson.getAds().size()==0) {
                    loadMoPub();
                } else if(apkSuggestionJson != null ) {

                    HashMap<String, String> adTypeArgs = new HashMap<String, String>();

                    final ApkSuggestionJson.Ads appSuggested = (ApkSuggestionJson.Ads) apkSuggestionJson.getAds().get(0);

                    if (appSuggested.getInfo().getAd_type().equals("app:suggested")) {
                        adTypeArgs.put("type", appSuggested.getInfo().getAd_type());
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Load_Publicity", adTypeArgs);

                        customAdBannerView.setVisibility(View.VISIBLE);
                        ImageView iconbackground = (ImageView) customAdBannerView.findViewById(R.id.app_icon_background);
                        ImageView icon = (ImageView) customAdBannerView.findViewById(R.id.app_icon);
                        TextView name = (TextView) customAdBannerView.findViewById(R.id.app_name);
                        RatingBar rating = (RatingBar) customAdBannerView.findViewById(R.id.app_rating);

                        name.setText(appSuggested.getData().getName());
                        ImageLoader.getInstance().displayImage(appSuggested.getData().getIcon(), iconbackground);
                        ImageLoader.getInstance().displayImage(appSuggested.getData().getIcon(), icon);
                        rating.setRating(appSuggested.getData().getStars().floatValue());
                        customAdBannerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Home_Page_Clicked_On_Sponsored_App");
                                Intent i = new Intent(AppViewActivity.this, appViewClass);
                                long id = appSuggested.getData().getId().longValue();
                                i.putExtra("id", id);
                                i.putExtra("packageName", appSuggested.getData().getPackageName());
                                i.putExtra("repoName", appSuggested.getData().getRepo());
                                i.putExtra("fromSponsored", true);
                                i.putExtra("location", "homepage");
                                i.putExtra("keyword", "__NULL__");
                                i.putExtra("cpc", appSuggested.getInfo().getCpc_url());
                                i.putExtra("cpi", appSuggested.getInfo().getCpi_url());
                                i.putExtra("whereFrom", "sponsored");
                                i.putExtra("download_from", "sponsored");


                                if(appSuggested.getPartner() != null){
                                    Bundle bundle = new Bundle();

                                    bundle.putString("partnerType", appSuggested.getPartner().getPartnerInfo().getName());
                                    bundle.putString("partnerClickUrl", appSuggested.getPartner().getPartnerData().getClick_url());

                                    i.putExtra("partnerExtra", bundle);
                                }


                                startActivity(i);
                            }
                        });


                    } else if (appSuggested.getInfo().getAd_type().equals("url:googleplay")) {
                        adTypeArgs.put("type", appSuggested.getInfo().getAd_type());
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Load_Publicity", adTypeArgs);

                        urlAdBannerView.setVisibility(View.VISIBLE);
                        ImageView banner = (ImageView) urlAdBannerView.findViewById(R.id.app_ad_banner);
                        ImageLoader.getInstance().displayImage(appSuggested.getData().getImage(), banner);
                        urlAdBannerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Clicked_On_Sponsored_Google_Play_Link");
                                try {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(appSuggested.getData().getUrl()));
                                    List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(i, 0);
                                    String activityToOpen = "";
                                    for (ResolveInfo resolveInfo : resolveInfos) {
                                        if (resolveInfo.activityInfo.packageName.equals("com.android.vending")) {
                                            activityToOpen = resolveInfo.activityInfo.name;
                                        }
                                    }
                                    i.setClassName("com.android.vending", activityToOpen);



                                    startActivity(i);
                                }catch(ActivityNotFoundException e){
                                    e.printStackTrace();

                                    Intent i = new Intent(getApplicationContext(), Aptoide.getConfiguration().getSearchActivityClass());
                                    String param = appSuggested.getData().getUrl().split("=")[1];
                                    i.putExtra(android.app.SearchManager.QUERY, param);
                                    startActivity(i);
                                }

                                try {
                                    GenericUrl url = new GenericUrl(appSuggested.getInfo().getCpc_url());
                                    AndroidHttp.newCompatibleTransport().createRequestFactory().buildGetRequest(url).setSuppressUserAgentSuffix(true).executeAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });



                    } else if (appSuggested.getInfo().getAd_type().equals("url:banner")) {
                        adTypeArgs.put("type", appSuggested.getInfo().getAd_type());
                        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Load_Publicity", adTypeArgs);

                        urlAdBannerView.setVisibility(View.VISIBLE);
                        ImageView banner = (ImageView) urlAdBannerView.findViewById(R.id.app_ad_banner);
                        ImageLoader.getInstance().displayImage(appSuggested.getData().getImage(), banner);

                        urlAdBannerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Clicked_On_Sponsored_Banner_Link");
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appSuggested.getData().getUrl()));

                                try {
                                    GenericUrl url = new GenericUrl(appSuggested.getInfo().getCpc_url());
                                    AndroidHttp.newCompatibleTransport().createRequestFactory().buildGetRequest(url).setSuppressUserAgentSuffix(true).executeAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                startActivity(intent);
                            }
                        });
                    }

                }
            }
        });
    }

    private void loadMoPub() {
        publicityView.setVisibility(View.VISIBLE);
        ((MoPubView) publicityView).setAdUnitId("85aa542ded4e49f79bc6a1db8563ca66");
        ((MoPubView) publicityView).loadAd();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", "mopub");
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Load_Publicity", map);

        ((MoPubView) publicityView).setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Loaded_MoPub_Ad");
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Failed_MoPub_Ad");
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("AppView_Clicked_MoPub_Ad");
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {}

            @Override
            public void onBannerCollapsed(MoPubView banner) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_view, menu);

        if (isInstalled) {
            menu.findItem(R.id.menu_uninstall).setVisible(true);

            if(!isUpdate ) {
                //Log.d( "schedule_download", "isInstalled && !isUpdate" );
                menu.findItem( R.id.menu_schedule ).setVisible( false );
            }

        } else if(isDownloadCompleted || !isPaidToschedule) {
            //Log.d( "schedule_download", "isDownloadCompleted" );
            menu.findItem(R.id.menu_schedule).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home || i == R.id.home) {
            finish();
        } else if (i == R.id.menu_schedule) {
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Schedule_Download_Button");

            new Database(Aptoide.getDb()).scheduledDownloadIfMd5(package_name, md5, versionName, repoName, name, icon);

        } else if (i == R.id.menu_uninstall) {
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Uninstall_Button");

            Fragment uninstallFragment = new UninstallRetainFragment();
            Bundle args = new Bundle(  );
            args.putString("name", name);
            args.putString( "package", package_name );
            args.putString( "version", versionName );
            args.putString( "icon", icon );
            uninstallFragment.setArguments( args );
            supportInvalidateOptionsMenu();
            getSupportFragmentManager().beginTransaction().add(uninstallFragment, "uninstallFrag").commit();

        } else if (i == R.id.menu_search_other) {
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Search_Other_Market_Button");
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
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Share_Button");

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.install) + " \"" + name + "\"");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, wUrl);

            if (wUrl != null) {
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
            }
        } else if( i == R.id.menu_SendFeedBack){
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("App_View_Clicked_On_Send_Feedback_Button");

            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
        }else if(i== R.id.menu_share_install){
            if (item.isChecked()) item.setChecked(false);
            else item.setChecked(true);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, final Bundle bundle) {
        return new SimpleCursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                long id = bundle.getLong("id");
                //Log.d("Aptoide-AppView", "getapk id: " + id);

                return new Database(Aptoide.getDb()).getApkInfo(id);
            }
        };
    }



    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor apkCursor) {

        if (apkCursor.getCount() > 0) {
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
            isPaidApp = apkCursor.getInt(apkCursor.getColumnIndex("price"))>0;
            isPaidToschedule = !isPaidApp;
            //float rating = apkCursor.getFloat(apkCursor.getColumnIndex(Schema.Apk.COLUMN_RATING));

            appName.setText(Html.fromHtml(name).toString());
            appVersionName.setText(Html.fromHtml(versionName).toString());
//        ratingBar.setRating(rating);
            String sizeString = IconSizes.generateSizeString(this);
            if (localIcon.contains("_icon")) {
                String[] splittedUrl = localIcon.split("\\.(?=[^\\.]+$)");
                localIcon = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
            }

            ImageLoader.getInstance().displayImage(icon = iconpath + localIcon, appIcon);
            downloadId = md5.hashCode();

            GetApkInfoRequestFromVercode request = new GetApkInfoRequestFromVercode(getApplicationContext());

            request.setRepoName(repoName);
            request.setPackageName(package_name);
            request.setVersionName(versionName);
            request.setVercode(versionCode);

            if (token != null) {
                request.setToken(token);
            }

            cacheKey = package_name + repoName + versionCode;
            BusProvider.getInstance().post(publishDetails());

            PackageInfo info = getPackageInfo(package_name);
            TextView btinstall =(TextView) findViewById(R.id.btinstall);

            if(isPaidApp){
                btinstall.setEnabled(false);
            }

            if (info == null) {
                btinstall.setText(getString(R.string.install));
                btinstall.setOnClickListener(new InstallFromUrlListener(icon, name, versionName, package_name, md5, apkpath + path, repoName));
            } else {
                isInstalled = true;

                if (versionCode > info.versionCode) {
                    isUpdate = true;
                    btinstall.setText(getString(R.string.update));
                    btinstall.setEnabled(true);
                    btinstall.setOnClickListener(new InstallFromUrlListener(icon, name, versionName, package_name, md5, apkpath + path, repoName));
                    Updateapp_version_installed(info.versionName);
                } else if (versionCode < info.versionCode) {
                    btinstall.setText(getString(R.string.downgrade));
                    btinstall.setOnClickListener(new DowngradeListener(icon, name, info.versionName, versionName, info.packageName));
                    Updateapp_version_installed(info.versionName);
                } else {
                    changebtInstalltoOpen(btinstall);
                }
                supportInvalidateOptionsMenu();
            }
            spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);
        } else {
            Toast.makeText(this, R.string.error_occured, Toast.LENGTH_LONG).show();
            finish();
        }

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
    public void onInstalledEvent(InstalledApkEvent event) {
        onRefresh(null);
    }

    @Subscribe
    public void onUnInstalledEvent(UnInstalledApkEvent event) {
        onRefresh(null);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spiceManager.getFromCache(GetApkInfoJson.class, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

    }

    @Subscribe
    public void onRefresh(AppViewRefresh event) {
        GetApkInfoRequestFromVercode request = new GetApkInfoRequestFromVercode(getApplicationContext());

        request.setRepoName(repoName);
        request.setPackageName(package_name);
        request.setVersionName(versionName);
        request.setVercode(versionCode);
        if (token != null) request.setToken(token);

        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

    }

    @Subscribe
    public void onSpinnerItemClick(OnMultiVersionClick event) {

        GetApkInfoRequestFromVercode request = new GetApkInfoRequestFromVercode(getApplicationContext());

        request.setRepoName(event.getRepoName());
        request.setPackageName(event.getPackage_name());
        request.setVersionName(event.getVersionName());
        request.setVercode(event.getVersionCode());
        if (token != null) request.setToken(token);
        cacheKey = event.getPackage_name() + event.getRepoName() + event.getVersionCode();
        //Log.d("OnMultiVersionClick-downloads", "downloads: " + event.getDownloads());
        downloads = event.getDownloads();

        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request, cacheKey, DurationInMillis.ONE_HOUR, requestListener);

    }

    @Subscribe
    public void onDownloadUpdate(Download download) {
        if (download != null && download.getId() == downloadId) {

            TextView progressText = (TextView) findViewById(R.id.progress);
            ProgressBar pb = (ProgressBar) findViewById(R.id.downloading_progress);

            findViewById(R.id.download_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.btinstall).setVisibility(View.GONE);
            findViewById(R.id.badge_layout).setVisibility(View.GONE);
            findViewById(R.id.extra_info_layout).setVisibility(View.GONE);

            switch (download.getDownloadState()) {
                case ACTIVE:
                    findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                    pb.setIndeterminate(false);
                    pb.setProgress(download.getProgress());
                    progressText.setText(download.getProgress() + "% - " + Utils.formatBits((long) download.getSpeed()) + "/s");
                    break;
                case INACTIVE:
                    break;
                case COMPLETE:
                    findViewById(R.id.ic_action_resume).setVisibility(View.GONE);
                    findViewById(R.id.download_progress).setVisibility(View.GONE);
                    findViewById(R.id.btinstall).setVisibility(View.VISIBLE);
                    findViewById(R.id.extra_info_layout).setVisibility(View.VISIBLE);

                    if(showBadgeLayout){
                        findViewById(R.id.badge_layout).setVisibility(View.VISIBLE);
                    }

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

    public String getMd5() {
        return md5;
    }

    public static class AppViewPager extends FixedFragmentStatePagerAdapter {

        private final String[] TITLES;

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        public AppViewPager(FragmentManager fm, Context context) {
            super(fm);
            TITLES = new String[]{context.getString(R.string.info), context.getString(R.string.review), context.getString(R.string.related), context.getString(R.string.advanced)};
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
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

        public List getScreenshots() {
            return screenshots.getScreenshots();
        }
    }

    public static class DetailsEvent {

        Details details;


        public DetailsEvent(Details details) {
            this.details = details;
        }

        public String getDescription() {
            return details.getDescription();
        }

        public String getVersionName() {
            return details.getVersion();
        }

        public String getPublisher() {

            if (details.getDeveloper() != null) {
                return details.getDeveloper().getInfo().getName();
            } else {
                return "N/A";
            }
        }

        public GetApkInfoJson.Meta.Developer getDeveloper() {
            return details.getDeveloper();
        }

        public long getSize() {
            return details.getSize();
        }

        public int getDownloads() {
            return details.getDownloads();
        }

        public List<String> getScreenshots() {

            if (details.getScreenshotsHd() != null) {

                ArrayList<String> screenshotsPath = new ArrayList<String>();

                for (GetApkInfoJson.Media.Screenshots screenshot : details.getScreenshotsHd()) {
                    screenshotsPath.add(screenshot.getOrient() + "|" + screenshot.getPath());
                }

                return screenshotsPath;
            } else {
                return details.getScreenshots();
            }
        }

        public ArrayList<MediaObject> getScreenshotsAndThumbVideo() {
            ArrayList<MediaObject> imagesPath = new ArrayList<MediaObject>();

            if (details.getVideos() != null) {
                for (GetApkInfoJson.Media.Videos video : details.getVideos()) {
                    imagesPath.add(new Video(video.getThumb(), video.getUrl()));
                    //Log.d("AppView", "media objects [thumb]: " + video.getThumb());
                    //Log.d("AppView", "media objects [url]: " + video.getUrl());
                }
            }


            if (details.getScreenshotsHd() != null) {
                for (GetApkInfoJson.Media.Screenshots screenshot : details.getScreenshotsHd()) {
                    imagesPath.add(new Screenshot(screenshot.getPath(), screenshot.getOrient(), true));
                }
            } else if (details.getScreenshots() != null) {
                for (String screenshot : details.getScreenshots()) {
                    imagesPath.add(new Screenshot(screenshot, "portrait", false));
                }
            }

            return imagesPath;
        }

        public String getLikes() {
            return details.getLikes();
        }

        public String getDontLikes() {
            return details.getDontLikes();
        }

        public String getNews() {
            return details.getNews();
        }

        public float getRating() {
            return details.rating;
        }

        public String getStore() {
            return details.getStore();
        }

        public MultiStoreItem[] getOtherVersions() {
            return details.getVersions();
        }
    }


    public static class RatingEvent {
        private ArrayList<Comment> comments;
        private String uservote;
        private GetApkInfoJson.Meta.Votes flagVotes;
        private String flagUservote;
        private GetApkInfoJson.Meta.Veredict flagVeredict;

        public String getCacheString() {
            return cacheString;
        }

        public void setCacheString(String cacheString) {
            this.cacheString = cacheString;
        }

        private String cacheString;

        public RatingEvent() {

        }

        public String getFlagUservote() {
            return flagUservote;
        }

        public void setFlagUservote(String flagUservote) {
            this.flagUservote = flagUservote;
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

        public GetApkInfoJson.Meta.Votes getFlagVotes() {
            return flagVotes;
        }

        public void setFlagVotes(GetApkInfoJson.Meta.Votes flagVotes) {
            this.flagVotes = flagVotes;
        }

        public GetApkInfoJson.Meta.Veredict getVeredict(){ return flagVeredict; }
        public void setFlagVeredict(GetApkInfoJson.Meta.Veredict flagVeredict){ this.flagVeredict = flagVeredict; }
    }

    public static class SpecsEvent {
        private ArrayList<String> permissions;
        private Filters.Screen minScreen = Filters.Screen.normal;
        private int minSdk;

        private SpecsEvent() {

        }

        public ArrayList getPermissions() {
            return permissions;
        }

        public void setPermissions(ArrayList<String> permissions) {
            this.permissions = permissions;
        }

        public Filters.Screen getMinScreen() {
            return minScreen;
        }

        public int getMinSdk() {
            return minSdk;
        }

        public void setMinSdk(int minSdk) {
            this.minSdk = minSdk;
        }

        public void setMinScreen(Filters.Screen minScreen) {
            this.minScreen = minScreen;
        }
    }
    /*
        public static class RelatedAppsEvent {
            private RelatedAppsEvent(Related related) {

            }
        }
    */
    private static class Screenshots {
        private List<String> screenshots;

        public List<String> getScreenshots() {
            return screenshots;
        }

        public void setScreenshots(List<String> screenshots) {
            this.screenshots = screenshots;
        }
    }

    public static class RelatedEvent{

    }

    private static class Details {

        private GetApkInfoJson.Meta.Developer developer;
        private String news;
        public float rating;
        private List<GetApkInfoJson.Media.Screenshots> screenshotsHd;
        private List<GetApkInfoJson.Media.Videos> videos;
        private MultiStoreItem[] versions;

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

        public List<String> getScreenshots() {
            return screenshots;
        }

        public void setScreenshots(List<String> screenshots) {
            this.screenshots = screenshots;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getDontLikes() {
            return dontLikes;
        }

        public void setDontLikes(String dontLikes) {
            this.dontLikes = dontLikes;
        }

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

        public void setScreenshotsHd(List<GetApkInfoJson.Media.Screenshots> screenshotsHd) {
            this.screenshotsHd = screenshotsHd;
        }

        public List<GetApkInfoJson.Media.Screenshots> getScreenshotsHd() {
            return screenshotsHd;
        }

        public List<GetApkInfoJson.Media.Videos> getVideos() {
            return videos;
        }

        public void setVideos(List<GetApkInfoJson.Media.Videos> videos) {
            this.videos = videos;
        }

        public void setOtherVersions(MultiStoreItem[] versions){ this.versions = versions;}

        public MultiStoreItem[] getVersions() {
            return versions;
        }
    }

/*    private static class Rating {}
    private static class Specs {}
    private static class Related {}*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != LOGIN_REQUEST_CODE) {
            if (requestCode == DOWGRADE_REQUEST_CODE) {

                //Log.d("Downgrade", "OnactivityResult");
                try {
                    getPackageManager().getPackageInfo(package_name, 0);

                    Toast.makeText(this, getString(R.string.downgrade_requires_uninstall), Toast.LENGTH_SHORT).show();

                } catch (PackageManager.NameNotFoundException e) {
                    isFromActivityResult = true;
                    spiceManager.getFromCache( GetApkInfoJson.class, cacheKey, DurationInMillis.ONE_HOUR, requestListener );

                }
            } else if (requestCode == 359) {
                //Log.d( "commentsUpdate", "AppViewActivity : onActivityResult" );
                refreshOnResume = true;
            } else if(requestCode == Purchase_REQUEST_CODE){


                if(resultCode == RESULT_OK){
                    refreshOnResume = true;
                    autoDownload = true;
                }

                //Toast.makeText(this, "OnActivityResult " + getCacheKey(), Toast.LENGTH_LONG).show();


            }
        }
    }

    @Override
    public void addApkFlagClick(String flag) {

        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");

        AddApkFlagRequest flagRequest = new AddApkFlagRequest();
        flagRequest.setToken(token);
        flagRequest.setRepo(repoName);
        flagRequest.setMd5sum(md5);
        flagRequest.setFlag(flag);

        spiceManager.execute(flagRequest, new AlmostGenericResponseV2RequestListener(this) {
            @Override
            public void CaseOK() {
                spiceManager.removeDataFromCache(GetApkInfoJson.class, getCacheKey());
                BusProvider.getInstance().post(new AppViewRefresh());
            }
        });
    }

    private SuccessfullyPostCallback postCallback;

    public void setSuccessfullyPostCallback(SuccessfullyPostCallback postCallback) {
        this.postCallback = postCallback;
    }

    @Override
    public void addComment(String comment, String answerTo) {
        if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("username", "NOT_SIGNED_UP").equals("NOT_SIGNED_UP")) {

            AddCommentRequest request = new AddCommentRequest(this);
            request.setApkversion(getVersionName());
            request.setPackageName(getPackage_name());
            request.setRepo(getRepoName());
            request.setToken(getToken());
            request.setText(comment);

            if(answerTo != null) {
                request.setAnswearTo(answerTo);
            }

            if(comment.length()<10){
                Toast.makeText(getApplicationContext(), R.string.error_IARG_100, Toast.LENGTH_LONG).show();
                return;
            }

            spiceManager.execute(request, addCommentRequestListener);
            AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");
        } else {

            AptoideDialog.updateUsernameDialog().show(getSupportFragmentManager(), "updateNameDialog");

        }
    }

    RequestListener<GenericResponseV2> addCommentRequestListener =
            new AlmostGenericResponseV2RequestListener(this)  {
        @Override
        public void CaseOK() {
            Toast.makeText(Aptoide.getContext(), getString(R.string.comment_submitted), Toast.LENGTH_LONG).show();
            if(postCallback != null) {
                postCallback.clearState();
            }
            spiceManager.removeDataFromCache(GetApkInfoJson.class, (AppViewActivity.this).getCacheKey());
            BusProvider.getInstance().post(new AppViewRefresh());
        }
    };

    @Override
    public void voteComment(int commentId, AddApkCommentVoteRequest.CommentVote vote) {
        AddApkCommentVoteRequest commentVoteRequest = new AddApkCommentVoteRequest();

        commentVoteRequest.setRepo(repoName);
        commentVoteRequest.setToken(token);
        commentVoteRequest.setCmtid(commentId);
        commentVoteRequest.setVote(vote);

        spiceManager.execute(commentVoteRequest, commentRequestListener);
        AptoideDialog.pleaseWaitDialog().show(getSupportFragmentManager(), "pleaseWaitDialog");

    }

    RequestListener<GenericResponseV2> commentRequestListener =
            new AlmostGenericResponseV2RequestListener(this) {
        @Override
        public void CaseOK() {
            Toast.makeText(Aptoide.getContext(), getString(R.string.vote_submitted), Toast.LENGTH_LONG).show();
            //Log.d("likes","commentRequestListener");
            spiceManager.removeDataFromCache(GetApkInfoJson.class, (AppViewActivity.this).getCacheKey());
            BusProvider.getInstance().post(new AppViewRefresh());
        }
    };
}