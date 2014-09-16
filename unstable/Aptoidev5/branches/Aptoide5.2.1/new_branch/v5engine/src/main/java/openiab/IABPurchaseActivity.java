package openiab;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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
import openiab.webservices.IabPurchaseStatusRequest;
import openiab.webservices.PayProductRequestBase;
import openiab.webservices.json.IabPurchaseStatusJson;

public class IABPurchaseActivity extends BasePurchaseActivity{
    private int apiVersion;
    private String type;
    private String sku;
    private String developerPayload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_purchase);


        final Intent intent = getIntent();
        developerPayload = intent.getStringExtra("developerPayload");
        apiVersion = intent.getIntExtra("apiVersion", 3);
        sku = intent.getStringExtra("sku");

        //sku = "com.aptoide.partners";

        //token =  "27286b943179065fcef3b6adcafe8680d8515e4652010602c45f6";

        String user = intent.getStringExtra("user");

        if (user == null) {
            AccountManager.get(this).addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {

                    try {
                        String account = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);

                        if(account!=null){
                            ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + account);
                            Intent billingIntent = new Intent(IABPurchaseActivity.this, BillingService.class);
                            //bindService(billingIntent, mConnection, BIND_AUTO_CREATE);
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
            Intent billingIntent = new Intent(this, BillingService.class);
            //bindService(billingIntent, mConnection, BIND_AUTO_CREATE);

        }

        findViewById(R.id.buttonCancel).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AptoideinAppBilling", "Purchase Cancelled");
                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_USER_CANCELED);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected boolean makeExtraTestsOnPurchaseOk(IabPurchaseStatusJson iabPurchaseStatusJson) {
        return !sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0));
    }
/*
    @Override
    protected boolean TestifPurchased(IBinder service) {
        try {
            Bundle bundle = ((BillingBinder)service).getPurchases(apiVersion, packageName, type, "");

            for(String json : bundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")){

                if(sku.equals(json)){
                    return true;
                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e){
            finish();
            return false;
        }
        return false;
    }

    @Override
    protected IabSkuDetailsRequest AddWhatevertoConnectionRequest(IabSkuDetailsRequest request) {
        ArrayList<String> item_list = new ArrayList<String>();
        item_list.add(sku);
        request.setApiVersion(Integer.toString(apiVersion));

        for (String itemId : item_list) {
            request.addToSkuList(itemId);
            Log.d("Aptoide-Binder", "Sku details request for " + itemId);
        }
        return request;
    }*/

    @Override
    protected void requestsetExtra(PayProductRequestBase pprb) {
        pprb.setApiVersion(String.valueOf(apiVersion));
        pprb.setDeveloperPayload(developerPayload);
    }

    @Override
    protected void processPaymentConfirmation(final ProofOfPayment confirmation) {
        final IabPurchaseStatusRequest purchaseStatus = BuildPurchaseStatusRequest(confirmation);

        purchaseStatus.setDeveloperPayload(developerPayload);
        purchaseStatus.setApiVersion(String.valueOf(apiVersion));

        DialogFragment df = new ProgressDialogFragment();
        df.show(getSupportFragmentManager(), "pleaseWaitDialog");
        df.setCancelable(false);

        spiceManager.execute(purchaseStatus, new PurchaseRequestListener() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Toast.makeText(Aptoide.getContext(), R.string.error_occured_retry_later, Toast.LENGTH_LONG).show();

                Intent i = BuildIntentForAlarm(confirmation);
                i.putExtra("apiVersion", apiVersion);
                i.putExtra("developerPayload", developerPayload);

                PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
                manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60000, 60000, intent);

                dismissAllowingStateLoss();
            }
        });
    }
}
