package cm.aptoidetv.pt;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cm.aptoidetv.pt.Dialogs.ProgressDialogFragment;
import cm.aptoidetv.pt.Dialogs.UsernameDialog;
import cm.aptoidetv.pt.Model.Comment;
import cm.aptoidetv.pt.Model.GetApkInfoJson;
import cm.aptoidetv.pt.Model.MediaObject;
import cm.aptoidetv.pt.Model.Screenshot;
import cm.aptoidetv.pt.Model.Video;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.old.AddCommentRequest;
import cm.aptoidetv.pt.WebServices.old.AddLikeRequest;
import cm.aptoidetv.pt.WebServices.old.AptoideUtils;
import cm.aptoidetv.pt.WebServices.old.GetApkInfoRequestFromMd5;
import cm.aptoidetv.pt.WebServices.old.UpdateUserRequest;
import cm.aptoidetv.pt.WebServices.old.json.GenericResponseV2;

public class DetailsActivity extends ActionBarActivity {
    private DownloadManager downloadmanager;
    private long downloadID;

    public static final String CACHEKEYLikeRequest = "CK";
    public static final String PACKAGE_NAME = "packageName";
    public static final String FEATURED_GRAPHIC = "featuredGraphic";
    public static final String APP_NAME = "name";
    public static final String DOWNLOADS = "downloads";
    public static final String DOWNLOAD_URL = "download_URL";
    public static final String MD5_SUM = "md5sum";
    public static final String APP_ICON = "icon";
    public static final String APP_SIZE = "size";

    private String packageName, downloads, verName, md5sum, icon, size;

    private ImageView app_icon;
    private Button downloadButton;
    private Button canceldownloadButton;
    private TextView app_developer;
    private TextView app_version;
    private TextView app_downloads;
    private RatingBar rating_bar;
    private TextView app_ratings;
    private TextView app_size;
    private TextView app_description;
    private ProgressBar downloading_progress;
    private TextView downloading_info;
    private LinearLayout screenshots;
    private LinearLayout commentsContainer;
    private LinearLayout commentsLayout;
    private View app_view_details_layout;
    private View loading_pb;
    private EditText editText_addcomment;

//    private TextView noComments;

    private SpiceManager manager = new SpiceManager(HttpService.class);
    final private  DetailsActivity activity =this;
    @Override
    public void onStart() {
        super.onStart();
        manager.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.shouldStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelDownload();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_AppCompat);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String appName = getIntent().getStringExtra(APP_NAME);
        if(appName!=null) {
            TextView app_name = (TextView) findViewById(R.id.app_name);
            app_name.setText(appName);
        }

        packageName = getIntent().getStringExtra(PACKAGE_NAME);
        /*featuredGraphic = getIntent().getStringExtra(FEATURED_GRAPHIC);
        download_URL = getIntent().getStringExtra(DOWNLOAD_URL);*/
        downloads = getIntent().getStringExtra(DOWNLOADS);
        md5sum = getIntent().getStringExtra(MD5_SUM);
        icon = getIntent().getStringExtra(APP_ICON);
        size = getIntent().getStringExtra(APP_SIZE);

        app_view_details_layout = findViewById(R.id.app_view_details_layout);
        loading_pb = findViewById(R.id.loading_pb);

        app_icon = (ImageView) findViewById(R.id.app_icon);
        downloadButton = (Button) findViewById(R.id.download);
        app_developer = (TextView) findViewById(R.id.app_developer);
        app_version = (TextView) findViewById(R.id.app_version);
        app_downloads = (TextView) findViewById(R.id.app_downloads);
        if(downloads!=null)
            app_downloads.setText(getString(R.string.downloads)+": " + downloads);
        rating_bar = (RatingBar) findViewById(R.id.rating_bar);
        app_ratings = (TextView) findViewById(R.id.app_ratings);
        app_size = (TextView) findViewById(R.id.app_size);
        app_description = (TextView) findViewById(R.id.app_description);
        screenshots = (LinearLayout) findViewById(R.id.screenshots);
        commentsLayout = (LinearLayout) findViewById(R.id.layout_comments);
//        noComments = (TextView) findViewById(R.id.no_comments);
        commentsContainer = (LinearLayout) findViewById(R.id.comments_container);
        editText_addcomment = (EditText) findViewById(R.id.editText_addcomment);

