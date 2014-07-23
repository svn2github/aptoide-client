package com.aptoide.openiab;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.dialogs.ProgressDialogFragment;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import com.aptoide.openiab.webservices.*;
import com.aptoide.openiab.webservices.json.IabPurchaseStatusJson;
import com.aptoide.openiab.webservices.json.IabSimpleResponseJson;
import com.aptoide.openiab.webservices.json.IabSkuDetailsJson;
import com.aptoide.openiab.webservices.json.PaymentServices;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.retry.RetryPolicy;
import com.paypal.android.sdk.payments.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by j-pac on 12-02-2014.
 */
public class PurchaseActivity extends ActionBarActivity implements Callback {

    private static final String TAG = "paymentExample";


    static HashMap<String, Integer> servicesList = new HashMap<String, Integer>();
    private static final String PAYPAL_NAME = "paypal";
    private static final String UNITEL_NAME = "unitel";
    private static final String FORTUMO_NAME = "fortumo";

    private static final int PAYPAL_CODE = 1;
    private static final int UNITEL_CODE = 2;
    private static final int FORTUMO_CODE = 3;

    static {
        servicesList.put(PAYPAL_NAME, PAYPAL_CODE);
        servicesList.put(UNITEL_NAME, UNITEL_CODE);
        servicesList.put(FORTUMO_NAME, FORTUMO_CODE);
    }


    private String packageName;
    private int apiVersion;
    private String type;
    private String sku;
    AsyncTask<Void, Void, Bundle> execute;
    private int aptoideProductId;

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
    private String currency;
    private double tax;
    private String developerPayload;

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

    private double price;

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


    private String getMccCode(String networkOperator) {
        return networkOperator == null ? "" : networkOperator.substring(0, mncPortionLength(networkOperator));

    }

    private String getMncCode(String networkOperator) {
        return networkOperator == null ? "" : networkOperator.substring(mncPortionLength(networkOperator));

    }

    private int mncPortionLength(String networkOperator) {
        return Math.min(3, networkOperator.length());
    }

    private String simcc;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            isBound = true;


