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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.SearchFragment2;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemClickedListener;
import android.support.v17.leanback.widget.Row;
import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import cm.aptoidetv.pt.Model.BindInterface;
import cm.aptoidetv.pt.Model.SearchJson;

/*
 * This class demonstrates how to do in-app search 
 */
@SuppressLint("DefaultLocale")
public class SearchFragment extends SearchFragment2
        implements android.support.v17.leanback.app.SearchFragment2.SearchResultProvider {
    private static final String TAG = "SearchFragment";
    private static final int SEARCH_DELAY_MS = 300;

    private ArrayObjectAdapter mRowsAdapter;
    private Handler mHandler = new Handler();
    private SearchRunnable mDelayedLoad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);

        setOnItemClickedListener(getDefaultItemClickedListener());
        mDelayedLoad = new SearchRunnable();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    private void queryByWords(String words) {
        if (!TextUtils.isEmpty(words)) {
            mDelayedLoad.setSearchQuery(words);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.post(mDelayedLoad);
        }
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        queryByWords(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        queryByWords(query);
        return true;
    }

    private void loadRows(List<SearchJson.Results.Apks> apks) {

        mRowsAdapter.clear();

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for(SearchJson.Results.Apks apk : apks){

            listRowAdapter.add(apk);

        }

        HeaderItem header = new HeaderItem(0, "Search results", null);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));


//        HashMap<String, List<Movie>> applications = VideoProvider.getMovieList();
//        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//        for (Map.Entry<String, List<Movie>> entry : applications.entrySet())
//        {
//            for (int i = 0; i < entry.getValue().size(); i++) {
//                Movie application = entry.getValue().get(i);
//                if (application.getTitle().toLowerCase(Locale.ENGLISH).indexOf(query.toLowerCase(Locale.ENGLISH)) >= 0) {
//                    listRowAdapter.add(application);
//                }
//            }
//        }
//        HeaderItem header = new HeaderItem(0, getResources().getString(R.string.search_results),
//                null);
//        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    protected OnItemClickedListener getDefaultItemClickedListener() {
        return new OnItemClickedListener() {
            @Override
            public void onItemClicked(Object item, Row row) {

                ((BindInterface)item).startActivity(getActivity());

            }
        };
    }

    private class SearchRunnable implements Runnable {

        private volatile String searchQuery;

        public SearchRunnable() {
        }

        public void run() {
            if(searchQuery.length()>=3) {

                GenericUrl url = new GenericUrl("http://webservices.aptoide.com/webservices/3/listSearchApks/" + searchQuery + "/options=(repos=geniatechapps)/json");
                //HttpRequestFactory requestFactory = new UrlFetchTransport();
                //AndroidHttp.newCompatibleTransport().createRequestFactory();
                HttpRequestFactory requestFactory = new ApacheHttpTransport().createRequestFactory();

                try {
                    final HttpRequest httpRequest = requestFactory.buildGetRequest(url);
                    httpRequest.setParser(new GsonFactory().createJsonObjectParser());
                    Executor executor = Executors.newSingleThreadExecutor();

                    FutureTask<HttpResponse> future = new FutureTask<>(new Callable<HttpResponse>() {

                        public HttpResponse call() throws Exception {
                            final List<SearchJson.Results.Apks> apks = httpRequest.execute().parseAs(SearchJson.class).getResults().getApks();

                            mHandler.postDelayed(new LoadRowsRunnable(apks), SEARCH_DELAY_MS);

                            return null;
                        }
                    });

                    executor.execute(future);
/*                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setSearchQuery(String value) {
            this.searchQuery = value;
        }
    }

    public class LoadRowsRunnable implements Runnable {
        public List<SearchJson.Results.Apks> apks;

        public LoadRowsRunnable(List<SearchJson.Results.Apks> apks) {
            this.apks = apks;
        }

        @Override
        public void run() {
            loadRows(apks);
        }
    };
}
