package cm.aptoide.ptdev;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import cm.aptoide.ptdev.adapters.RollBackAdapter;
import cm.aptoide.ptdev.adapters.RollbackSectionListAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;

import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;



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

        RollbackSectionListAdapter adapter = new RollbackSectionListAdapter(getLayoutInflater(), rollBackAdapter);

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
        } else if(i == R.id.menu_clear_rollback){
            FlurryAgent.logEvent("Rollback_Cleared_Rollback_List");
            new Database(Aptoide.getDb()).deleteRollbackItems();
            getSupportLoaderManager().restartLoader(17, null, this);
        } else if( i == R.id.menu_SendFeedBack){
            FeedBackActivity.screenshot(this);
            startActivity(new Intent(this,FeedBackActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_rollback_activity, menu);

        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
