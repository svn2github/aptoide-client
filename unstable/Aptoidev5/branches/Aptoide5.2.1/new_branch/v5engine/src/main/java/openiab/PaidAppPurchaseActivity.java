package openiab;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.paypal.android.sdk.payments.ProofOfPayment;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import openiab.webservices.PayProductRequestBase;
import openiab.webservices.json.IabPurchaseStatusJson;

/**
 * Created by asantos on 15-09-2014.
 */
public class PaidAppPurchaseActivity extends BasePurchaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_purchase);

        String user = getIntent().getStringExtra("user");


        if (user == null) {
            AccountManager.get(this).addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {

                    try {
                        String account = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);

                        if(account!=null){
                            ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + account);
                        }else {
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                }

            }, new Handler(Looper.getMainLooper()));
        } else {
            ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + user);
        }

        findViewById(R.id.buttonCancel).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {

    }
    @Override
    protected boolean makeExtraTestsOnPurchaseOk(IabPurchaseStatusJson iabPurchaseStatusJson) {
        return false;
    }
    @Override
    protected void processPaymentConfirmation(final ProofOfPayment confirmation) {
        DialogFragment df = new ProgressDialogFragment();
        df.show(getSupportFragmentManager(), "pleaseWaitDialog");
        df.setCancelable(false);

        spiceManager.execute(BuildPurchaseStatusRequest(confirmation), new PurchaseRequestListener() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Toast.makeText(Aptoide.getContext(), R.string.error_occured_retry_later, Toast.LENGTH_LONG).show();

                PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 1,
                        BuildIntentForAlarm(confirmation), PendingIntent.FLAG_UPDATE_CURRENT);
                manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60000, 60000, intent);

                dismissAllowingStateLoss();
            }
        });
    }
}