        if(icon==null) {
            Picasso.with(this)
                    .load(icon)
                    .error(R.drawable.icon_non_available)
                    .into(app_icon);
        }
       // new DetailRowBuilderTask().execute(md5sum);

        GetApkInfoRequestFromMd5 request = new GetApkInfoRequestFromMd5(this);
        request.setMd5Sum(md5sum);

        manager.execute(request, "details"+md5sum, DurationInMillis.ALWAYS_RETURNED,  new RequestListener<GetApkInfoJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                finish();
            }

            @Override
            public void onRequestSuccess(GetApkInfoJson apkInfoJson) {
                loading_pb.setVisibility(View.GONE);
                app_view_details_layout.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.VISIBLE);
                addDownloadButtonListener(apkInfoJson);
                packageName = apkInfoJson.getApk().getPackage();
                int totalRatings =  apkInfoJson.getMeta().getLikevotes().getLikes().intValue() + apkInfoJson.getMeta().getLikevotes().getDislikes().intValue();
                if(downloads!=String.valueOf(apkInfoJson.getMeta().getDownloads())){
                    downloads= String.valueOf(apkInfoJson.getMeta().getDownloads());
                    app_downloads.setText(getString(R.string.downloads)+": " + downloads);
                }
                if(apkInfoJson.getMeta().getDeveloper()!=null)
                    app_developer.setText(apkInfoJson.getMeta().getDeveloper().getInfo().getName());
                verName = apkInfoJson.getApk().getVername();
                app_version.setText(getString(R.string.version)+": " + verName);
                app_size.setText(getString(R.string.size)+": "+ Utils.formatBytes(apkInfoJson.getApk().getSize().longValue()));
                if(totalRatings>0){
                    rating_bar.setVisibility(View.VISIBLE);
                    rating_bar.setRating(apkInfoJson.getMeta().getLikevotes().getRating().floatValue());
                    RatingBar rating_bar2 = (RatingBar) findViewById(R.id.rating_bar2);
                    rating_bar2.setVisibility(View.VISIBLE);
                    rating_bar2.setRating(apkInfoJson.getMeta().getLikevotes().getRating().floatValue());
                }else {
                    rating_bar.setVisibility(View.INVISIBLE);
                }
                findViewById(R.id.button_like).setOnClickListener(new AddLikeListener(true,manager));
                findViewById(R.id.button_dont_like).setOnClickListener(new AddLikeListener(false,manager));
                findViewById(R.id.button_send_comment).setOnClickListener(new AddCommentListener());
                app_ratings.setText("("+totalRatings + " " + getString(R.string.ratings)+")");
                String description = apkInfoJson.getMeta().getDescription();
                app_description.setText(Html.fromHtml(description.replace("\n", "<br/>")));

                icon = apkInfoJson.getApk().getIconhd()!=null?apkInfoJson.getApk().getIconhd():apkInfoJson.getApk().getIcon();
                if(icon !=null)
                    Picasso.with(DetailsActivity.this)
                            .load(icon)
                            .error(R.drawable.icon_non_available)
                            .into(app_icon);

                screenshots.removeAllViews();
                View cell;
                ArrayList<MediaObject> mediaObjects = apkInfoJson.getMedia().getScreenshotsAndThumbVideo();
                String imagePath = "";
                int screenshotIndexToAdd = 0;

