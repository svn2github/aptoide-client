package openiab;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.retry.RetryPolicy;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;

import org.json.JSONException;
import org.onepf.oms.BillingBinder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.IRemoteFortumoPayment;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import openiab.webservices.BasePurchaseStatusRequest;
import openiab.webservices.IabPurchaseStatusRequest;
import openiab.webservices.PaidAppPurchaseStatusRequest;
import openiab.webservices.PayProductRequestBase;
import openiab.webservices.PayProductRequestPayPal;
import openiab.webservices.PayProductRequestUnitel;
import openiab.webservices.PaypalPurchaseAuthorizationRequest;
import openiab.webservices.json.IabPurchaseStatusJson;
import openiab.webservices.json.IabSimpleResponseJson;
import openiab.webservices.json.PaymentServices;

public abstract class BasePurchaseActivity extends ActionBarActivity implements Callback {
    private static final boolean LOG = false;
    private static final String TAG = "pois";

    static HashMap<String, Integer> servicesList = new HashMap<String, Integer>();
    protected static final String PAYPAL_NAME = "paypal";
    protected static final String UNITEL_NAME = "unitel";
    protected static final String FORTUMO_NAME = "fortumo";

    protected static final int PAYPAL_CODE = 1;
    protected static final int UNITEL_CODE = 2;
    protected static final int FORTUMO_CODE = 3;

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AW47wxAycZoTcXd5KxcJPujXWwImTLi-GNe3XvUUwFavOw8Nq4ZnlDT1SZIY";

    //private static final String CONFIG_CLIENT_ID = "AQ7o2RBHX3UxiM3xhHccETYWVVLLU0nD7GXxsmQg2MhRajAZztqHeidrPgqr";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private static PayPalConfiguration config;

    static {
        servicesList.put(PAYPAL_NAME, PAYPAL_CODE);
        servicesList.put(UNITEL_NAME, UNITEL_CODE);
        servicesList.put(FORTUMO_NAME, FORTUMO_CODE);

        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(CONFIG_CLIENT_ID)
                        // The following are only used in PayPalFuturePaymentActivity.
                .merchantName("Aptoide");
    }


    protected String packageName;

