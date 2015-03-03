package cm.aptoidetv.pt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import cm.aptoidetv.pt.WebServices.HttpService;
import cm.aptoidetv.pt.WebServices.RequestTV;
import cm.aptoidetv.pt.WebServices.Response;

/**
 * Created by asantos on 16-12-2014.
 */
public class MainFail extends Activity implements RequestsTvListener {

    private SpiceManager manager = new SpiceManager(HttpService.class);

    @Override
    public void onStart() {
        super.onStart();
        manager.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.shouldStop();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemePicker.setThemePicker(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainfail);
    }
    @Override
    public void onSuccess() {
        findViewById(R.id.Loading_fail).setVisibility(View.GONE);
        startActivity(new Intent(this, MainActivity.class).putExtra(MainActivity.ARGS_SKIP,true));
        finish();
    }

    @Override
    public void onFailure() {
        findViewById(R.id.Loading_fail).setVisibility(View.GONE);
    }

    public void RetryClick(View view){
        findViewById(R.id.Loading_fail).setVisibility(View.VISIBLE);
        final RequestTV request = new RequestTV(this);

        RequestListener<Response> requestListener = new RequestListener<Response>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                onFailure();
            }

            @Override
            public void onRequestSuccess(Response response) {
                onSuccess();
            }
        };
        manager.execute(request, "store", DurationInMillis.ALWAYS_RETURNED, requestListener);
    }
}
