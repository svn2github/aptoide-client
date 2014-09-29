package cm.aptoide.ptdev.webservices.timeline;

import android.util.Log;
import android.widget.Toast;

import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public abstract class TimelineRequestListener<E> implements RequestListener<E> {

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Log.d("Timeline-Request", "onRequestFailure - " + spiceException.getMessage());
        // No Internet connection
        if (spiceException instanceof NoNetworkException) {
            Toast.makeText(Aptoide.getContext(), "Error: Please turn on your Internet.", Toast.LENGTH_LONG).show();
        }
        // Server unavailable
        else if (spiceException instanceof NetworkException) {
            Toast.makeText(Aptoide.getContext(), "Error: Server unreachable.", Toast.LENGTH_LONG).show();
        }
    }

    protected abstract void caseOK(E response);

    @Override
    public  void onRequestSuccess(E response){
        String status = ((GenericResponse)response).getStatus();
        if (status.equals("OK"))
            caseOK(response);

    };
}