                for (int i = 0; i != mediaObjects.size(); i++) {

                    cell = getLayoutInflater().inflate(R.layout.row_item_screenshots_gallery, null);
                    final ImageView imageView = (ImageView) cell.findViewById(R.id.screenshot_image_item);
                    //final ProgressBar progress = (ProgressBar) cell.findViewById(R.id.screenshot_loading_item);
                    final ImageView play = (ImageView) cell.findViewById(R.id.play_button);
                    final FrameLayout mediaLayout = (FrameLayout) cell.findViewById(R.id.media_layout);

                    if (mediaObjects.get(i) instanceof Video) {
                        screenshotIndexToAdd++;
                        imagePath = mediaObjects.get(i).getImageUrl();
                        play.setVisibility(View.VISIBLE);
                        imageView.setOnClickListener(new VideoListener(DetailsActivity.this, ((Video) mediaObjects.get(i)).getVideoUrl()));
                        mediaLayout.setOnClickListener(new VideoListener(DetailsActivity.this, ((Video) mediaObjects.get(i)).getVideoUrl()));

                    } else if (mediaObjects.get(i) instanceof Screenshot) {
                        imagePath = Utils.screenshotToThumb(DetailsActivity.this, mediaObjects.get(i).getImageUrl(), ((Screenshot) mediaObjects.get(i)).getOrient());
                        imageView.setOnClickListener(new ScreenShotsListener(DetailsActivity.this, new ArrayList<String>(apkInfoJson.getMedia().getScreenshots()), i - screenshotIndexToAdd));
                        mediaLayout.setOnClickListener(new ScreenShotsListener(DetailsActivity.this, new ArrayList<String>(apkInfoJson.getMedia().getScreenshots()), i - screenshotIndexToAdd));
                    }
                    screenshots.addView(cell);
                    AppTV.getPicasso()
                            .load(imagePath)
                            .error(R.drawable.icon_non_available)
                            .into(imageView);
                }
                List<Comment> comments = apkInfoJson.getMeta().getComments();
//                Log.d(TAG, "comments size: " + comments.size());

//                for(int i=0; i < comments.size(); i++) {
//                    Log.d(TAG, "comments["+i+"]: " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getComments().get(i).getText());
//                }
                final View TocommentsButtonFocus = fillComments(DetailsActivity.this, commentsContainer, comments);

