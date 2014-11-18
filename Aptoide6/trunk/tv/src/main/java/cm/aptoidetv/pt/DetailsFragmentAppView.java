package cm.aptoidetv.pt;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.Row;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import cm.aptoidetv.pt.Model.GetApkInfoJson;
import cm.aptoidetv.pt.Model.MediaObject;

public class DetailsFragmentAppView extends DetailsFragment {

    private static final int ACTION_WATCH_TRAILER = 1;
    private static final int ACTION_RENT = 2;
    private static final int ACTION_BUY = 3;


    public static final String PACKAGE_NAME = "packageName";
    public static final String FEATURED_GRAPHIC = "featuredGraphic";
    public static final String APP_NAME = "name";
    public static final String DOWNLOAD_URL = "download";
    public static final String VERCODE = "vercode";
    public static final String MD5_SUM = "md5sum";
    public static final String APP_ICON = "icon";


//    private GetApkInfoJson mSelectedApp;

    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private DownloadManager downloadmanager;
    private boolean isRegistered = false;
    private String TAG = "DetailsFragmentAppView";
    private String packageName, featuredGraphic, appName, download, vercode, md5sum, icon;
    private OnItemClickedListener defaultItemClickedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageName = getActivity().getIntent().getStringExtra(PACKAGE_NAME);
        featuredGraphic = getActivity().getIntent().getStringExtra(FEATURED_GRAPHIC);
        appName = getActivity().getIntent().getStringExtra(APP_NAME);
        download = getActivity().getIntent().getStringExtra(DOWNLOAD_URL);
        vercode = getActivity().getIntent().getStringExtra(VERCODE);
        md5sum = getActivity().getIntent().getStringExtra(MD5_SUM);
        icon = getActivity().getIntent().getStringExtra(APP_ICON);

        initBackground();

        setOnItemClickedListener(getDefaultItemClickedListener());

