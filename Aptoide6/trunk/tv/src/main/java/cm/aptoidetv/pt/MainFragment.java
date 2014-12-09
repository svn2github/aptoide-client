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

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cm.aptoidetv.pt.Model.ApplicationAPK;
import cm.aptoidetv.pt.Model.BindInterface;
import cm.aptoidetv.pt.Model.EditorsChoice;
import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.RequestTV;
import cm.aptoidetv.pt.WebServices.Response;

public class MainFragment extends BrowseFragment{
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
/*    private static int GRID_ITEM_WIDTH = 200;
    private static int GRID_ITEM_HEIGHT = 200;
    private static String mVideosUrl;*/
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;

    private RequestListener<Response> requestListener;
    private List<EditorsChoice> mEditorsChoice = new ArrayList<>();
/*    private StoreApplication mStoreApplications = new StoreApplication();*/

    private SpiceManager manager = new SpiceManager(HttpService.class);

    @Override
    public void onStart() {
        super.onStart();
        manager.start(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.shouldStop();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

//        loadmEditorsChoice();

        prepareBackgroundManager();
        setupUIElements();

        setupEventListeners();
        RequestTV request = new RequestTV("store");

        requestListener= new RequestListener<Response>(){
            @Override
            public void onRequestFailure(SpiceException spiceException) {
            }

            @Override
            public void onRequestSuccess(Response response) {
                loadmEditorsChoice();
                mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                CardPresenter cardPresenter = new CardPresenter();

                //List<String> categories = getCategories();
                List<Response.GetStore.Widgets.Widget> categories = response.responses.getStore.datasets.widgets.data.list;

                if (categories == null || categories.isEmpty())
                    return;
                {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    for (EditorsChoice editorsChoice : mEditorsChoice) {
                        listRowAdapter.add(editorsChoice);
                    }
                    if(mEditorsChoice.size()>0) {
                        HeaderItem header = new HeaderItem(mRowsAdapter.size() - 1, "Editors Choice", null);
                        mRowsAdapter.add(new ListRow(header, listRowAdapter));
                    }
                }
                for( Response.GetStore.Widgets.Widget widget : categories ) {
                    if(widget==null || widget.data==null || response.responses.listApps.datasets.getDataset()==null)
                        continue;
                    final String ref_id = widget.data.ref_id;

                    if(response.responses.listApps.datasets.getDataset().get(ref_id)==null ||
                       response.responses.listApps.datasets.getDataset().get(ref_id).data==null ||
                       response.responses.listApps.datasets.getDataset().get(ref_id).data.list==null)
                        continue;

                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( cardPresenter );
                    for( Response.ListApps.Apk apk :  response.responses.listApps.datasets.getDataset().get(ref_id).data.list) {
                        ApplicationAPK storeApplication = new ApplicationAPK(apk,widget.name);
                        listRowAdapter.add(storeApplication);
                    }

                    if( listRowAdapter.size() > 0 ) {
                        HeaderItem header = new HeaderItem( mRowsAdapter.size() - 1, widget.name, null );

                        mRowsAdapter.add( new ListRow( header, listRowAdapter ) );
                    }

 /*                   ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( cardPresenter );
                    for( EditorsChoice editorsChoice : mEditorsChoice) {
                        if( widget.equalsIgnoreCase( editorsChoice.getCategories() ) )
                            listRowAdapter.add(editorsChoice);
                    }

                    for( StoreApplication.PackageName storeApplication : mStoreApplications.getPackagenames()) {
                        if( widget.equalsIgnoreCase(storeApplication.getCategory()) )
                            listRowAdapter.add(storeApplication);
                    }
                    if( listRowAdapter.size() > 0 ) {
                        HeaderItem header = new HeaderItem( mRowsAdapter.size() - 1, widget, null );
                        mRowsAdapter.add( new ListRow( header, listRowAdapter ) );
                    }*/
                }

                setAdapter( mRowsAdapter );

            }
        };
        manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED,  requestListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
//        setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        // set fastLane (or headers) background color

        TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor, R.attr.searchColor});
        int brandColorResourceId = typedArray.getResourceId(0, 0);
        int searchColorResourceId = typedArray.getResourceId(1, R.color.search_opaque);

