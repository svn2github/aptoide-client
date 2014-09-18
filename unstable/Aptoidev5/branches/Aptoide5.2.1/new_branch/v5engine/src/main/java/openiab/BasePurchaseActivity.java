package openiab;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.octo.android.robospice.SpiceManager;
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

import java.math.BigDecimal;
import java.util.HashMap;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import openiab.webservices.IabPurchaseAuthorizationRequest;
import openiab.webservices.IabPurchaseStatusRequest;
import openiab.webservices.PayProductRequestBase;
import openiab.webservices.PayProductRequestPayPal;
import openiab.webservices.PayProductRequestUnitel;
import openiab.webservices.json.IabPurchaseStatusJson;
import openiab.webservices.json.IabSimpleResponseJson;

public abstract class BasePurchaseActivity extends ActionBarActivity implements Callback {

    private static final String TAG = "paymentExample";

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
                .merchantName("Aptoide")
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
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
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onStartSession(this, "X89WPPSKWQB2FT6B8F3X");
    }

    @Override
    protected void onStop() {
        super.onStop();
        spiceManager.shouldStop();
        if(Build.VERSION.SDK_INT >= 10) FlurryAgent.onEndSession(this);
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
/*


    private String getMccCode(String networkOperator) {
        return networkOperator == null ? "" : networkOperator.substring(0, mncPortionLength(networkOperator));

    }

    private String getMncCode(String networkOperator) {
        return networkOperator == null ? "" : networkOperator.substring(mncPortionLength(networkOperator));

    }

    private int mncPortionLength(String networkOperator) {
        return Math.min(3, networkOperator.length());
    }



    protected abstract boolean TestifPurchased(IBinder service);
    protected abstract IabSkuDetailsRequest AddWhatevertoConnectionRequest(IabSkuDetailsRequest request);

    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            isBound = true;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try{
                        repo = new Database(Aptoide.getDb()).getRollbackRepo(packageName);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(TestifPurchased(service)){
                        Intent intent = new Intent();
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ITEM_ALREADY_OWNED);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                        //token = accountManager.blockingGetAuthToken(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0], "Full access", true);

                        //String token =  "27286b943179065fcef3b6adcafe8680d8515e4652010602c45f6";
                        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                        IabSkuDetailsRequest request = new IabSkuDetailsRequest();

                        request.setPackageName(packageName);
                        request.setToken(token);
                        //request.setOemid(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);

                        if (telephonyManager != null && telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY) {
                            request.setMcc(getMccCode(telephonyManager.getNetworkOperator()));
                            request.setMnc(getMncCode(telephonyManager.getNetworkOperator()));
                            simcc = telephonyManager.getSimCountryIso();
                            request.setSimcc(simcc);
                        }
                        request= AddWhatevertoConnectionRequest(request);

                        spiceManager.execute(request, packageName + "-getSkuDetails-" + request.getSkuList() + token, DurationInMillis.ONE_MINUTE, new RequestListener<IabSkuDetailsJson>() {
                            @Override
                            public void onRequestFailure(SpiceException spiceException) {
                                Intent intent = new Intent();
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onRequestSuccess(IabSkuDetailsJson response) {
                                ArrayList<String> detailsList = new ArrayList<String>();

                                if ("OK".equals(response.getStatus())) {

                                    for (IabSkuDetailsJson.PublisherResponse.PurchaseDataObject details : response.getPublisher_response().getDetails_list()) {
                                        detailsList.add(details.getJson());
                                        Log.d("AptoideBillingService", "Sku Details: " + details.getJson());
                                    }


                                    LinearLayout paymentMethodsLayout = (LinearLayout) findViewById(R.id.payment_methods);
                                    paymentMethodsLayout.removeAllViews();
                                    Button button;

                                    if (response.getPayment_services()!=null && response.getPayment_services().isEmpty()) {
                                        TextView noPaymentsFound = new TextView(BasePurchaseActivity.this);
                                        noPaymentsFound.setText(R.string.no_payments_available);
                                        paymentMethodsLayout.addView(noPaymentsFound);
                                    }else if(response.getPayment_services() !=null ){

                                        for (PaymentServices service : response.getPayment_services()) {

                                            int serviceCode = servicesList.get(service.getShort_name());

                                            switch (serviceCode) {
                                                case PAYPAL_CODE:
                                                    for (PaymentServices.PaymentType type : service.getTypes()) {

                                                        if ("future".equals(type.getReqType())) {

                                                            button = (Button) LayoutInflater.from(BasePurchaseActivity.this).inflate(R.layout.button_paypal, null);
                                                            button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                                            paymentMethodsLayout.addView(button);

                                                            IabPurchaseAuthorizationRequest request = new IabPurchaseAuthorizationRequest();
                                                            request.setToken(token);
                                                            HasAuthorization hasAuthorization = new HasAuthorization(button);
                                                            hasAuthorization.setCurrency(service.getCurrency());
                                                            hasAuthorization.setPrice(service.getPrice());
                                                            hasAuthorization.setTax(service.getTaxRate());
                                                            spiceManager.execute(request, "authorization-" + token, DurationInMillis.ONE_DAY, hasAuthorization);


                                                        } else if ("single".equals(type.getReqType())) {

                                                            button = (Button) LayoutInflater.from(BasePurchaseActivity.this).inflate(R.layout.button_visa, null);

                                                            button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                                            OnPaypalClick onPaypalClick = new OnPaypalClick();
                                                            onPaypalClick.setCurrency(service.getCurrency());
                                                            onPaypalClick.setPrice(service.getPrice());
                                                            onPaypalClick.setTax(service.getTaxRate());
                                                            onPaypalClick.setRepo(repo);


                                                            try {
                                                                JSONObject sku_details = new JSONObject(detailsList.get(0));
                                                                onPaypalClick.setDescription(sku_details.getString("title") + " - " + sku_details.getString("description"));

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                onPaypalClick.setDescription("No description");
                                                            }

                                                            button.setOnClickListener(onPaypalClick);
                                                            paymentMethodsLayout.addView(button);

                                                            if(response.getPayment_services().size() == 1 && service.getTypes().size() == 1){
                                                                onPaypalClick.onClick(null);
                                                            }

                                                        }
                                                    }
                                                    break;
                                                case UNITEL_CODE:

                                                    if(telephonyManager!=null && telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY){



                                                        for (PaymentServices.PaymentType type : service.getTypes()) {
                                                            button = (Button) LayoutInflater.from(BasePurchaseActivity.this).inflate(R.layout.button_carrier, null);

                                                            if (button != null) {
                                                                button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                                                paymentMethodsLayout.addView(button);
                                                                DecimalFormat df = new DecimalFormat("######.#");
                                                                button.setOnClickListener(new UnitelPurchaseListener(getSupportFragmentManager(), String.valueOf(response.getPublisher_response().getDetails_list().get(0).getPrice()), telephonyManager.getSimOperatorName(), response.getPublisher_response().getDetails_list().get(0).getTitle(), service.getId(), telephonyManager.getSubscriberId(), service.getCurrency(), df.format(service.getPrice())));
                                                            }
                                                        }

                                                    }


                                                    break;
//                                            case FORTUMO_CODE:
//
//                                                for (PaymentServices.PaymentType type : service.getTypes()) {
//                                                    button = (Button) LayoutInflater.from(PurchaseActivity.this).inflate(R.layout.button_carrier, null);
//                                                    button.setText(type.getLabel());
//                                                    paymentMethodsLayout.addView(button);
//                                                }
//
//
//                                                break;
                                            }


                                        }
                                    }

                                    if(!detailsList.isEmpty()) Log.d("AptoideSkudetailsForPurchase", "SkuDetails: " + detailsList.get(0));

                                    //if(detailsJson != null) {
                                    try {
                                        findViewById(R.id.progress).setVisibility(View.GONE);
                                        findViewById(R.id.content).setVisibility(View.VISIBLE);

                                        JSONObject sku_details = new JSONObject(detailsList.get(0));
                                        aptoideProductId = response.getMetadata().getId();
                                        ((TextView) findViewById(R.id.title)).setText(sku_details.getString("title"));
                                        ((TextView) findViewById(R.id.price)).setText(sku_details.getString("price"));
                                        ImageLoader.getInstance().displayImage(response.getMetadata().getIcon(), (ImageView) findViewById(R.id.icon));
                                        ((TextView) findViewById(R.id.app_purchase_description)).setText(sku_details.getString("description"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Intent intent = new Intent();
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                }
                            }

                        });
                }
            }).start();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
*/

    protected abstract void requestsetExtra(PayProductRequestBase pprb);

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
        //requestUnitel.setOemId(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);
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
            if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Purchase_Page_Clicked_On_Paypal_Button");
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

        if (requestCode == REQUEST_CODE_PAYMENT) {
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
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
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


        IabPurchaseAuthorizationRequest request = new IabPurchaseAuthorizationRequest();

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
    protected Intent BuildIntentForAlarm(ProofOfPayment confirmation){
        Intent i = new Intent("PAYPAL_PAYMENT");
        i.putExtra("token", token);
        i.putExtra("aptoideProductId", aptoideProductId);
        i.putExtra("tax", valuesToVerify.tax);
        i.putExtra("price", valuesToVerify.price);
        i.putExtra("currency", valuesToVerify.currency);
        i.putExtra("paymentId", confirmation.getPaymentId());
        if(simcc!=null)i.putExtra("simcc", simcc);
        return i;
    }
    protected IabPurchaseStatusRequest BuildPurchaseStatusRequest(ProofOfPayment confirmation){
        final IabPurchaseStatusRequest purchaseStatus = new IabPurchaseStatusRequest();
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
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    protected abstract boolean makeExtraTestsOnPurchaseOk(IabPurchaseStatusJson iabPurchaseStatusJson);
}
