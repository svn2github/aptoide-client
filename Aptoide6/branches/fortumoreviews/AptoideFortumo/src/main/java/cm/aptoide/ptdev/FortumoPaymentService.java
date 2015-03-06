package cm.aptoide.ptdev;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
/**
 * Created by asantos on 02-03-2015.
 */
public class FortumoPaymentService extends Service {
    private static final boolean LOG = false;
    private static final String AptoideFortumoPaymentService = "Aptoide.FortumoPaymentService";
    private static final String TAG = "pois";

    @Override
    public IBinder onBind(Intent intent) {
        if(LOG){Log.d(TAG,"FortumoPaymentService onbind");}
        if(AptoideFortumoPaymentService.equals(intent.getAction())) {
            if(LOG){Log.d(TAG,"The AIDLMessageService was binded.");}
            return new AIDLRemoteMessageService(this);
        }
        return null;
    }

    public void getStringForRemoteService(String ID, boolean isConsumable, int userId,
                                            String PAYMENTSERVICE_ID,
                                            String PAYMENTSERVICE_INAPPSECRET,
                                            String PAYMENTSERVICE_NAME,
                                            Messenger msger){
        if(LOG){Log.d(TAG,"FortumoPaymentService getStringForRemoteService");}
        if(LOG){Log.d(TAG,"FortumoPaymentService getStringForRemoteService ID: "+ID);}
        if(LOG){Log.d(TAG,"FortumoPaymentService getStringForRemoteService userId: "+userId);}
        Intent i = new Intent(this,FortumoPaymentActivity.class);
        i.putExtra(FortumoPaymentActivity.EXTRA_ID,ID);
        i.putExtra(FortumoPaymentActivity.EXTRA_ISCONSUMABLE,isConsumable);
        i.putExtra(FortumoPaymentActivity.EXTRA_USER_ID,userId);
        i.putExtra(FortumoPaymentActivity.EXTRA_PAYMENTSERVICE_ID,PAYMENTSERVICE_ID);
        i.putExtra(FortumoPaymentActivity.EXTRA_PAYMENTSERVICE_NAME,PAYMENTSERVICE_NAME);
        i.putExtra(FortumoPaymentActivity.EXTRA_PAYMENTSERVICE_INAPPSECRET,PAYMENTSERVICE_INAPPSECRET);
        i.putExtra(FortumoPaymentActivity.EXTRA_PARCELABLE_MSGER,msger);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);

    }
}
