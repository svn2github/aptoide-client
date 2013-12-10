package cm.aptoide.ptdev;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import cm.aptoide.ptdev.adapters.SearchAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-12-2013
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchManager extends SherlockFragmentActivity {


    private CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SearchAdapter(this);
        setContentView(R.layout.page_search);
        Bundle args = new Bundle();
        String query;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(getIntent().hasExtra("search")){
            query = getIntent().getExtras().getString("search");
        } else {
            query = getIntent().getExtras().getString(android.app.SearchManager.QUERY).replaceAll("\\s{2,}|\\W", " ").trim();
            query = query.replaceAll("\\s{2,}", " ");
        }

        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,"cm.aptoide.ptdev.SuggestionProvider", 1);
        suggestions.saveRecentQuery(query, null);

        Toast.makeText(this, "Searched for : " + query, Toast.LENGTH_LONG).show();
        args.putString("query", query);

        Fragment fragment = new SearchFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragment).commit();
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




    public static class SearchFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>   {


        private MergeAdapter adapter;
        private String query;
        private CursorAdapter cursorAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            adapter = new MergeAdapter();
            View v = LayoutInflater.from(getSherlockActivity()).inflate(R.layout.separator_search, null);
            adapter.addView(v);

            cursorAdapter = new SearchAdapter(getSherlockActivity());
            adapter.addAdapter(cursorAdapter);
            query = getArguments().getString("query");
            getLoaderManager().initLoader(60, getArguments(), this);

        }



        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {



            return new SimpleCursorLoader(getSherlockActivity()) {
                @Override
                public Cursor loadInBackground() {
                    return new Database(Aptoide.getDb()).getSearchResults(args.getString("query"));
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            cursorAdapter.swapCursor(data);
            setListAdapter(adapter);
            setEmptyText("No results for " + query);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            cursorAdapter.swapCursor(null);
        }

    }

}