        new DetailRowBuilderTask().execute(md5sum);


    }

    private void initBackground() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget( backgroundManager );

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        if( featuredGraphic != null && !TextUtils.isEmpty(featuredGraphic)) {
            try {
                updateBackground(new URI(featuredGraphic));
            } catch (URISyntaxException e) { }
        } else {
            try {
                updateBackground(new URI(icon));
            } catch (URISyntaxException e) { }
        }
    }

    protected void updateBackground(URI uri) {
        if( uri.toString() == null ) {
            try {
                uri = new URI("");
            } catch( URISyntaxException e ) {}
        }

        Picasso.with(getActivity())
                .load(uri.toString())
                .error(getResources().getDrawable(R.drawable.default_background))
//                .resize( mMetrics.widthPixels, mMetrics.heightPixels )
                .into(mBackgroundTarget);
    }

    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            queryDownloadStatus();
        }
    };

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();

        Cursor c = downloadmanager.query(query);
        if(c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.d(TAG, "Status Check: " + status);
                Log.d(TAG, "Download URL: " + download);
                Log.d(TAG, "Name: " + appName);
//                Log.d(TAG, "Path: " + mSelectedApp.getPackagename().getPath());

            switch(status) {
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    try {

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.DIRECTORY_DOWNLOADS + "/apks/"  + packageName+"-"+vercode+"-"+md5sum+".apk")), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DownloadManager.STATUS_FAILED:

                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isRegistered) {
            getActivity().unregisterReceiver(onComplete);
            isRegistered = false;
        }
    }

    protected OnItemClickedListener getDefaultItemClickedListener() {
        return new OnItemClickedListener() {
            @Override
            public void onItemClicked(Object item, Row row) {

                ((MediaObject)item).startActivity(getActivity());

            }
        };
    }

    private class DetailRowBuilderTask extends AsyncTask<String, Integer, DetailsOverviewRow> {
        @Override
        protected DetailsOverviewRow doInBackground( String... application ) {

//            mSelectedApp = application[0];

            DetailsOverviewRow row = null;

            try {
                GenericUrl url = new GenericUrl("http://webservices.aptoide.com/webservices/3/getApkInfo/geniatechapps/md5sum:"+ application[0]+"/json");

                Log.d(TAG, url.toString());
                HttpRequest httpRequest = AndroidHttp.newCompatibleTransport().createRequestFactory().buildGetRequest(url);

                httpRequest.setParser( new GsonFactory().createJsonObjectParser());

                GetApkInfoJson json = httpRequest.execute().parseAs(GetApkInfoJson.class);




            try {
                row = new DetailsOverviewRow(json);
                Bitmap poster = Picasso.with(getActivity())
                        .load( json.getApk().getIconHd() )
                        .resize(Utils.dpToPx(getActivity().getResources().getInteger(R.integer.detail_thumbnail_square_size), getActivity().getApplicationContext()),
                                Utils.dpToPx(getActivity().getResources().getInteger(R.integer.detail_thumbnail_square_size), getActivity().getApplicationContext()))
                        .centerCrop()
                        .get();
                row.setImageBitmap( getActivity(), poster );

            } catch ( IOException e ) {
            } catch( NullPointerException e ) {
            }

            if (row != null) {
                row.addAction(new Action(ACTION_WATCH_TRAILER, "DOWNLOAD"));

            }

            } catch (IOException e) {
                e.printStackTrace();
            }



            return row;
        }

        @Override
        protected void onPostExecute( final DetailsOverviewRow detailRow ) {
            if( detailRow == null )
                return;

            ClassPresenterSelector ps = new ClassPresenterSelector();
            DetailsOverviewRowPresenter dorPresenter = new DetailsOverviewRowPresenter( new DetailsDescriptionPresenterAppView() );

            TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor});
            int brandColorResourceId = typedArray.getResourceId(0, 0);
            typedArray.recycle();

            // set detail background and style
            dorPresenter.setBackgroundColor( getResources().getColor(brandColorResourceId) );

            dorPresenter.setStyleLarge(true);

            dorPresenter.setOnActionClickedListener( new OnActionClickedListener() {
                @Override
                public void onActionClicked( Action action ) {
                    if (action.getId() == ACTION_WATCH_TRAILER ) {

                        String servicestring = Context.DOWNLOAD_SERVICE;

                        downloadmanager = (DownloadManager) getActivity().getSystemService(servicestring);

                        Uri uri = Uri.parse(((GetApkInfoJson)detailRow.getItem()).getApk().getPath());
                        Log.d(TAG, "getPath() " + ((GetApkInfoJson)detailRow.getItem()).getApk().getPath());

                        DownloadManager.Request request = new DownloadManager.Request(uri);

                        request.addRequestHeader("User-Agent", Utils.getUserAgentString(getActivity()));
                        Log.d(TAG, "User-Agent" +  Utils.getUserAgentString(getActivity()));

                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(false);
                        request.setTitle("Downloading " + ((GetApkInfoJson) detailRow.getItem()).getMeta().getTitle());
                        Log.d(TAG, "getName() " + ((GetApkInfoJson)detailRow.getItem()).getMeta().getTitle());

                        request.setDestinationInExternalPublicDir("apks", (((GetApkInfoJson) detailRow.getItem()).getApk().getPackage() + "-"+(((GetApkInfoJson) detailRow.getItem()).getApk().getVercode().intValue())+"-"+(((GetApkInfoJson) detailRow.getItem()).getApk().getMd5sum())+".apk"));
                        Log.d(TAG, "save to sdcard: " + (((GetApkInfoJson) detailRow.getItem()).getApk().getPackage() + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getVercode().intValue()) + "-" + (((GetApkInfoJson) detailRow.getItem()).getApk().getMd5sum()) + ".apk"));

                        downloadmanager.enqueue(request);
                        getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        isRegistered = true;
                    }
                }
            });

            ps.addClassPresenter( DetailsOverviewRow.class, dorPresenter );
            ps.addClassPresenter( ListRow.class, new ListRowPresenter() );

            ArrayObjectAdapter adapter = new ArrayObjectAdapter( ps );
            adapter.add( detailRow );

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( new DescriptionPresenter() );
            listRowAdapter.add( ((GetApkInfoJson) detailRow.getItem()).getMeta().getDescription() );

            HeaderItem header = new HeaderItem( 0, "Description", null );
            adapter.add( new ListRow( header, listRowAdapter ) );

            loadScreenshots(adapter, detailRow);

            setAdapter( adapter );
        }



        private void loadScreenshots(ArrayObjectAdapter adapter, DetailsOverviewRow detailRow) {

            ArrayList<MediaObject> mediaObjects;
            mediaObjects = ((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshotsAndThumbVideo();


            Log.d(TAG, "mediaObjects size " + mediaObjects.size());

//            List<Screenshot> screenshots = ((GetApkInfoJson) detailRow.getItem()).getMedia().getScreenshots();
//            List<GetApkInfoJson.Media.Screenshots> related = new ArrayList<GetApkInfoJson.Media.Screenshots>();
//            for( Screenshot screen : screenshots ) {
//                if( screen.getCategory().equals(screen.getCategory()) ) {
//                    related.add( screen );
//                }
//            }

//            if( related.isEmpty() )
//                return;

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( new ScreenshotsPresenter() );
            for( MediaObject mediaObject : mediaObjects ) {
                listRowAdapter.add( mediaObject );
            }

            HeaderItem header = new HeaderItem( 0, "Screenshots", null );
            adapter.add( new ListRow( header, listRowAdapter ) );


        }

    }

}
