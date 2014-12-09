package cm.aptoide.ptdev;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.services.HttpClientSpiceService;

/**
 * Created by asantos on 09-12-2014.
 */
public abstract class MoreBaseActivity extends ActionBarActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getFragment()).commit();
    }

    protected abstract MoreBaseFragment getFragment();

    public static class MoreBaseFragment extends Fragment {

        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            spiceManager.start(activity);

        }

        @Override
        public void onDetach() {
            super.onDetach();
            spiceManager.shouldStop();
        }
    }
}
