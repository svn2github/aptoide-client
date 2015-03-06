package cm.aptoidetv.pt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by brutus on 11-12-2013.
 */
public class AccountAuthenticatorService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            AccountAuthenticator authenticator = new AccountAuthenticator(this);
            return authenticator.getIBinder();
        }
}
