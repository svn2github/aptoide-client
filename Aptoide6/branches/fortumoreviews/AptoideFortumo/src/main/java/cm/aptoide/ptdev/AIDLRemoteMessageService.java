package cm.aptoide.ptdev;

import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by asantos on 02-03-2015.
 */

public class AIDLRemoteMessageService extends IRemoteFortumoPayment.Stub {

    private final FortumoPaymentService service;

    public AIDLRemoteMessageService(FortumoPaymentService service) {
        this.service = service;
    }

    @Override
    public void getMessage(String ID, boolean isConsumable, int userId,
                             String PAYMENTSERVICE_ID,
                             String PAYMENTSERVICE_INAPPSECRET,
                             String PAYMENTSERVICE_NAME,
                             Messenger msger) throws RemoteException {
        service.getStringForRemoteService(ID, isConsumable, userId,
         PAYMENTSERVICE_ID,
         PAYMENTSERVICE_INAPPSECRET,
         PAYMENTSERVICE_NAME,
         msger);
    }
}