    AsyncTask<Void, Void, Bundle> execute;
    protected int aptoideProductId;
    protected SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    protected String simcc;
    protected String repo;
    protected ValuesToVerify valuesToVerify;
    private boolean alreadyRegistered = false;
    //protected boolean isBound;
    protected String token;

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start(this);
        FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
        FlurryAgent.onEndSession(this);
    }



    public class HasAuthorization implements RequestListener<IabSimpleResponseJson> {

        private String currency;

        private double price;
        private double tax;
        private Button button;

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public HasAuthorization(Button button) {

            this.button = button;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            OnPaypalFutureClick onPaypalFutureClick = new OnPaypalFutureClick();
            onPaypalFutureClick.setProductId(String.valueOf(aptoideProductId));
            button.setOnClickListener(onPaypalFutureClick);
        }

        @Override
        public void onRequestSuccess(IabSimpleResponseJson iabPurchaseStatusJson) {
            OnPaypalFutureClick onPaypalFutureClick = new OnPaypalFutureClick();
            onPaypalFutureClick.setCurrency(this.currency);
            onPaypalFutureClick.setPrice(this.price);
            onPaypalFutureClick.setTax(this.tax);
            alreadyRegistered = iabPurchaseStatusJson.getStatus().equals("OK");
            onPaypalFutureClick.setProductId(String.valueOf(aptoideProductId));
            button.setOnClickListener(onPaypalFutureClick);
        }

    }
    protected void requestsetExtra(PayProductRequestBase pprb){
        return;
    }

    @Override
    public void onClick(int payType, String imsi,  String price, String currency) {
        DialogFragment df = AptoideDialog.pleaseWaitDialog();

        df.setCancelable(false);
        df.show(getSupportFragmentManager(), "pleaseWaitDialog");
        PayProductRequestUnitel requestUnitel = new PayProductRequestUnitel();
        requestUnitel.setProductId(String.valueOf(aptoideProductId));
        requestUnitel.setPayType(String.valueOf(payType));
        requestUnitel.setToken(token);
        requestUnitel.setImsi(imsi);
        requestUnitel.setPrice(price);
        requestUnitel.setCurrency(currency);
        requestUnitel.setRepo(repo);
        requestsetExtra(requestUnitel);
        requestUnitel.setRetryPolicy(noRetryPolicy);
        spiceManager.execute(requestUnitel, new PurchaseRequestListener());

    }

    @Override
    public void onCancel() {

    }

    public class ValuesToVerify {

        public double tax;
        public double price;
        public String currency;
        public String repo;

    }

    public class OnPaypalClick implements View.OnClickListener {

        private String description;
        private double price;
        private String currency;
        private double tax;
        private String repo;

        public void setPrice(double price) {
            this.price = price;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public void onClick(View view) {
            valuesToVerify = new ValuesToVerify();
            valuesToVerify.currency = this.currency;
            valuesToVerify.price = this.price;
            valuesToVerify.tax = this.tax;
            valuesToVerify.repo = this.repo;
            PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(price), currency, description,
                    PayPalPayment.PAYMENT_INTENT_SALE);

            Intent intent = new Intent(BasePurchaseActivity.this, PaymentActivity.class);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            FlurryAgent.logEvent("Purchase_Page_Clicked_On_Paypal_Button");
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public double getTax() {
            return tax;
        }

        public void setRepo(String repo) {
            this.repo = repo;
        }
    }


    public class OnPaypalFutureClick implements View.OnClickListener {

        private String productId;
        private String currency;
        private double price;
        private double tax;

        public void setProductId(String price) {
            this.productId = price;
        }

        @Override
        public void onClick(View view) {
            valuesToVerify = new ValuesToVerify();
            valuesToVerify.currency = this.currency;
            valuesToVerify.price = this.price;
            valuesToVerify.tax = this.tax;
            valuesToVerify.repo = repo;
            if (alreadyRegistered) {
                final String correlationId = PayPalConfiguration.getApplicationCorrelationId(BasePurchaseActivity.this);
                PayProductRequestPayPal request = new PayProductRequestPayPal();
                request.setToken(token);
                request.setRepo(repo);
                request.setProductId(productId);
                request.setPrice(String.valueOf(price));
                request.setCurrency(currency);
                if(simcc!=null && simcc.length()>0){
                    request.setSimCountryCode(simcc);
                }
                request.setCorrelationId(correlationId);
                request.setRetryPolicy(noRetryPolicy);
                requestsetExtra(request);

                DialogFragment df = new ProgressDialogFragment();
                df.show(getSupportFragmentManager(), "pleaseWaitDialog");
                df.setCancelable(false);
                spiceManager.execute(request, new PurchaseRequestListener());
            } else {
                Intent ppIntent = new Intent(BasePurchaseActivity.this, PayPalFuturePaymentActivity.class);
                startActivityForResult(ppIntent, REQUEST_CODE_FUTURE_PAYMENT);
                spiceManager.removeDataFromCache(IabSimpleResponseJson.class, "authorization-" + token);
            }
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getCurrency() {
            return currency;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public double getTax() {
            return tax;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT < 8){
            Toast.makeText(this, "In-App Billing not supported in this device.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent paypalIntent = new Intent(this, PayPalService.class);
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(paypalIntent);
        final Intent intent = getIntent();

        packageName = intent.getStringExtra("packageName");
        token = intent.getStringExtra("token");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if(LOG){Log.d(TAG,"BasePurchaseActivity onActivityResult");}
        switch (requestCode){
            case REQUEST_CODE_PAYMENT:
                if (resultCode == Activity.RESULT_OK) {
                    PaymentConfirmation confirm =
                            data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                    if (confirm != null) {
                        try {
                            Log.i(TAG, confirm.toJSONObject().toString(4));
                            Log.i(TAG, confirm.getProofOfPayment().toJSONObject().toString(4));
                            Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                            /**
                             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                             * or consent completion.
                             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                             * for more details.
                             *
                             * For sample mobile backend interactions, see
                             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                             */
                            processPaymentConfirmation(confirm.getProofOfPayment());


                        } catch (JSONException e) {
                            Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i(TAG, "The user canceled.");
                } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();

                    Log.i(TAG,
                            "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
                }
                break;
            case REQUEST_CODE_FUTURE_PAYMENT:
                if (resultCode == Activity.RESULT_OK) {
                    PayPalAuthorization auth =
                            data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                    if (auth != null) {
                        try {
                            Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));
                            String authorization_code = auth.getAuthorizationCode();
                            Log.i("FuturePaymentExample", authorization_code);
                            sendAuthorizationToServer(auth);
                        } catch (JSONException e) {
                            Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i("FuturePaymentExample", "The user canceled.");
                    Log.i("FuturePaymentExample",
                            "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration." +
                                    " Please see the docs.");
                } else {
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void sendAuthorizationToServer(final PayPalAuthorization authorization) {

        /**
         * TODO: Send the authorization response to your server, where it can
         * exchange the authorization code for OAuth access and refresh tokens.
         *
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         *
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */


        PaypalPurchaseAuthorizationRequest request = new PaypalPurchaseAuthorizationRequest();

        request.setAuthToken(authorization.getAuthorizationCode());
        request.setToken(token);
        DialogFragment df = new ProgressDialogFragment();
        df.setCancelable(false);
        df.show(getSupportFragmentManager(), "pleaseWaitDialog");

        spiceManager.execute(request, new RequestListener<IabSimpleResponseJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Toast.makeText(Aptoide.getContext(), R.string.failed_auth_code, Toast.LENGTH_LONG).show();
                dismissAllowingStateLoss();
            }

            @Override
            public void onRequestSuccess(IabSimpleResponseJson iabPurchaseStatusJson) {
                String correlationId = PayPalConfiguration.getApplicationCorrelationId(BasePurchaseActivity.this);
                PayProductRequestPayPal request = new PayProductRequestPayPal();
                request.setToken(token);
                if(simcc!=null && simcc.length()>0){
                    request.setSimCountryCode(simcc);
                }
                request.setPrice(String.valueOf(valuesToVerify.price));
                request.setCurrency(valuesToVerify.currency);
                request.setProductId(String.valueOf(aptoideProductId));
                request.setCorrelationId(correlationId);
                request.setRetryPolicy(noRetryPolicy);
                requestsetExtra(request);

                if("OK".equals(iabPurchaseStatusJson.getStatus())) {
                    spiceManager.execute(request, new PurchaseRequestListener());
                }else{
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
                    dismissAllowingStateLoss();
                }
            }
        });
    }

    RetryPolicy noRetryPolicy = new RetryPolicy() {
        @Override
        public int getRetryCount() {
            return 0;
        }

        @Override
        public void retry(SpiceException e) {

        }

        @Override
        public long getDelayBeforeRetry() {
            return 0;
        }
    };

    protected abstract void processPaymentConfirmation(final ProofOfPayment confirmation);

    protected Intent buildIntentForAlarm(ProofOfPayment confirmation, String paymenType){
        Intent i = new Intent("PAYPAL_PAYMENT");
        i.putExtra("token", token);
        i.putExtra("aptoideProductId", aptoideProductId);
        i.putExtra("tax", valuesToVerify.tax);
        i.putExtra("price", valuesToVerify.price);
        i.putExtra("currency", valuesToVerify.currency);
        i.putExtra("paymentId", confirmation.getPaymentId());
        i.putExtra("paymentType", paymenType);
        if(simcc!=null)i.putExtra("simcc", simcc);
        return i;
    }

    protected abstract BasePurchaseStatusRequest BuildPurchaseStatusRequest();

    protected BasePurchaseStatusRequest BuildPurchaseStatusRequest(ProofOfPayment confirmation){
        final BasePurchaseStatusRequest purchaseStatus = BuildPurchaseStatusRequest();
        purchaseStatus.setToken(token);
        purchaseStatus.setProductId(aptoideProductId);
        purchaseStatus.setPayType(1);
        purchaseStatus.setPayKey(confirmation.getPaymentId());
        purchaseStatus.setTaxRate(valuesToVerify.tax);
        purchaseStatus.setPrice(valuesToVerify.price);
        purchaseStatus.setCurrency(valuesToVerify.currency);
        purchaseStatus.setRepo(valuesToVerify.repo);
        if(simcc!=null)purchaseStatus.setSimcc(simcc);
        return purchaseStatus;
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));

        super.onDestroy();

        if (execute != null && !execute.isCancelled()) {
            execute.cancel(false);
        }
        releaseService();
    }

    protected void dismissAllowingStateLoss(){
        DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
        if (pd != null) {
            pd.dismissAllowingStateLoss();
        }
    }

    public class PurchaseRequestListener implements RequestListener<IabPurchaseStatusJson>{
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
            dismissAllowingStateLoss();
        }

        @Override
        public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
            Intent intent = new Intent();
            dismissAllowingStateLoss();

            if (iabPurchaseStatusJson != null) {
                if ("OK".equals(iabPurchaseStatusJson.getStatus())) {
                    if (makeExtraTestsOnPurchaseOk(iabPurchaseStatusJson)) {
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                    }
                    if ("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                        intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                        intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                    } else {
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                        Toast.makeText(Aptoide.getContext(),
                                "Transaction result is: " + iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState(),
                                Toast.LENGTH_LONG).show();
                    }
                }

            } else {
                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
            }
            if(LOG){Log.d(TAG, "PurchaseRequestListener finish setResult Result ok.");}
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    protected boolean makeExtraTestsOnPurchaseOk(IabPurchaseStatusJson iabPurchaseStatusJson){
        return false;
    }

    private void telephonyPayment(PaymentServices service,LinearLayout paymentMethodsLayout,int buttonResource,View.OnClickListener onClick){
        Button button;
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager!=null && telephonyManager.getSimState()== TelephonyManager.SIM_STATE_READY){
            for (PaymentServices.PaymentType type : service.getTypes()) {
                button = (Button) LayoutInflater.from(this).inflate(buttonResource, null);
                if (button != null) {
                    button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                    paymentMethodsLayout.addView(button);
                    button.setOnClickListener(onClick);
                }
            }
        }
    }

    protected void caseUNITEL(PaymentServices service,LinearLayout paymentMethodsLayout){
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final DecimalFormat df = new DecimalFormat("######.#");
        telephonyPayment(service,paymentMethodsLayout,R.layout.button_unitel,new UnitelPurchaseListener(getSupportFragmentManager(),
                            String.valueOf(service.getPrice()),
                            telephonyManager.getSimOperatorName(),
                            service.getName(),
                            service.getId(), telephonyManager.getSubscriberId(),
                            service.getCurrency(),
                            df.format(service.getPrice())));
    }
    protected void caseFortumo(final int id,final PaymentServices service,LinearLayout paymentMethodsLayout){
        telephonyPayment(service,paymentMethodsLayout,R.layout.button_fortumo,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LOG){Log.d(TAG, "clicked on fortumo!");}
                initService(id,false,service);

            }
        });
    }

    public class FortumoServiceConnection implements ServiceConnection {
        int prodID;
        boolean consumable;
        PaymentServices payService;

        public FortumoServiceConnection(int id, boolean consumable, PaymentServices service){
            this.prodID = id;
            this.consumable=consumable;
            this.payService=service;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(LOG){Log.d(TAG, "FortumoServiceConnection - onServiceConnected");}
            iservice = IRemoteFortumoPayment.Stub.asInterface( service);
            try {
                final Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(LOG){Log.d(TAG, "handling the Message");}
                        Bundle b = msg.getData();
                        callAfterFortumo(b.getInt(RESULT_EXTRA_PRODID_INT),
                                b.getString(RESULT_EXTRA_PAYCODE_STRING),
                                b.getBoolean(RESULT_EXTRA_ISCONSUMABLE_BOOL));
                    }
                };
                iservice.getMessage(String.valueOf(prodID), consumable,
                        SecurePreferences.getInstance().getInt("User_ID",0),
                        payService.service_id,
                        payService.inapp_secret,
                        payService.getName(),
                        new Messenger(handler));
            } catch (Exception e) {
                Log.e(TAG, "Error: "+e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(LOG){Log.d(TAG, "FortumoServiceConnection - onServiceDisconnected");}
            iservice=null;
        }
    }

    private static final String RESULT_EXTRA_ISCONSUMABLE_BOOL = "IEIC";
    private static final String RESULT_EXTRA_PAYCODE_STRING = "IEPC";
    private static final String RESULT_EXTRA_PRODID_INT = "IEPID";

    private IRemoteFortumoPayment iservice;
    private FortumoServiceConnection connection;
    private void initService(int id, boolean consumable, PaymentServices service) {
        if(LOG){Log.d(TAG, "initService() ");}
        connection = new FortumoServiceConnection(id, consumable, service);
        Intent i = new Intent("cm.aptoide.ptdev.FortumoPaymentService");
        i.setAction("Aptoide.FortumoPaymentService");

        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        if(!ret) {
            Toast.makeText(this,"Aptoide Fortumo not Installed!", Toast.LENGTH_LONG).show();
        }
    }

    /** Unbinds this activity from the service. */
    private void releaseService() {
        if(LOG){Log.d(TAG, "releaseService() unbound.");}
        if(connection==null) return;
        unbindService(connection);
        connection = null;
    }

    private void callAfterFortumo(int prodID,String payCode,boolean isconsumable){
        BasePurchaseStatusRequest request = isconsumable ? new IabPurchaseStatusRequest() : new PaidAppPurchaseStatusRequest();
        request.setPayKey(payCode);
        request.setProductId(prodID);
        request.setPayreqtype("notification");
        spiceManager.execute(request,1, DurationInMillis.ALWAYS_EXPIRED,new RequestListener<IabPurchaseStatusJson>(){

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                if(LOG){Log.d(TAG, "RequestListener onRequestFailure");}
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                if(LOG){Log.d(TAG, "RequestListener onRequestSuccess");}

                if("OK".equals(iabPurchaseStatusJson.getStatus())){
                    if(LOG){Log.d(TAG, "RequestListener onOK");}
                    Intent i = new Intent();
                    i.putExtra("Package",packageName);
                        if(LOG){Log.d(TAG, "Fortumo Result ok.");}
                    setResult(RESULT_OK,i);
                }else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        });
    }

}
