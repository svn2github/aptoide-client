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
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import cm.aptoidetv.pt.Model.GetApkInfoJson;
import cm.aptoidetv.pt.Model.MediaObject;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends Activity {

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

        new DetailRowBuilderTask().execute(md5sum);

    }

    private class DetailRowBuilderTask extends AsyncTask<String, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground(String... application) {

//            mSelectedApp = application[0];

            DetailsOverviewRow row = null;

            try {
                GenericUrl url = new GenericUrl("http://webservices.aptoide.com/webservices/3/getApkInfo/geniatechapps/md5sum:" + application[0] + "/json");

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
                Log.d(TAG, json.getMeta().getDescription());


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

                Picasso.with(DetailsActivity.this)
                        .load(((GetApkInfoJson) detailRow.getItem()).getApk().getIconHd())
                        .error(R.drawable.icon_non_available)
                        .into(app_icon);
//                Log.d(TAG, "Loading icon " + ((GetApkInfoJson) detailRow.getItem()).getApk().getIconHd());

                app_name.setText(((GetApkInfoJson) detailRow.getItem()).getMeta().getTitle());
                app_developer.setText(((GetApkInfoJson) detailRow.getItem()).getMeta().getDeveloper().getInfo().getName());
                app_version.setText("Version: " + ((GetApkInfoJson) detailRow.getItem()).getApk().getVername());
                app_downloads.setText("Downloads: " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getDownloads());
                rating_bar.setRating(((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getRating().floatValue());
                app_ratings.setText("Likes: " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getLikes() + " Dislikes: " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getLikevotes().getDislikes());
                app_description.setText(((GetApkInfoJson) detailRow.getItem()).getMeta().getDescription());



            } catch (Exception e) {
                e.printStackTrace();
            }


            ClassPresenterSelector ps = new ClassPresenterSelector();

            ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
            adapter.add(detailRow);

            ArrayList<MediaObject> mediaObjects;
            mediaObjects = ((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshotsAndThumbVideo();


            Log.d(TAG, "mediaObjects size " + mediaObjects.size());


            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new ScreenshotsPresenter());
            for (MediaObject mediaObject : mediaObjects) {
                listRowAdapter.add(mediaObject);
            }

            HeaderItem header = new HeaderItem(0, "Screenshots", null);
            adapter.add(new ListRow(header, listRowAdapter));

        }

    }

}
