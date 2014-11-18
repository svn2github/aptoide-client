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

    private String packageName, featuredGraphic, appName, download, vercode, md5sum, icon;

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
        download = getIntent().getStringExtra(DOWNLOAD_URL);
        vercode = getIntent().getStringExtra(VERCODE);
        md5sum = getIntent().getStringExtra(MD5_SUM);
        icon = getIntent().getStringExtra(APP_ICON);

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

                Log.d(TAG, json.getMeta().getTitle());
                Log.d(TAG, json.getApk().getIconHd());
                Log.d(TAG, json.getMeta().getDeveloper().getInfo().getName());
                Log.d(TAG, json.getApk().getVername());
                Log.d(TAG, json.getMeta().getDownloads()+"");
                Log.d(TAG, json.getMeta().getLikevotes().getLikes()+""+json.getMeta().getLikevotes().getDislikes());


//                try {
//                    row = new DetailsOverviewRow(json);
//                    Bitmap poster = Picasso.with(this)
//                            .load(json.getApk().getIconHd())
//                            .resize(Utils.dpToPx(getActivity().getResources().getInteger(R.integer.detail_thumbnail_square_size), getActivity().getApplicationContext()),
//                                    Utils.dpToPx(getActivity().getResources().getInteger(R.integer.detail_thumbnail_square_size), getActivity().getApplicationContext()))
//                            .centerCrop()
//                            .get();
//                    row.setImageBitmap(getActivity(), poster);
//
//                } catch (IOException e) {
//                } catch (NullPointerException e) {
//                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            return row;
        }

        @Override
        protected void onPostExecute(final DetailsOverviewRow detailRow) {
            if (detailRow == null)
                return;


        }

    }

}
