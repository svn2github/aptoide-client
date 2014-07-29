package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.adapters.SearchAdapter;
import cm.aptoide.ptdev.adapters.SearchAdapter2;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.services.DownloadService;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.IconSizes;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.ListSearchApkRequest;
import cm.aptoide.ptdev.webservices.json.SearchJson;

import com.commonsware.cwac.merge.MergeAdapter;
import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-12-2013
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */



public class SearchManager extends ActionBarActivity implements SearchQueryCallback{



    private DownloadService downloadService;

    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_search);

        Bundle args = new Bundle();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if(savedInstanceState!=null) {
            query = savedInstanceState.getString("query");
            getSupportActionBar().setTitle("'"+query+"'");

            Log.d("SearchManager", "Read From savedInstanceState");
            return;
        }
        if(query==null) {
            Log.d("SearchManager", "Query has null");
            if (getIntent().hasExtra("search")) {
                query = getIntent().getExtras().getString("search");
            } else {
                query = getIntent().getExtras().getString(android.app.SearchManager.QUERY).replaceAll("\\s{2,}|\\W", " ").trim();
                query = query.replaceAll("\\s{2,}", " ");
            }
        }
        Log.d("SearchManager","Query:"+ query);

        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,Aptoide.getConfiguration().getSearchAuthority(), 1);
        suggestions.saveRecentQuery(query, null);

