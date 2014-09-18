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
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.paypal.android.sdk.payments.ProofOfPayment;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import openiab.webservices.BasePurchaseStatusRequest;
import openiab.webservices.PaidAppPurchaseStatusRequest;
import openiab.webservices.PayProductRequestBase;
import openiab.webservices.PaypalPurchaseAuthorizationRequest;
import openiab.webservices.json.IabPurchaseStatusJson;
import openiab.webservices.json.PaymentServices;

/**
 * Created by asantos on 15-09-2014.
 */
public class PaidAppPurchaseActivity extends BasePurchaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_app_purchase);

        String user = getIntent().getStringExtra("user");
        aptoideProductId = getIntent().getIntExtra("ID",0);
        final ArrayList<PaymentServices> PaymentServicesList = getIntent().getParcelableArrayListExtra("PaymentServices");

        if (user == null) {
            AccountManager.get(this).addAccount(Aptoide.getConfiguration().getAccountType(), AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {

                    try {
                        String account = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);

                        if(account!=null){
                            updateUI(account,PaymentServicesList);
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
            updateUI(user,PaymentServicesList);
        }

        findViewById(R.id.buttonCancel).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    private void updateUI(String user, ArrayList<PaymentServices> paymentServicesList){
        ((TextView) findViewById(R.id.username)).setText(getString(R.string.account) + ": " + user);
        LinearLayout paymentMethodsLayout = (LinearLayout) findViewById(R.id.payment_methods);
        paymentMethodsLayout.removeAllViews();
        Button button;

        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (paymentServicesList!=null && paymentServicesList.isEmpty()) {
            TextView noPaymentsFound = new TextView(this);
            noPaymentsFound.setText(R.string.no_payments_available);
            paymentMethodsLayout.addView(noPaymentsFound);
        }else if(paymentServicesList !=null ){
            for (PaymentServices service : paymentServicesList) {

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
                                spiceManager.execute(request, "authorization-" + token, DurationInMillis.ONE_DAY, hasAuthorization);

                            } else if ("single".equals(type.getReqType())) {

                                button = (Button) LayoutInflater.from(this).inflate(R.layout.button_visa, null);

                                button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                OnPaypalClick onPaypalClick = new OnPaypalClick();
                                onPaypalClick.setCurrency(service.getCurrency());
                                onPaypalClick.setPrice(service.getPrice());
                                onPaypalClick.setTax(service.getTaxRate());
                                onPaypalClick.setRepo(repo);

                                onPaypalClick.setDescription("TODO description Title!!!");


                                button.setOnClickListener(onPaypalClick);
                                paymentMethodsLayout.addView(button);

                                if(paymentServicesList.size() == 1 && service.getTypes().size() == 1){
                                    onPaypalClick.onClick(null);
                                }

                            }
                        }
                        break;
                    case UNITEL_CODE:

                        if(telephonyManager!=null && telephonyManager.getSimState()== TelephonyManager.SIM_STATE_READY){



                            for (PaymentServices.PaymentType type : service.getTypes()) {
                                button = (Button) LayoutInflater.from(this).inflate(R.layout.button_carrier, null);

                                if (button != null) {
                                    button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                    paymentMethodsLayout.addView(button);
                                    DecimalFormat df = new DecimalFormat("######.#");
                                    button.setOnClickListener(new UnitelPurchaseListener(getSupportFragmentManager(),
                                            String.valueOf(service.getPrice()),
                                            telephonyManager.getSimOperatorName(),
                                            service.getName(),
                                            service.getId(), telephonyManager.getSubscriberId(),
                                            service.getCurrency(),
                                            df.format(service.getPrice())));
                                }
                            }

                        }
                        break;
                }


            }
        }
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

    @Override
    protected BasePurchaseStatusRequest BuildPurchaseStatusRequest() {
        return new PaidAppPurchaseStatusRequest();
    }
}
