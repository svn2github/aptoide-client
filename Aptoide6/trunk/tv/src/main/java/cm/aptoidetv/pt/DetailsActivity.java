/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cm.aptoidetv.pt;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Data;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cm.aptoidetv.pt.Model.Comment;
import cm.aptoidetv.pt.Model.GetApkInfoJson;
import cm.aptoidetv.pt.Model.MediaObject;
import cm.aptoidetv.pt.Model.Screenshot;
import cm.aptoidetv.pt.Model.Video;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends Activity {

    private DownloadManager downloadmanager;

    public static final String PACKAGE_NAME = "packageName";
    public static final String FEATURED_GRAPHIC = "featuredGraphic";
    public static final String APP_NAME = "name";
    public static final String DOWNLOAD_URL = "download";
    public static final String VERCODE = "vercode";
    public static final String MD5_SUM = "md5sum";
    public static final String APP_ICON = "icon";
    private static final String TAG = "DetailsActivity";
    public static final String MOVIE = "";
    public static final String SHARED_ELEMENT_NAME = "";

    private String packageName, featuredGraphic, appName, downloads, vercode, md5sum, icon;

    private ImageView app_icon;
    private Button download;
    private TextView app_name;
    private TextView app_developer;
    private TextView app_version;
    private TextView app_downloads;
    private RatingBar rating_bar;
    private TextView app_ratings;
    private TextView app_description;
    private ProgressBar downloading_progress;
    private LinearLayout screenshots;
    private LinearLayout commentsContainer;
    private TextView comments_label;
    private LinearLayout commentsLayout;
//    private TextView noComments;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        packageName = getIntent().getStringExtra(PACKAGE_NAME);
        featuredGraphic = getIntent().getStringExtra(FEATURED_GRAPHIC);
        appName = getIntent().getStringExtra(APP_NAME);
        downloads = getIntent().getStringExtra(DOWNLOAD_URL);
        vercode = getIntent().getStringExtra(VERCODE);
        md5sum = getIntent().getStringExtra(MD5_SUM);
        icon = getIntent().getStringExtra(APP_ICON);

        app_icon = (ImageView) findViewById(R.id.app_icon);
        download = (Button) findViewById(R.id.download);
        app_name = (TextView) findViewById(R.id.app_name);
        app_developer = (TextView) findViewById(R.id.app_developer);
        app_version = (TextView) findViewById(R.id.app_version);
        app_downloads = (TextView) findViewById(R.id.app_downloads);
        rating_bar = (RatingBar) findViewById(R.id.rating_bar);
        app_ratings = (TextView) findViewById(R.id.app_ratings);
        app_description = (TextView) findViewById(R.id.app_description);
        screenshots = (LinearLayout) findViewById(R.id.screenshots);
        comments_label = (TextView) findViewById(R.id.comments_label);
        commentsLayout = (LinearLayout) findViewById(R.id.layout_comments);
//        noComments = (TextView) findViewById(R.id.no_comments);
        commentsContainer = (LinearLayout) findViewById(R.id.commentContainer);

        new DetailRowBuilderTask().execute(md5sum);

    }

    private class DetailRowBuilderTask extends AsyncTask<String, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(String... application) {

//            mSelectedApp = application[0];

            DetailsOverviewRow row = null;

            try {
                GenericUrl url = new GenericUrl("http://webservices.aptoide.com/webservices/3/getApkInfo/+"+getString(R.string.defaultstorename)+"/md5sum:" + application[0] + "/json");

                Log.d(TAG, url.toString());
                HttpRequest httpRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildGetRequest(url);

                httpRequest.setParser(new GsonFactory().createJsonObjectParser());

                GetApkInfoJson json = httpRequest.execute().parseAs(GetApkInfoJson.class);

//                Log.d(TAG, json.getMeta().getTitle());
//                Log.d(TAG, json.getApk().getIconHd());
//                Log.d(TAG, json.getMeta().getDeveloper().getInfo().getName());
//                Log.d(TAG, json.getApk().getVername());
//                Log.d(TAG, json.getMeta().getDownloads()+"");
//                Log.d(TAG, json.getMeta().getLikevotes().getLikes()+""+json.getMeta().getLikevotes().getDislikes());
//                Log.d(TAG, json.getMeta().getDescription());


                try {
                    row = new DetailsOverviewRow(json);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            return row;
        }

        @Override
        protected void onPostExecute(final DetailsOverviewRow detailRow) {
            if (detailRow == null)
                return;

            try {



                boolean iconHdExistes = !Data.isNull(((GetApkInfoJson) detailRow.getItem()).getApk().getIconHd());

                String iconPath;

                if(iconHdExistes){
                    iconPath = ((GetApkInfoJson) detailRow.getItem()).getApk().getIconHd();
                }else{
                    iconPath = ((GetApkInfoJson) detailRow.getItem()).getApk().getIcon();
                }

                Picasso.with(DetailsActivity.this)
                        .load(iconPath)
                        .error(R.drawable.icon_non_available)
                        .into(app_icon);
//                Log.d(TAG, "Loading icon " + ((GetApkInfoJson) detailRow.getItem()).getApk().getIconHd());

                app_name.setText(((GetApkInfoJson) detailRow.getItem()).getMeta().getTitle());

                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String servicestring = Context.DOWNLOAD_SERVICE;

                        downloadmanager = (DownloadManager) getSystemService(servicestring);

                        Uri uri = Uri.parse(((GetApkInfoJson) detailRow.getItem()).getApk().getPath());
//                        Log.d(TAG, "getPath() " + ((GetApkInfoJson) detailRow.getItem()).getApk().getPath());

                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        new updateDownLoadInfoTask().execute();
                        request.addRequestHeader("User-Agent", Utils.getUserAgentString(DetailsActivity.this));
                        Log.d(TAG, "User-Agent" + Utils.getUserAgentString(DetailsActivity.this));

                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(false);
                        request.setTitle("Downloading " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getTitle());
//                        Log.d(TAG, "getName() " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getTitle());

                        request.setDestinationInExternalPublicDir("apks", (((GetApkInfoJson) detailRow.getItem()).getApk().getPackage() + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getVercode().intValue()) + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getMd5sum()) + ".apk"));
//                        Log.d(TAG, "save to sdcard: " + (((GetApkInfoJson) detailRow.getItem()).getApk().getPackage() + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getVercode().intValue()) + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getMd5sum()) + ".apk"));

                        downloadmanager.enqueue(request);
                        //registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        //isRegistered = true;
                    }
                });

                app_developer.setText(((GetApkInfoJson) detailRow.getItem()).getMeta().getDeveloper().getInfo().getName());
                app_version.setText(getString(R.string.version)+": " + ((GetApkInfoJson) detailRow.getItem()).getApk().getVername());
                app_downloads.setText(getString(R.string.downloads)+": " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getDownloads());
                rating_bar.setRating(((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getRating().floatValue());
                app_ratings.setText(getString(R.string.likes)+": " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getLikes() +" "+ getString(R.string.dislikes)+": " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getDislikes());
                String description = ((GetApkInfoJson) detailRow.getItem()).getMeta().getDescription();
                app_description.setText(Html.fromHtml(description.replace("\n", "<br/>")));

                screenshots.removeAllViews();
                View cell;
                ArrayList<MediaObject> mediaObjects = ((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshotsAndThumbVideo();
                String imagePath = "";
                int screenshotIndexToAdd = 0;
                for (int i = 0; i != mediaObjects.size(); i++) {
//                    Log.d(TAG, "mediaObjects: " + mediaObjects.get(i).getImageUrl());
                    cell = getLayoutInflater().inflate(R.layout.row_item_screenshots_gallery, null);
                    final ImageView imageView = (ImageView) cell.findViewById(R.id.screenshot_image_item);
                    final ProgressBar progress = (ProgressBar) cell.findViewById(R.id.screenshot_loading_item);
                    final ImageView play = (ImageView) cell.findViewById(R.id.play_button);
                    final FrameLayout mediaLayout = (FrameLayout) cell.findViewById(R.id.media_layout);

                    if (mediaObjects.get(i) instanceof Video) {
                        screenshotIndexToAdd++;
                        imagePath = mediaObjects.get(i).getImageUrl();
//                        Log.d(TAG, "VIDEOIMAGEPATH: " + imagePath);
                        play.setVisibility(View.VISIBLE);
                        mediaLayout.setForeground(getResources().getDrawable(R.color.overlay_black));
                        imageView.setOnClickListener(new VideoListener(DetailsActivity.this, ((Video) mediaObjects.get(i)).getVideoUrl()));
                        mediaLayout.setOnClickListener(new VideoListener(DetailsActivity.this, ((Video) mediaObjects.get(i)).getVideoUrl()));
                        //Log.d("FragmentAppView", "VIDEOURL: " + ((Video) mediaObjects.get(i)).getVideoUrl());


                    } else if (mediaObjects.get(i) instanceof Screenshot) {
                        imagePath = Utils.screenshotToThumb(DetailsActivity.this, mediaObjects.get(i).getImageUrl(), ((Screenshot) mediaObjects.get(i)).getOrient());
//                        Log.d(TAG, "IMAGEPATH: " + imagePath);
                        imageView.setOnClickListener(new ScreenShotsListener(DetailsActivity.this, new ArrayList<String>(((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshots()), i - screenshotIndexToAdd));
                        mediaLayout.setOnClickListener(new ScreenShotsListener(DetailsActivity.this, new ArrayList<String>(((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshots()), i - screenshotIndexToAdd));
                    }

                    screenshots.addView(cell);

                    Picasso.with(DetailsActivity.this)
                            .load(imagePath)
                            .error(R.drawable.icon_non_available)
                            .into(imageView);




                }

                List<Comment> comments = ((GetApkInfoJson) detailRow.getItem()).getMeta().getComments();
                Log.d(TAG, "comments size: " + comments.size());

                for(int i=0; i < comments.size(); i++) {
                    Log.d(TAG, "comments["+i+"]: " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getComments().get(i).getText());
                }
                FillComments.fillComments(DetailsActivity.this, commentsContainer, comments);
                commentsLayout.setVisibility(View.VISIBLE);
                if (comments.size() == 0) {
                    commentsLayout.setVisibility(View.GONE);
                    comments_label.setVisibility(View.GONE);
                    Log.d(TAG, getString(R.string.no_comments));
//                    noComments.startAnimation(AnimationUtils.loadAnimation(DetailsActivity.this, android.R.anim.fade_in));
//                    noComments.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public static class FillComments{

        public static void fillComments(Activity activity, LinearLayout commentsContainer, List<Comment> comments) {
            final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            View view;

            commentsContainer.removeAllViews();
            for (Comment comment : FragmentComments.getCompoundedComments(comments)) {
                view = FragmentComments.createCommentView(activity, commentsContainer, comment, dateFormater);
                commentsContainer.addView(view);
            }
        }
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

    private class updateDownLoadInfoTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            downloading_progress = (ProgressBar) findViewById(R.id.downloading_progress);
            downloading_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            downloading_progress.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            downloading_progress.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean loopagain = true;
            do {
                DownloadManager.Query query = new DownloadManager.Query();

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

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(new File(Environment.DIRECTORY_DOWNLOADS + "/apks/" + packageName + "-" + vercode + "-" + md5sum + ".apk")), "application/vnd.android.package-archive");
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(intent);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        case DownloadManager.STATUS_FAILED:
                            loopagain = false;
                            break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } while (loopagain);
            return null;
        }
    }
}


