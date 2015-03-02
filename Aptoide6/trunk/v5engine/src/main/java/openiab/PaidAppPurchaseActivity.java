package openiab;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.paypal.android.sdk.payments.ProofOfPayment;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import fortumo.FortumoPaymentActivity;
import fortumo.PaymentStatusReceiver;
import openiab.webservices.BasePurchaseStatusRequest;
import openiab.webservices.IabPurchaseStatusRequest;
import openiab.webservices.PaidAppPurchaseStatusRequest;
import openiab.webservices.PaypalPurchaseAuthorizationRequest;
import openiab.webservices.json.IabPurchaseStatusJson;
import openiab.webservices.json.PaymentServices;

/**
 * Created by asantos on 15-09-2014.
 */
public class PaidAppPurchaseActivity extends BasePurchaseActivity{
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_purchase);

        user = getIntent().getStringExtra("user");
        aptoideProductId = getIntent().getIntExtra("ID",0);
        final ArrayList<PaymentServices> paymentServicesList = getIntent().getParcelableArrayListExtra("PaymentServices");

        if (user == null) {
            AccountManager.get(this).addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        String account = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);

                        if(account!=null){
                            user = account;
                            updateUI(aptoideProductId,account,paymentServicesList);
                            ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + account);
                        }else {
                            Log.d("pois", "PaidAppPurchaseActivity finish no else do AccountManager.");
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("pois", "PaidAppPurchaseActivity finish na exception AccountManager.");
                        finish();
                    }

                }

            }, new Handler(Looper.getMainLooper()));

        } else {
            updateUI(aptoideProductId,user,paymentServicesList);
        }

        findViewById(R.id.buttonCancel).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, getIntent());
                Log.d("pois", "PaidAppPurchaseActivity finish no cancel button.");
                finish();
            }
        });
    }

    private void updateUI(final int aptoideProductId,String user, ArrayList<PaymentServices> paymentServicesList){
        ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + user);
        LinearLayout paymentMethodsLayout = (LinearLayout) findViewById(R.id.payment_methods);
        paymentMethodsLayout.removeAllViews();
        Button button;

        if (paymentServicesList!=null && paymentServicesList.isEmpty()) {
            TextView noPaymentsFound = new TextView(this);
            noPaymentsFound.setText(R.string.no_payments_available);
            paymentMethodsLayout.addView(noPaymentsFound);
        }else if(paymentServicesList !=null ){
            for (final PaymentServices service : paymentServicesList) {
                int serviceCode = servicesList.get(service.getShort_name());

                switch (serviceCode) {
                    case PAYPAL_CODE:
                        for (PaymentServices.PaymentType type : service.getTypes()) {
                            if ("future".equals(type.getReqType())) {

                                button = (Button) LayoutInflater.from(this).inflate(R.layout.button_paypal, null);
                                button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                paymentMethodsLayout.addView(button);

                                PaypalPurchaseAuthorizationRequest request = new PaypalPurchaseAuthorizationRequest();
                                request.setToken(token);
                                HasAuthorization hasAuthorization = new HasAuthorization(button);
                                hasAuthorization.setCurrency(service.getCurrency());
                                hasAuthorization.setPrice(service.getPrice());
                                hasAuthorization.setTax(service.getTaxRate());
                                spiceManager.execute(request, hasAuthorization);

                            } else if ("single".equals(type.getReqType())) {

                                button = (Button) LayoutInflater.from(this).inflate(R.layout.button_visa, null);

                                button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                OnPaypalClick onPaypalClick = new OnPaypalClick();
                                onPaypalClick.setCurrency(service.getCurrency());
                                onPaypalClick.setPrice(service.getPrice());
                                onPaypalClick.setTax(service.getTaxRate());
                                onPaypalClick.setRepo(repo);

                                onPaypalClick.setDescription( Aptoide.getConfiguration().getMarketName() + " Paid App - " + packageName);

                                button.setOnClickListener(onPaypalClick);
                                paymentMethodsLayout.addView(button);

                                if(paymentServicesList.size() == 1 && service.getTypes().size() == 1){
                                    onPaypalClick.onClick(null);
                                }

                            }
                        }
                        break;
                    case UNITEL_CODE:
                        caseUNITEL(service,paymentMethodsLayout);
                        break;
                    /*case FORTUMO_CODE:
                        caseFortumo(aptoideProductId,service,paymentMethodsLayout);
                        break;
                     */
                }
            }
        }
    }

/*    public void FortumoCallBack(boolean ok,boolean isconsumable,String payment_code){
        if(ok) {
            Log.d("pois", "FortumoCallBack called!");
            BasePurchaseStatusRequest request = isconsumable ? new IabPurchaseStatusRequest() : new PaidAppPurchaseStatusRequest();
            request.setPayKey(payment_code);
            spiceManager.execute(request, new RequestListener<IabPurchaseStatusJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Log.d("pois", "RequestListener onRequestFailure");
                    dismissAllowingStateLoss();
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                    Log.d("pois", "RequestListener onRequestSuccess");
                    dismissAllowingStateLoss();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }else{
            dismissAllowingStateLoss();
            Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
        }
    }*/

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
                        buildIntentForAlarm(confirmation, "paidapk"), PendingIntent.FLAG_UPDATE_CURRENT);
                manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60000, 60000, intent);

                dismissAllowingStateLoss();
            }

            @Override
            public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                Intent intent = new Intent();
                dismissAllowingStateLoss();
                if (iabPurchaseStatusJson != null && "OK".equals(iabPurchaseStatusJson.getStatus())) {
                    setResult(RESULT_OK, intent);
                    Log.d("pois", "PaidAppPurchaseActivity finish no processPaymentConfirmation.");
                    finish();
                } else {
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected BasePurchaseStatusRequest BuildPurchaseStatusRequest() {
        return new PaidAppPurchaseStatusRequest();
    }
}