                if (TocommentsButtonFocus==null) {
                    findViewById(R.id.TocommentsButton).setVisibility(View.GONE);
                    commentsLayout.setVisibility(View.GONE);
                    findViewById(R.id.no_comments).setVisibility(View.VISIBLE);
//                    Log.d(TAG, getString(R.string.no_comments));
//                    noComments.startAnimation(AnimationUtils.loadAnimation(DetailsActivity.this, android.R.anim.fade_in));
//                    noComments.setVisibility(View.VISIBLE);
                }else{
                    View TocommentsButton = findViewById(R.id.TocommentsButton);
                    findViewById(R.id.no_comments).setVisibility(View.GONE);
                    TocommentsButton.setVisibility(View.VISIBLE);
                    commentsLayout.setVisibility(View.VISIBLE);
                    TocommentsButtonFocus.setFocusable(true);
                    TocommentsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TocommentsButtonFocus.requestFocus();
                        }
                    });
                }
            }
        });
    }

    private PackageInfo getPackageInfo(String package_name){
        try {
            return getPackageManager().getPackageInfo(package_name, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    private void changebtInstalltoOpen(String packageName){
        final Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
        downloadButton.setText(getString(R.string.open));
        if (i != null) {
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            });
        } else {
            downloadButton.setEnabled(false);
        }
    }
    private void addDownloadButtonListener(final GetApkInfoJson ApkInfoJson){
        PackageInfo info = getPackageInfo(ApkInfoJson.getApk().getPackage());
        if (info != null && info.versionCode>=ApkInfoJson.getApk().getVercode().intValue()) {
            changebtInstalltoOpen(ApkInfoJson.getApk().getPackage());
        } else {
            if(info!=null)
                downloadButton.setText(getString(R.string.update));
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String servicestring = Context.DOWNLOAD_SERVICE;
                        downloadmanager = (DownloadManager) getSystemService(servicestring);

                        Uri uri = Uri.parse(ApkInfoJson.getApk().getPath());
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        String APKpath = ApkInfoJson.getApk().getPackage() + "-" + (ApkInfoJson.getApk().getVercode().intValue()) + "-" + (ApkInfoJson.getApk().getMd5sum()) + ".apk";
                        new updateDownLoadInfoTask().execute(APKpath);
                        request.addRequestHeader("User-Agent", Utils.getUserAgentString(DetailsActivity.this));
                        request.setAllowedOverRoaming(false);
                        request.setTitle("Downloading " + ApkInfoJson.getMeta().getTitle());
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        request.setDestinationInExternalPublicDir("apks", APKpath);
                        downloadID = downloadmanager.enqueue(request);
                    }
                });
        }
    }

    public void cancelDownload(View v){
        if(downloadmanager.remove(downloadID)>0){
            restoreDownloadButton();
        }
    }
    private void cancelDownload() {
        if(downloadmanager!=null)
            downloadmanager.remove(downloadID);
    }

    private void restoreDownloadButton(){
        downloading_progress.setVisibility(View.GONE);
        downloading_info.setVisibility(View.GONE);
        canceldownloadButton.setVisibility(View.GONE);
        downloadButton.setVisibility(View.VISIBLE);
    }

    public View fillComments(Activity activity, LinearLayout commentsContainer, List<Comment> comments) {
        final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        View view;
        View viewtoRet=null;
        commentsContainer.removeAllViews();

        for (Comment comment : FragmentComments.getCompoundedComments(comments)) {
            view = FragmentComments.createCommentView(activity, commentsContainer, comment, dateFormater);
            commentsContainer.addView(view);
            if(viewtoRet==null){
                viewtoRet=view;
            }
        }
        return viewtoRet;
    }

    public static class ScreenShotsListener implements View.OnClickListener {
        private Context context;
        private final int position;
        private ArrayList<String> urls;

        public ScreenShotsListener(Context context, ArrayList<String> urls, int position) {
            this.context = context;
            this.position = position;
            this.urls = urls;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ScreenshotsViewer.class);
            intent.putStringArrayListExtra("url", urls);
            intent.putExtra("position", position);
            context.startActivity(intent);
        }
    }

    public static class VideoListener implements View.OnClickListener {

        private Context context;
        private String videoUrl;

        public VideoListener(Context context, String videoUrl) {
            this.context = context;
            this.videoUrl = videoUrl;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            context.startActivity(intent);
        }
    }

    private class updateDownLoadInfoTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            downloading_progress = (ProgressBar) findViewById(R.id.downloading_progress);
            downloading_progress.setVisibility(View.VISIBLE);
            downloading_info = (TextView) findViewById(R.id.downloading_info);
            downloading_info.setVisibility(View.VISIBLE);
            canceldownloadButton = (Button) findViewById(R.id.canceldownloadButton);
            canceldownloadButton.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.GONE);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            restoreDownloadButton();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            downloading_progress.setProgress(values[0]);
            downloading_info.setText(values[0]+"%");
        }

        @Override
        protected Void doInBackground(String... path) {
            boolean loopagain = true;
            while (loopagain) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadID);
                Cursor c = downloadmanager.query(query);

                if (c.moveToFirst()) {
                    int bytes_downloaded = c.getInt(c
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    publishProgress(dl_progress);
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    switch (status) {
                        case DownloadManager.STATUS_PAUSED:
                        case DownloadManager.STATUS_PENDING:
                        case DownloadManager.STATUS_RUNNING:
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            try {
                                Log.d("pois","deu SUCCESSFUL:");
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                File apk = new File(absolutePath + "/apks/" + path[0]);
                                if(!apk.exists()){
                                    Log.d("pois","Nao existe!");
                                    loopagain = false;
                                    break;
                                }
                                intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");

                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(intent);

                            } catch (Exception e) {
                                Log.d("pois","deu exception");
                                e.printStackTrace();
                            }
                        case DownloadManager.STATUS_FAILED:
                            Log.d("pois","deu Fail");
                            loopagain = false;
                            break;
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                //Uri myDownloads = Uri.parse( "content://downloads/my_downloads" );
            }
            return null;
        }
    }

    public class DARequestListener implements RequestListener<GenericResponseV2> {
        private int msg;
        DARequestListener(int msgID){
            msg=msgID;
        }
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            dismiss();
        }

        @Override
        public void onRequestSuccess(GenericResponseV2 genericResponse) {
            dismiss();
            if (genericResponse.getStatus().equals("OK")) {
                Toast.makeText(AppTV.getContext(), getString(msg), Toast.LENGTH_LONG).show();
                manager.removeDataFromCache(GetApkInfoJson.class, CACHEKEYLikeRequest);
            } else {
                AptoideUtils.toastError(genericResponse.getErrors());
            }
        }
    }

    public class AddCommentListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            final AccountManager accountManager = AccountManager.get(activity);

            if (accountManager.getAccountsByType(AppTV.getConfiguration().getAccountType()).length > 0) {
                if (!PreferenceManager.getDefaultSharedPreferences(AppTV.getContext()).getString("username", "NOT_SIGNED_UP").equals("NOT_SIGNED_UP")) {
                    String comment = editText_addcomment.getText().toString();

                    if(comment.length()<10){
                        Toast.makeText(getApplicationContext(), R.string.error_IARG_100, Toast.LENGTH_LONG).show();
                        return;
                    }

                    AddCommentRequest request = new AddCommentRequest(activity);
                    request.setApkversion(verName);
                    request.setPackageName(packageName);
                    request.setToken(LoginActivity.getToken(activity));
                    request.setText(comment);
                    manager.execute(request,CACHEKEYLikeRequest, DurationInMillis.ONE_SECOND, new DARequestListener(R.string.comment_submitted));
                    new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");
                } else {
                    new UsernameDialog().show(getSupportFragmentManager(), "updateNameDialog");
                }
            } else {
                accountManager.addAccount(AppTV.getConfiguration().getAccountType(), LoginActivity.AUTHTOKEN_TYPE_FULL_ACCESS,
                        null, null, activity, null, null);

            }

        }
    }

    public class AddLikeListener implements View.OnClickListener {
        private final boolean isLike;
        private SpiceManager manager;

        public AddLikeListener(boolean isLike,SpiceManager sm) {
            manager=sm;
            this.isLike = isLike;
        }


        @Override
        public void onClick(View v) {

            final AccountManager accountManager = AccountManager.get(activity);

            if (accountManager.getAccountsByType(AppTV.getConfiguration().getAccountType()).length > 0) {
                addLike();
            } else {
                accountManager.addAccount(AppTV.getConfiguration().getAccountType(),
                        LoginActivity.AUTHTOKEN_TYPE_FULL_ACCESS,
                        null, null, activity, null, null);
            }
        }

        private void addLike() {
            AddLikeRequest request = new AddLikeRequest(activity);
            request.setApkversion(verName);
            request.setpackagename(packageName);
            request.setToken(LoginActivity.getToken(activity));
            request.setLike(isLike);

            manager.execute(request, CACHEKEYLikeRequest, DurationInMillis.ONE_SECOND, new DARequestListener(R.string.opinion_success));
            new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");
        }
    }
    public void updateUsername(final String username) {
        UpdateUserRequest request = new UpdateUserRequest(this);
        request.setName(username);

        new ProgressDialogFragment().show(getSupportFragmentManager(), "pleaseWaitDialog");
        manager.execute(request, new RequestListener<GenericResponseV2>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                dismiss();
            }

            @Override
            public void onRequestSuccess(GenericResponseV2 createUserJson) {
                dismiss();

                if (createUserJson.getStatus().equals("OK")) {
                    Toast.makeText(AppTV.getContext(), R.string.username_success, Toast.LENGTH_LONG).show();
                    PreferenceManager.getDefaultSharedPreferences(activity).edit().putString("username", username).commit();
                } else {
                    AptoideUtils.toastError(createUserJson.getErrors());
                }
            }
        });
    }
    private void dismiss(){
        ProgressDialogFragment pd = (ProgressDialogFragment) getSupportFragmentManager()
                .findFragmentByTag("pleaseWaitDialog");
        if(pd!=null)
            pd.dismissAllowingStateLoss();
    }
}


