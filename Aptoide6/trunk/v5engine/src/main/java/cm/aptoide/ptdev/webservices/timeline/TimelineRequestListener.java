package cm.aptoide.ptdev.webservices.timeline;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import cm.aptoide.ptdev.fragments.GenericResponse;

/**
 * Created by asantos on 24-09-2014.
 */
public class TimelineRequestListener<E> implements RequestListener<E> {

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        caseFAIL();
/*        Log.d("Timeline-Request", "onRequestFailure - " + spiceException.getMessage());
        // No Internet connection
        if (spiceException instanceof NoNetworkException) {
            Toast.makeText(Aptoide.getContext(), "Error: Please turn on your Internet.", Toast.LENGTH_LONG).show();
        }
        // Server unavailable
        else if (spiceException instanceof NetworkException) {
            Toast.makeText(Aptoide.getContext(), "Error: Server unreachable.", Toast.LENGTH_LONG).show();
        }*/
    }

    protected void caseOK(E response){};
    protected void caseFAIL(){};


    @Override
    public  void onRequestSuccess(E response){
        String status = ((GenericResponse)response).getStatus();
        if (status.equals("OK"))
            caseOK(response);
        else
            caseFAIL();
    };
}