//        Toast.makeText(this, "Searched: " + query, Toast.LENGTH_LONG).show();
        args.putString("query", query);

        getSupportActionBar().setTitle("'"+query+"'");


        Fragment fragment = new SearchFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragment, "search").commit();

        bindService(new Intent(this, DownloadService.class), conn2, BIND_AUTO_CREATE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", query);
        Log.d("SearchManager","onSaveInstanceState:"+ query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if(i == android.R.id.home){
            finish();
        }else if(i == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setQuery(String query) {
        this.query = query;
    }


    public static class SearchFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{


        SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
        private List<SearchJson.Results.Apks> items = new ArrayList<SearchJson.Results.Apks>();
        private List<SearchJson.Results.Apks> items2 = new ArrayList<SearchJson.Results.Apks>();

        private SearchQueryCallback callback;
        private boolean loading;
        private View v2;
        private View searchLayout;
        private boolean hasUapks;
        //private View v2;

        @Override
        public void onStart() {
            super.onStart();
            manager.start(getActivity());
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(getActivity(), "X89WPPSKWQB2FT6B8F3X");
        }

        @Override
        public void onStop() {
            super.onStop();
            if(manager.isStarted()) manager.shouldStop();
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(getActivity());
        }

        private MergeAdapter adapter;
        private String query;
        int positionsub = 0;
        private SearchAdapter2 searchAdapterapks;
        private SearchAdapter2 searchAdapterapks2;

        private SearchAdapter cursorAdapter;
        private StoreActivity.Sort sort = StoreActivity.Sort.DOWNLOADS;
        private View v;
        TextView more;
        private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            adapter = new MergeAdapter();
            setRetainInstance(true);
            searchAdapterapks = new SearchAdapter2(getActivity(), items);
            searchAdapterapks2 = new SearchAdapter2(getActivity(), items2);

            query = getArguments().getString("query");

            searchLayout = LayoutInflater.from(getActivity()).inflate(R.layout.didyoumean_and_uapks_search_layout, null);
            v = ((SearchManager)getActivity()).getFooterView(R.layout.footer_search);
            v2 = ((SearchManager)getActivity()).getFooterView(R.layout.footer_search);

            pb = new ProgressBar(getActivity());
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            pb.setLayoutParams(params);

            adapter.addView(searchLayout);

            adapter.addAdapter(searchAdapterapks);

            adapter.addView(v);

            adapter.setActive(v, false);


            adapter.addAdapter(searchAdapterapks2);

            adapter.addView(pb);
            adapter.setActive(pb, false);

            adapter.addView(v2);
            adapter.setActive(v2, false);



            setHasOptionsMenu(true);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            position+=positionsub;
            Intent intent = new Intent(getActivity(), appViewClass);

            if(adapter.getItem(position) instanceof Cursor){
                intent.putExtra("id", id);
            }else{
                intent.putExtra("fromRelated", true);
                intent.putExtra("repoName", ((SearchJson.Results.Apks) adapter.getItem(position)).getRepo());
                intent.putExtra("md5sum", ((SearchJson.Results.Apks) adapter.getItem(position)).getMd5sum());
                intent.putExtra("download_from", "search_result");
            }
            startActivity(intent);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.callback = (SearchQueryCallback) activity;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            setListAdapter(null);
            cursorAdapter = new SearchAdapter(getActivity());
            adapter = new MergeAdapter();
            return new SimpleCursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    return new Database(Aptoide.getDb()).getSearchResults(args.getString("query"), sort);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
            cursorAdapter.swapCursor(data);
            v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_search, null);
            adapter.addView(v);
            adapter.addAdapter(cursorAdapter);

            Toast.makeText(Aptoide.getContext(), "Loading from database", Toast.LENGTH_LONG).show();

            if(isAdded()){

                TextView foundResults = (TextView) v.findViewById(R.id.results);
                more = (TextView) v.findViewById(R.id.more);
                if(data.getCount()>0){
                    foundResults.setText(getString(R.string.found_results, data.getCount()));
                }else{
                    foundResults.setText(getString(R.string.no_search_result, query));
                }
                setListAdapter(adapter);
                setListShown(true);
                setEmptyText(getString(R.string.no_search_result, query));



                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            int visibleItems = getListView().getLastVisiblePosition() - getListView().getFirstVisiblePosition();
                            if( ((SearchManager)getActivity()).isSearchMoreVisible() && visibleItems < data.getCount() ){
                                more.setVisibility(View.VISIBLE);
                                more.setOnClickListener(((SearchManager) getActivity()).getSearchListener());
                            }else{
                                more.setVisibility(View.GONE);
                            }
                        }catch (IllegalStateException e){

                        }

//                    Toast.makeText(getActivity(), "Last visible pos : " + getListView().getLastVisiblePosition() + " first visible :" + getListView().getFirstVisiblePosition(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getActivity(), String.valueOf(visibleItems < data.getCount()), Toast.LENGTH_LONG).show();


                    }
                });
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            cursorAdapter.swapCursor(null);
        }


        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        ProgressBar pb;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);







            //getListView().addFooterView(pb);

            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
            final ListSearchApkRequest request = new ListSearchApkRequest();


            request.setSearchString(query);


            if (!isNetworkAvailable(getActivity())) {
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                getLoaderManager().initLoader(60, bundle, SearchFragment.this);
                return;
            }


            manager.execute(request, query, DurationInMillis.ONE_HOUR, new RequestListener<SearchJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Bundle bundle = new Bundle();
                    bundle.putString("query", query);
                    getLoaderManager().initLoader(60, bundle, SearchFragment.this);
                }

                @Override
                public void onRequestSuccess(SearchJson searchJson) {
                    if (searchJson == null) {
                        return;
                    }

                    if ("FAIL".equals(searchJson.getStatus())) {
                        for (cm.aptoide.ptdev.model.Error error : searchJson.getErrors()) {
                            Integer errorCode = Errors.getErrorsMap().get(error.getCode());
                            String errorMsg;
                            if (errorCode != null) {
                                errorMsg = getString(errorCode);
                            } else {
                                errorMsg = error.getMsg();
                            }
                            if (getActivity() != null) {
                                getActivity().finish();
                            }

                            Toast.makeText(Aptoide.getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    /*u_items.clear();
                    u_items.addAll(searchJson.getResults().getU_Apks());
                    if(u_items.size()>0) {
                        adapter.addAdapter(searchAdapteruapks);
                        TextView foundUResults = (TextView) v.findViewById(R.id.resultsU);
                        foundUResults.setVisibility(View.VISIBLE);
                    }*/









                    LinearLayout didyoumeanContainer = (LinearLayout) searchLayout.findViewById(R.id.didyoumeancontainer);
                    LinearLayout usearchContainer = (LinearLayout) v.findViewById(R.id.container);
                    LinearLayout usearchContainer2 = (LinearLayout) v2.findViewById(R.id.container);



                    didyoumeanContainer.removeAllViews();

                    final String sizeString = IconSizes.generateSizeString(getActivity());

                    for (final String s : searchJson.getResults().getDidyoumean()) {
                        //Log.d("didyou", s);
                        TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.row_app_search_did_you_mean, null);

                        SpannableString content = new SpannableString(s);
                        content.setSpan(new UnderlineSpan(), 0, s.length(), 0);
                        tv.setText(content);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Search_Results_Clicked_On_Did_You_Mean_Recommendation");

                                Bundle args = new Bundle();

                                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), Aptoide.getConfiguration().getSearchAuthority(), 1);
                                suggestions.saveRecentQuery(s, null);

                                args.putString("query", s);

                                ((SearchManager) getActivity()).getSupportActionBar().setTitle("'" + s + "'");
                                callback.setQuery(s);
                                Fragment fragment = new SearchFragment();
                                fragment.setArguments(args);
                                getFragmentManager()
                                        .beginTransaction()
                                        .addToBackStack(null)
                                        .replace(R.id.fragContainer, fragment)
                                        .commit();
                            }
                        });
                        didyoumeanContainer.addView(tv);
                    }

                    fillUApks(searchJson, usearchContainer, sizeString);
                    fillUApks(searchJson, usearchContainer2, sizeString);


                    if(items.isEmpty()){
                        items.clear();
                        items.addAll(searchJson.getResults().getApks());
                    }

                    int getDidyoumeanSize = searchJson.getResults().getDidyoumean().size();
                    int uapksSize = searchJson.getResults().getU_Apks().size();


                    if(uapksSize > 0){
                        hasUapks = true;
                        adapter.setActive(v, true);
                    }



                    //adapter.notifyDataSetChanged();


                        if (getDidyoumeanSize > 0) {
                            adapter.setActive(searchLayout, true);
                        }else{
                            adapter.setActive(searchLayout, false);
                        }

                        //positionsub = -1;
                        //Log.d("SearchManager", "Adding Header View");


                        //getListView().addHeaderView(searchLayout, null, false);





                    if (isAdded()) {

//                        TextView foundResults = (TextView) v.findViewById(R.id.results);
                        more = (TextView) v.findViewById(R.id.more);
                        if (searchAdapterapks.getCount() > 0) {
                            //foundResults.setText(getString(R.string.found_results, searchAdapterapks.getCount()));
                        } else {
//                            foundResults.setText(getString(R.string.no_search_result, query));
                        }

                        setListShown(true);
                        setEmptyText(getString(R.string.no_search_result, query));


//                        Handler handler = new Handler();
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    int visibleItems = getListView().getLastVisiblePosition() - getListView().getFirstVisiblePosition();
//                                    if (((SearchManager) getActivity()).isSearchMoreVisible() && visibleItems < adapter.getCount()) {
//                                        more.setVisibility(View.VISIBLE);
//                                        more.setOnClickListener(((SearchManager) getActivity()).getSearchListener());
//                                    } else {
//                                        more.setVisibility(View.GONE);
//                                    }
//                                } catch (IllegalStateException e) {
//
//                                }
//                            }
//                        });
                    }
                    //setEmptyText(getString(R.string.no_search_result, query));

                    getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                            int lastInScreen = firstVisibleItem + visibleItemCount;
                            if((lastInScreen + 5 >= totalItemCount && !loading)){

                                loading = true;
                                ListSearchApkRequest request1 = new ListSearchApkRequest();
                                request1.setSearchString(query);
                                request1.setOffset(items.size() + items2.size());

                                adapter.setActive(pb, true);

                                manager.execute(request1, query + items.size() + items2.size(), DurationInMillis.ONE_HOUR, new RequestListener<SearchJson>() {

                                    @Override
                                    public void onRequestFailure(SpiceException spiceException) {
                                        loading = false;
                                        adapter.setActive(pb, false);
                                    }

                                    @Override
                                    public void onRequestSuccess(SearchJson searchJson) {
                                        items2.addAll(searchJson.getResults().getApks());
                                        if(!searchJson.getResults().getApks().isEmpty()){
                                            adapter.notifyDataSetChanged();
                                            loading = false;
                                        }else if(items2.size()> 9 && hasUapks){
                                            adapter.setActive(v2, true);
                                        }

                                        adapter.setActive(pb, false);

                                    }
                                });
                            }

                        }
                    });


                    if(getListView().getAdapter() == null){
                        setListAdapter(adapter);
                    }


                }



            });

            //getLoaderManager().restartLoader(60, getArguments(), this);

        }

        private void fillUApks(SearchJson searchJson, LinearLayout usearchContainer, String sizeString) {
            usearchContainer.removeAllViews();
            for (SearchJson.Results.Apks apk : searchJson.getResults().getU_Apks()) {
                View element = LayoutInflater.from(getActivity()).inflate(R.layout.row_app_search_result_other, usearchContainer, false);
                ImageView app_icon = (ImageView) element.findViewById(R.id.app_icon);

                String iconUrl = apk.getIcon();

                if (iconUrl.contains("_icon")) {
                    String[] splittedUrl = iconUrl.split("\\.(?=[^\\.]+$)");
                    iconUrl = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
                }
                ImageLoader.getInstance().displayImage(iconUrl, app_icon);

                TextView app_name = (TextView) element.findViewById(R.id.app_name);
                app_name.setText(apk.getName() + " - " + apk.getVername());

                usearchContainer.addView(element);

            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("offset", items.size());

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            //Log.d("Searchmanager", "OnDestroyView");
            getLoaderManager().destroyLoader(60);
        }

        @Override
        public void onResume() {
            super.onResume();
            ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("'"+query+"'");
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }

    }

    public boolean isSearchMoreVisible() {
        return true;
    }

    private View.OnClickListener getSearchListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Search_Results_Clicked_On_Search_More_Button");
                    String url = Aptoide.getConfiguration().getUriSearch() + query + "&q=" + Utils.filters(SearchManager.this);
                    //Log.d("TAG", "Searching for:" + url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    url = url.replaceAll(" ", "%20");
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (ActivityNotFoundException e){
                    Toast.makeText(Aptoide.getContext(), getString(R.string.error_occured), Toast.LENGTH_LONG).show();
                }

            }
        };
    }

    public View getFooterView(int res){
        View footer = LayoutInflater.from(this).inflate(res, null);
        Button search = (Button) footer.findViewById(R.id.search);
        search.setOnClickListener(getSearchListener());
        return footer;
    }

    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
    }

    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();
            BusProvider.getInstance().post(new DownloadServiceConnected());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {

        if (downloadService !=null){
            unbindService(conn2);
        }
        super.onDestroy();
    }
}
