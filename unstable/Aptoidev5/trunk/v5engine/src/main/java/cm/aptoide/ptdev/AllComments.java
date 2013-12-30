package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import cm.aptoide.ptdev.events.BusProvider;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;

/**
 * Created by rmateus on 26-12-2013.
 */
public class AllComments extends ActionBarActivity {

    private SpiceManager spiceManager = new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_comments);
    }

    public SpiceManager getSpice() {
        return spiceManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
    }
}
