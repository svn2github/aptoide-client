package cm.aptoide.ptdev;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import cm.aptoide.ptdev.adapters.RollBackAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.squareup.otto.Subscribe;
import pl.polidea.sectionedlist.SectionListAdapter;


public class RollbackActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private RollBackAdapter rollBackAdapter;

    @Subscribe
    public void onInstalledApkEvent(InstalledApkEvent event) {
        refreshRollbackList();
    }

    @Subscribe
    public void onUnistalledApkEvent(UnInstalledApkEvent event) {
        refreshRollbackList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_rollback);

        rollBackAdapter = new RollBackAdapter(this);


        ListView lView = (ListView) findViewById(R.id.rollback_list);
        lView.setDivider(null);

        SectionListAdapter adapter = new SectionListAdapter(getLayoutInflater(), rollBackAdapter);

        lView.setAdapter(adapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.rollback));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new SimpleCursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                return new Database(Aptoide.getDb()).getRollbackActions();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        rollBackAdapter.swapCursor(cursor);
        if(cursor.getCount()==0){
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.empty).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        rollBackAdapter.swapCursor(null);
    }

    public void refreshRollbackList() {
        getSupportLoaderManager().restartLoader(17, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        getSupportLoaderManager().restartLoader(17, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

    }
}
