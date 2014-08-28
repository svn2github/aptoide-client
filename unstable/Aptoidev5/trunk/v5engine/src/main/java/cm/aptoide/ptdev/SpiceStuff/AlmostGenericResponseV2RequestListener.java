package cm.aptoide.ptdev.SpiceStuff;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.HashMap;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.events.AppViewRefresh;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.webservices.Errors;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by asantos on 29-07-2014.
 */
public abstract class AlmostGenericResponseV2RequestListener implements RequestListener<GenericResponseV2>{

    FragmentActivity a;

    public AlmostGenericResponseV2RequestListener(FragmentActivity a){
        this.a=a;
    }

    private final void dismiss(){
        ProgressDialogFragment pd = (ProgressDialogFragment) a.getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
        if(pd!=null){
            pd.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Toast.makeText(Aptoide.getContext(), a.getString(R.string.error_occured), Toast.LENGTH_LONG).show();
        dismiss();
    }

    public abstract void CaseOK();

    @Override
    public void onRequestSuccess(GenericResponseV2 genericResponse) {
        dismiss();
        if("OK".equals(genericResponse.getStatus())){
            CaseOK();
        }else{
            HashMap<String, Integer> errorsMap = Errors.getErrorsMap();
            Integer stringId;
            String message;
            for(cm.aptoide.ptdev.model.Error error :  genericResponse.getErrors()){
                stringId = errorsMap.get( error.getCode() );
                if(stringId != null) {
                    message = a.getString( stringId );
                } else {
                    message = error.getMsg();
                }
                Toast.makeText(Aptoide.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }

    }
}
