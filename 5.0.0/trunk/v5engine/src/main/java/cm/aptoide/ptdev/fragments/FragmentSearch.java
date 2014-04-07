package cm.aptoide.ptdev.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.StoreActivity;
import cm.aptoide.ptdev.adapters.SearchAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.downloadmanager.Utils;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.commonsware.cwac.merge.MergeAdapter;

/**
 * Created by tdeus on 3/21/14.
 */
public class FragmentSearch extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>   {


    private MergeAdapter adapter;
    private String query;
    private CursorAdapter cursorAdapter;
    private StoreActivity.Sort sort = StoreActivity.Sort.DOWNLOADS;
    private View v;
    TextView more;
    private Class appViewClass = Aptoide.getConfiguration().getAppViewActivityClass();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MergeAdapter();
        v = LayoutInflater.from(getActivity()).inflate(R.layout.separator_search, null);
        adapter.addView(v);

        cursorAdapter = new SearchAdapter(getActivity());
        adapter.addAdapter(cursorAdapter);
        query = getArguments().getString("query");
        setHasOptionsMenu(true);




    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_categories, menu);
        menu.findItem(R.id.show_all).setVisible(false);
        menu.findItem(R.id.download).setChecked(true);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        if(id == R.id.name){
            setListShown(false);

            sort = StoreActivity.Sort.NAME;
        }else if(id == R.id.date){
            setListShown(false);

            sort = StoreActivity.Sort.DATE;
        }else if(id == R.id.download){
            setListShown(false);

            sort = StoreActivity.Sort.DOWNLOADS;
        }else if(id == R.id.rating){
            setListShown(false);

            sort = StoreActivity.Sort.RATING;
        }else if(id == R.id.price){
            setListShown(false);

            sort = StoreActivity.Sort.PRICE;
        }


        getLoaderManager().restartLoader(60, getArguments(), this);
        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(getActivity(), appViewClass);
        i.putExtra("id", id);
        startActivity(i);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

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
                int visibleItems = getListView().getLastVisiblePosition() - getListView().getFirstVisiblePosition();

//                    Toast.makeText(getActivity(), "Last visible pos : " + getListView().getLastVisiblePosition() + " first visible :" + getListView().getFirstVisiblePosition(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getActivity(), String.valueOf(visibleItems < data.getCount()), Toast.LENGTH_LONG).show();

                if(visibleItems < data.getCount()){
                    more.setVisibility(View.VISIBLE);
                    more.setOnClickListener(getSearchListener());
                }else{
                    more.setVisibility(View.GONE);
                }
            }
        });





    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));

        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer_search, null);
        Button search = (Button) footer.findViewById(R.id.search);
        search.setOnClickListener(getSearchListener());

        getListView().addFooterView(footer);
        getLoaderManager().initLoader(60, getArguments(), this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        getLoaderManager().destroyLoader(60);
    }

    private View.OnClickListener getSearchListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String url = Aptoide.getConfiguration().getUriSearch() + query + "&q=" + Utils.filters(getActivity());
                    Log.d("TAG", "Searching for:" + url);
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
}