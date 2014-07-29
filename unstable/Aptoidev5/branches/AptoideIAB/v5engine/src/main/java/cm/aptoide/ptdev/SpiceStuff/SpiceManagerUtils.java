package cm.aptoide.ptdev;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;

/**
 * Created by asantos on 28-07-2014.
 */
public class SpiceManagerUtils {

    public interface MegaRequestListenerCallBack{
        int addRequest();
        void removeRequest(int id);
    }

    public abstract class MegaRequestListener<E> implements RequestListener<E>{

        int id;
        MegaRequestListenerCallBack callBack;
        
        public MegaRequestListener(MegaRequestListenerCallBack callBack){
            this.callBack=callBack;
            id=callBack.addRequest();
        }
        
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            callBack.removeRequest(id);
        }

        @Override
        public void onRequestSuccess(E o) {
            callBack.removeRequest(id);
        }
    }
    
}