        typedArray.recycle();
        setBrandColor(getResources().getColor(brandColorResourceId));


        // set search icon color
        setSearchAffordanceColor(getResources().getColor(searchColorResourceId));
    }
/*
    private void loadmEditorsChoice() {
        String json = Utils.loadJSONFromResource(getActivity(), R.raw.editorschoice);
        Gson gson = new Gson();
        Type collection = new TypeToken<ArrayList<EditorsChoice>>(){}.getType();
        mEditorsChoice = gson.fromJson( json, collection );

     String jsonInfo = Utils.loadJSONFromResource( getActivity(), R.raw.info );
        Gson gsonInfo = new Gson();
        Type collectionInfo = new TypeToken<StoreApplication>(){}.getType();
        mStoreApplications = gsonInfo.fromJson( jsonInfo, collectionInfo );

        //getLoaderManager().initLoader(0, null, this);
    }*/
    private final void loadmEditorsChoice() {
        String json = Utils.loadJSONFromResource(getActivity(), R.raw.editorschoice);
        Gson gson = new Gson();
        Type collection = new TypeToken<ArrayList<EditorsChoice>>(){}.getType();
        mEditorsChoice = gson.fromJson( json, collection );
    }
    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
//        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
  /*  @Override
    public Loader<HashMap<String, List<Movie>>> onCreateLoader(int arg0, Bundle arg1) {
        Log.d(TAG, "VideoItemLoader created ");
        return new VideoItemLoader(getActivity(), mVideosUrl);
    }

    *//*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     *//*
    @Override
    public void onLoadFinished(Loader<HashMap<String, List<Movie>>> arg0,
                               HashMap<String, List<Movie>> data) {


        mRowsAdapter = new ArrayObjectAdapter( new ListRowPresenter() );
        CardPresenter cardPresenter = new CardPresenter();

        List<String> categories = getCategories();
        if( categories == null || categories.isEmpty() )
            return;

        for( String category : categories ) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( cardPresenter );
            for( EditorsChoice editorsChoice : mEditorsChoice) {
                if( category.equalsIgnoreCase( editorsChoice.getCategories() ) )
                    listRowAdapter.add(editorsChoice);
            }



            for( StoreApplication.PackageName storeApplication : mStoreApplications.getPackagenames()) {
                if( category.equalsIgnoreCase(storeApplication.getCategory()) )
                    listRowAdapter.add(storeApplication);
            }
            if( listRowAdapter.size() > 0 ) {
                HeaderItem header = new HeaderItem( mRowsAdapter.size() - 1, category, null );
                mRowsAdapter.add( new ListRow( header, listRowAdapter ) );
            }
        }

        setAdapter( mRowsAdapter );

    @Override
    public void onLoaderReset(Loader<HashMap<String, List<Movie>>> arg0) {
        mRowsAdapter.clear();
    }
    }
*/
/*
    private List<String> getCategories() {
        if( mEditorsChoice == null )
            return null;

        List<String> categories = new ArrayList<String>();
        for( EditorsChoice editorsChoice : mEditorsChoice) {
            if( !categories.contains(editorsChoice.getCategories()) ) {
                categories.add(editorsChoice.getCategories());
            }
        }

        for( StoreApplication.PackageName storeApplication : mStoreApplications.getPackagenames()) {
            if( !categories.contains(storeApplication.getCategory()) ) {
                categories.add(storeApplication.getCategory());
            }

        }

        return categories;
    }
*/



    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = getResources().getDrawable(resourceId);
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
        mBackgroundTimer.cancel();
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }


    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });
        }
    }


    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

//            if (item instanceof Movie) {
//                Movie movie = (Movie) item;
//                Log.d(TAG, "Item: " + item.toString());
//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                intent.putExtra(DetailsActivity.MOVIE, movie);
//
//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);
//            } else if (item instanceof String) {
//                if (((String) item).indexOf(getString(R.string.grid_view)) >= 0) {
//                    Intent intent = new Intent(getActivity(), VerticalGridActivity.class);
//                    startActivity(intent);
//                } else if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
//                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
            ((BindInterface)item).startActivity(getActivity());

        }
    }


    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                mBackgroundURI = ((Movie) item).getBackgroundImageURI();
                startBackgroundTimer();
            }

        }
    }
}