            new Thread(new Runnable() {
                @Override
                public void run() {
                    AccountManager accountManager = AccountManager.get(PurchaseActivity.this);

                    try {
                        Bundle bundle = ((BillingBinder)service).getPurchases(apiVersion, packageName, type, "");

                        for(String json : bundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")){

                            if(sku.equals(json)){
                                Intent intent = new Intent();
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ITEM_ALREADY_OWNED);
                                setResult(RESULT_OK, intent);
                                finish();
                                return;
                            }

                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        finish();
                        return;
                    }
                    ArrayList<String> item_list = new ArrayList<String>();
                    item_list.add(sku);

                    try {
                        token = accountManager.blockingGetAuthToken(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0], "Full access", true);

                        //String token =  "27286b943179065fcef3b6adcafe8680d8515e4652010602c45f6";
                        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                        IabSkuDetailsRequest request = new IabSkuDetailsRequest();
                        request.setApiVersion(Integer.toString(apiVersion));
                        request.setPackageName(packageName);
                        request.setToken(token);
                        //request.setOemid(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);

                        if (telephonyManager != null && telephonyManager.getSimState()==TelephonyManager.SIM_STATE_READY) {
                            request.setMcc(getMccCode(telephonyManager.getNetworkOperator()));
                            request.setMnc(getMncCode(telephonyManager.getNetworkOperator()));
                            simcc = telephonyManager.getSimCountryIso();
                            request.setSimcc(simcc);
                        }


                        for (String itemId : item_list) {
                            request.addToSkuList(itemId);
                            Log.d("Aptoide-Binder", "Sku details request for " + itemId);
                        }

                        final CountDownLatch latch = new CountDownLatch(1);
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
                                        TextView noPaymentsFound = new TextView(PurchaseActivity.this);
                                        noPaymentsFound.setText(R.string.no_payments_available);
                                        paymentMethodsLayout.addView(noPaymentsFound);
                                    }else if(response.getPayment_services() !=null ){

                                        for (PaymentServices service : response.getPayment_services()) {

                                            int serviceCode = servicesList.get(service.getShort_name());

                                            switch (serviceCode) {
                                                case PAYPAL_CODE:

                                                    for (PaymentServices.PaymentType type : service.getTypes()) {

                                                        if ("future".equals(type.getReqType())) {

                                                            button = (Button) LayoutInflater.from(PurchaseActivity.this).inflate(R.layout.button_paypal, null);
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

                                                            button = (Button) LayoutInflater.from(PurchaseActivity.this).inflate(R.layout.button_visa, null);

                                                            button.setText(type.getLabel() + " - " + service.getPrice() + " " + service.getSign());
                                                            OnPaypalClick onPaypalClick = new OnPaypalClick();
                                                            onPaypalClick.setCurrency(service.getCurrency());
                                                            onPaypalClick.setPrice(service.getPrice());
                                                            onPaypalClick.setTax(service.getTaxRate());


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
                                                        button = (Button) LayoutInflater.from(PurchaseActivity.this).inflate(R.layout.button_carrier, null);

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
                                        ImageLoader.getInstance().displayImage(response.getMetadata().getIcon(), (android.widget.ImageView) findViewById(R.id.icon));
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

                    } catch (android.accounts.OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }


                }
            }).start();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };





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
            requestUnitel.setApiVersion(String.valueOf(apiVersion));
            requestUnitel.setPrice(price);
            requestUnitel.setCurrency(currency);



            requestUnitel.setDeveloperPayload(developerPayload);
            //requestUnitel.setOemId(((AptoideConfigurationPartners) Aptoide.getConfiguration()).PARTNERID);
            requestUnitel.setRetryPolicy(noRetryPolicy);
            spiceManager.execute(requestUnitel, new RequestListener<IabPurchaseStatusJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
                    DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if (pd != null) {
                        pd.dismissAllowingStateLoss();
                    }

                }

                @Override
                public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                    Intent intent = new Intent();

                    DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");
                    if (pd != null) {
                        pd.dismissAllowingStateLoss();
                    }

                    if (iabPurchaseStatusJson != null) {


                        if ("OK".equals(iabPurchaseStatusJson.getStatus())) {

                            if (!sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0))) {
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            if ("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                                intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                                intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Toast.makeText(Aptoide.getContext(), "Transaction result is: " + iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState(), Toast.LENGTH_LONG).show();
                            }

                        }

                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });


        }

        @Override
        public void onCancel() {

        }




    public class ValuesToVerify {

        public double tax;
        public double price;
        public String currency;

    }

    ValuesToVerify valuesToVerify;


    public class OnPaypalClick implements View.OnClickListener {


        private String description;
        private double price;
        private String currency;
        private double tax;

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
            PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(price), currency, description,
                    PayPalPayment.PAYMENT_INTENT_SALE);

            Intent intent = new Intent(PurchaseActivity.this, PaymentActivity.class);
            PurchaseActivity.this.currency = currency;
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
    }

    private boolean alreadyRegistered = false;

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
            final Intent intent = getIntent();
            if (alreadyRegistered) {
                final String correlationId = PayPalConfiguration.getApplicationCorrelationId(PurchaseActivity.this);
                PayProductRequestPayPal request = new PayProductRequestPayPal();
                request.setToken(token);
                request.setProductId(productId);
                request.setDeveloperPayload(developerPayload);
                request.setApiVersion(String.valueOf(apiVersion));
                request.setPrice(String.valueOf(price));
                request.setCurrency(currency);
                if(simcc!=null && simcc.length()>0){
                    request.setSimCountryCode(simcc);
                }

                request.setCorrelationId(correlationId);
                request.setRetryPolicy(noRetryPolicy);

                DialogFragment df = new ProgressDialogFragment();
                df.show(getSupportFragmentManager(), "pleaseWaitDialog");
                df.setCancelable(false);
                spiceManager.execute(request, new RequestListener<IabPurchaseStatusJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
                        DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                        if (pd != null && pd.isAdded()) {
                            pd.dismiss();
                        }
                    }

                    @Override
                    public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                        try {

                            DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                            if (pd != null && pd.isAdded()) {
                                pd.dismiss();
                            }
                            if (iabPurchaseStatusJson != null) {

                                if ("OK".equals(iabPurchaseStatusJson.getStatus())) {

                                    if (!sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0))) {
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }

                                    if ("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                                        intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                                        intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Aptoide.getContext(), "Transaction result is: " + iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState(), Toast.LENGTH_LONG).show();
                                    }

                                }else{
                                    intent.putExtra(BillingBinder.RESPONSE_CODE, iabPurchaseStatusJson.getPublisherResponse().getResponse_code());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                            } else {
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } catch (Exception e) {
                            intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            } else {
                Intent ppIntent = new Intent(PurchaseActivity.this, PayPalFuturePaymentActivity.class);
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


    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AW47wxAycZoTcXd5KxcJPujXWwImTLi-GNe3XvUUwFavOw8Nq4ZnlDT1SZIY";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
                    // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Aptoide")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));


    private boolean isBound;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT < 8){
            Toast.makeText(this, "In-App Billing not supported in this device.", Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(R.layout.page_app_purchase);
        Intent paypalIntent = new Intent(this, PayPalService.class);
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(paypalIntent);

        final Intent intent = getIntent();
        developerPayload = intent.getStringExtra("developerPayload");

        packageName = intent.getStringExtra("packageName");
        sku = intent.getStringExtra("sku");
        //sku = "com.aptoide.partners";
        apiVersion = intent.getIntExtra("apiVersion", 3);
        type = intent.getStringExtra("type");
        token = intent.getStringExtra("token");
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
                            Intent billingIntent = new Intent(PurchaseActivity.this, BillingService.class);
                            bindService(billingIntent, mConnection, BIND_AUTO_CREATE);
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
            bindService(billingIntent, mConnection, BIND_AUTO_CREATE);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        final Intent intent = getIntent();


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

                Log.i(
                        TAG,
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


                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
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
                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                if (pd != null && pd.isAdded()) {
                    pd.dismiss();
                }
            }

            @Override
            public void onRequestSuccess(IabSimpleResponseJson iabPurchaseStatusJson) {
                final Intent intent = new Intent();

                String correlationId = PayPalConfiguration.getApplicationCorrelationId(PurchaseActivity.this);
                PayProductRequestPayPal request = new PayProductRequestPayPal();
                request.setToken(token);
                request.setApiVersion(String.valueOf(apiVersion));
                if(simcc!=null && simcc.length()>0){
                    request.setSimCountryCode(simcc);
                }


                request.setPrice(String.valueOf(valuesToVerify.price));
                request.setCurrency(valuesToVerify.currency);



                request.setDeveloperPayload(developerPayload);
                request.setProductId(String.valueOf(aptoideProductId));
                request.setCorrelationId(correlationId);
                request.setRetryPolicy(noRetryPolicy);

                if("OK".equals(iabPurchaseStatusJson.getStatus())) {

                    spiceManager.execute(request, new RequestListener<IabPurchaseStatusJson>() {
                        @Override
                        public void onRequestFailure(SpiceException spiceException) {
                            Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
                            DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                            if (pd != null && pd.isAdded()) {
                                pd.dismiss();
                            }
                        }

                        @Override
                        public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                            DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                            Intent intent = new Intent();


                            if (pd != null && pd.isAdded()) {
                                pd.dismiss();
                            }

                            try{



                                if ("OK".equals(iabPurchaseStatusJson.getStatus())) {

                                    if (!sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0))) {
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }

                                    if ("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                                        intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                                        intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                        setResult(RESULT_OK, intent);
                                        Toast.makeText(Aptoide.getContext(), "Transaction result is: " + iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState(), Toast.LENGTH_LONG).show();
                                        finish();
                                    }

                                } else {
                                    intent.putExtra(BillingBinder.RESPONSE_CODE, iabPurchaseStatusJson.getPublisherResponse().getResponse_code());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                            }catch (Exception e){
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                setResult(RESULT_OK, intent);
                                finish();
                            }


                        }
                    });
                }else{
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured_paying, Toast.LENGTH_LONG).show();
                    DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                    if (pd != null && pd.isAdded()) {
                        pd.dismiss();
                    }
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

    private void processPaymentConfirmation(final ProofOfPayment confirmation) {


        final IabPurchaseStatusRequest purchaseStatus = new IabPurchaseStatusRequest();
        purchaseStatus.setApiVersion(apiVersion);
        purchaseStatus.setToken(token);
        purchaseStatus.setRest(true);

        purchaseStatus.setProductId(aptoideProductId);
        purchaseStatus.setPayType(1);
        purchaseStatus.setPayKey(confirmation.getPaymentId());
        purchaseStatus.setTaxRate(valuesToVerify.tax);
        purchaseStatus.setPrice(valuesToVerify.price);
        purchaseStatus.setDeveloperPayload(developerPayload);
        purchaseStatus.setCurrency(valuesToVerify.currency);

        if(simcc!=null)purchaseStatus.setSimcc(simcc);
        final Intent intent = getIntent();

        DialogFragment df = new ProgressDialogFragment();
        df.show(getSupportFragmentManager(), "pleaseWaitDialog");
        df.setCancelable(false);



        spiceManager.execute(purchaseStatus, new RequestListener<IabPurchaseStatusJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Toast.makeText(Aptoide.getContext(), R.string.error_occured_retry_later, Toast.LENGTH_LONG).show();

                Intent i = new Intent("PAYPAL_PAYMENT");
                i.putExtra("token", token);
                i.putExtra("apiVersion", apiVersion);
                i.putExtra("aptoideProductId", aptoideProductId);
                i.putExtra("developerPayload", developerPayload);
                i.putExtra("tax", valuesToVerify.tax);
                i.putExtra("price", valuesToVerify.price);
                i.putExtra("currency", valuesToVerify.currency);
                i.putExtra("paymentId", confirmation.getPaymentId());
                if(simcc!=null)i.putExtra("simcc", simcc);

                PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
                manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60000, 60000, intent);

                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                if (pd != null && pd.isAdded()) {
                    pd.dismissAllowingStateLoss();
                }
            }

            @Override
            public void onRequestSuccess(IabPurchaseStatusJson iabPurchaseStatusJson) {
                DialogFragment pd = (DialogFragment) getSupportFragmentManager().findFragmentByTag("pleaseWaitDialog");

                if (pd != null && pd.isAdded()) {
                    pd.dismissAllowingStateLoss();
                }
                if (iabPurchaseStatusJson != null) {

                    if ("OK".equals(iabPurchaseStatusJson.getStatus())) {

                        if (!sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0))) {
                            intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                        if ("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                            intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                            intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                            intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(Aptoide.getContext(), "Transaction result is: " + iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState(), Toast.LENGTH_LONG).show();
                        }

                    }else{
                        intent.putExtra(BillingBinder.RESPONSE_CODE, iabPurchaseStatusJson.getPublisherResponse().getResponse_code());
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                } else {
                    intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                    setResult(RESULT_OK, intent);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));

        super.onDestroy();
        if (isBound) {
            unbindService(mConnection);
        }

        if (execute != null && !execute.isCancelled()) {
            execute.cancel(false);
        }


    }
}
