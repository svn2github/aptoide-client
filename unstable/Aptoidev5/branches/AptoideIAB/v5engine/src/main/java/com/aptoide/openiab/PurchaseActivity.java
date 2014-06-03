package com.aptoide.openiab;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import com.aptoide.openiab.webservices.IabPurchaseStatusRequest;
import com.aptoide.openiab.webservices.json.IabPurchaseStatusJson;
import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.IOpenInAppBillingService;

import java.util.ArrayList;

/**
 * Created by j-pac on 12-02-2014.
 */
public class PurchaseActivity extends ActionBarActivity {


    private String packageName;
    private int apiVersion;
    private String type;
    private String sku;
    AsyncTask<Void, Void, Bundle> execute;
    private int aptoideProductId;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            isBound = true;

            ArrayList<String> item_list = new ArrayList<String>();
            item_list.add(sku);
            final Bundle sku_bundle = new Bundle();
            sku_bundle.putStringArrayList(BillingBinder.ITEM_ID_LIST, item_list);



            if(login) {
                execute = new AsyncTask<Void, Void, Bundle>() {



                    @Override
                    protected Bundle doInBackground(Void... params) {
                        try {
                            return ((IOpenInAppBillingService) service).getSkuDetails(apiVersion, packageName, type, sku_bundle);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bundle result) {
                        if (result != null) {
                            if (result.getInt(BillingBinder.RESPONSE_CODE) == BillingBinder.RESULT_OK) {

                                ArrayList<String> detailsList = result.getStringArrayList(BillingBinder.DETAILS_LIST);

                                /*
                                for(String details : detailsList) {
                                    try {
                                        Log.d("AptoideSkudetailsForPurchase", "SkuDetails: " + details);
                                        JSONObject sku_details = new JSONObject(details);
                                        aptoideProductId = sku_details.getString("aptoideProductId");
                                        ((TextView) findViewById(R.id.title)).setText(sku_details.getString("title"));
                                        ((TextView) findViewById(R.id.price)).setText(sku_details.getString("price"));
                                        ((TextView) findViewById(R.id.description)).setText(sku_details.getString("description"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }*/

                                Log.d("AptoideSkudetailsForPurchase", "SkuDetails: " + detailsList.get(0));

                                //if(detailsJson != null) {
                                try {
                                    findViewById(R.id.progress).setVisibility(View.GONE);
                                    findViewById(R.id.content).setVisibility(View.VISIBLE);
                                    findViewById(R.id.buttonContinue).setEnabled(true);
                                    JSONObject sku_details = new JSONObject(detailsList.get(0));
                                    aptoideProductId = sku_details.getInt("aptoideProductId");
                                    ((TextView) findViewById(R.id.title)).setText(sku_details.getString("title"));
                                    ((TextView) findViewById(R.id.price)).setText(sku_details.getString("price"));
                                    ((TextView) findViewById(R.id.app_purchase_description)).setText(sku_details.getString("description"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Intent intent = new Intent();
                                    intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                                //}

                                //findViewById(R.id.button_ok).setOnClickListener(buttonOkListener);
                                return;

                            } else {
                                Intent intent = new Intent();
                                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                        }

                        Intent intent = new Intent();
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }.execute();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private boolean isBound;
    private String token;
    Spinner spinner;
    private boolean login = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.page_app_purchase);

        final Intent intent = getIntent();

        packageName = intent.getStringExtra("packageName");
        sku = intent.getStringExtra("sku");
        apiVersion = intent.getIntExtra("apiVersion", 0);
        type = intent.getStringExtra("type");
        token = intent.getStringExtra("token");
        spinner = (Spinner) findViewById(R.id.payment_spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{"PayPal - ExpressCheckout", "PayPal - Pre-Approval payment"}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView tv=new TextView(getContext());
                tv.setText(getItem(position));
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                return tv;
            }
        });

        String user = intent.getStringExtra("user");

        if(user==null){
            user = "Not Logged in";
            login = false;
            AccountManager.get(this).addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bnd = future.getResult();
                        intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_USER_CANCELED);
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }, new Handler(Looper.getMainLooper()));
        }
        ((TextView) findViewById(R.id.username)).setText("Account: " + user);

        Intent billingIntent = new Intent(this, BillingService.class);
        bindService(billingIntent, mConnection, BIND_AUTO_CREATE);



        findViewById(R.id.buttonCancel).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick (View v){
                Log.d("AptoideinAppBilling", "Purchase Cancelled");
                intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_USER_CANCELED);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        findViewById(R.id.buttonContinue).setOnClickListener(buttonOkListener);

    }

    private Button.OnClickListener buttonOkListener;

    {
        buttonOkListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i;
                switch (spinner.getSelectedItemPosition()){
                    case 1:

                        i = new Intent(PurchaseActivity.this, Buy.class);
                        i.putExtra("apiVersion", apiVersion);
                        i.putExtra("token", token);
                        i.putExtra("aptoideProductId", aptoideProductId);
                        i.putExtra("paymentTypeId", 1);
                        startActivityForResult(i, 757);

                        break;
                    case 0:

                        i = new Intent(PurchaseActivity.this, CreditCard.class);
                        i.putExtra("apiVersion", apiVersion);
                        i.putExtra("token", token);
                        i.putExtra("aptoideProductId", aptoideProductId);
                        i.putExtra("paymentTypeId", 1);
                        startActivityForResult(i, 757);

                        break;
                }






            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intent = getIntent();
        if(requestCode == 757) {
            if(resultCode == RESULT_OK) {

                int orderId = data.getIntExtra("orderId", -1);
                String token = data.getStringExtra("token");



                final IabPurchaseStatusRequest purchaseStatus = new IabPurchaseStatusRequest();
                purchaseStatus.setApiVersion(apiVersion);
                purchaseStatus.setToken(token);
                purchaseStatus.setOrderId(orderId);

                new AsyncTask<Void, Void, IabPurchaseStatusJson>() {

                    @Override
                    protected IabPurchaseStatusJson doInBackground(Void... params) {
                        try {
                            return purchaseStatus.loadDataFromNetwork();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(IabPurchaseStatusJson iabPurchaseStatusJson) {

                        if(iabPurchaseStatusJson != null) {
                            if("OK".equals(iabPurchaseStatusJson.getStatus())) {

                                if(!sku.equals(iabPurchaseStatusJson.getPublisherResponse().getItem().get(0))) {
                                    intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                if("COMPLETED".equals(iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getPurchaseState())) {
                                    intent.putExtra(BillingBinder.INAPP_PURCHASE_DATA, iabPurchaseStatusJson.getPublisherResponse().getData().get(0).getJson());
                                    intent.putExtra(BillingBinder.INAPP_DATA_SIGNATURE, iabPurchaseStatusJson.getPublisherResponse().getSignature().get(0));
                                    intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_OK);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                            }

                            intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_DEVELOPER_ERROR);
                            setResult(RESULT_OK, intent);

                        } else {
                            intent.putExtra(BillingBinder.RESPONSE_CODE, BillingBinder.RESULT_ERROR);
                            setResult(RESULT_OK, intent);
                        }
                    }
                }.execute();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isBound) {
            unbindService(mConnection);
        }

        if(execute!=null&&!execute.isCancelled()){
            execute.cancel(false);
        }
    }
}
